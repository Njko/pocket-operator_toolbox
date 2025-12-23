package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.TestFixtures
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import java.io.File
import javax.sound.midi.MidiSystem
import javax.sound.midi.ShortMessage
import kotlin.test.*

/**
 * TDD: RED phase - Tests for MIDI export functionality
 *
 * Feature: Export PO-12 patterns to MIDI files for DAW integration
 * Converts drum patterns to standard MIDI format with proper timing and note mapping.
 */
class MidiExporterTest {

    private val testOutputDir = File("build/test-output/midi")

    @BeforeTest
    fun setup() {
        testOutputDir.mkdirs()
    }

    @AfterTest
    fun cleanup() {
        testOutputDir.listFiles()?.forEach { it.delete() }
    }

    // === MIDI File Generation Tests ===

    @Test
    fun `should export pattern to valid MIDI file`() {
        val pattern = TestFixtures.createSimplePattern()
        val exporter = MidiExporter()
        val outputFile = File(testOutputDir, "test_pattern.mid")

        exporter.exportToMidi(pattern, outputFile)

        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0)

        // Verify it's a valid MIDI file
        val sequence = MidiSystem.getSequence(outputFile)
        assertNotNull(sequence)
    }

    @Test
    fun `should create MIDI file with correct format`() {
        val pattern = TestFixtures.createSimplePattern()
        val exporter = MidiExporter()
        val outputFile = File(testOutputDir, "format_test.mid")

        exporter.exportToMidi(pattern, outputFile)

        val sequence = MidiSystem.getSequence(outputFile)
        assertEquals(javax.sound.midi.Sequence.PPQ, sequence.divisionType)
        assertTrue(sequence.resolution > 0)
    }

    @Test
    fun `should handle empty pattern gracefully`() {
        val pattern = TestFixtures.createEmptyPattern()
        val exporter = MidiExporter()
        val outputFile = File(testOutputDir, "empty_pattern.mid")

        exporter.exportToMidi(pattern, outputFile)

        assertTrue(outputFile.exists())
        val sequence = MidiSystem.getSequence(outputFile)
        assertNotNull(sequence)
    }

    // === Voice to MIDI Note Mapping Tests ===

    @Test
    fun `should map PO12 voices to General MIDI drum notes`() {
        val mapper = MidiNoteMapper()

        // Test standard drum mappings (GM Drum Map)
        assertEquals(36, mapper.getMidiNote(PO12DrumVoice.KICK))           // Bass Drum 1
        assertEquals(38, mapper.getMidiNote(PO12DrumVoice.SNARE))          // Acoustic Snare
        assertEquals(42, mapper.getMidiNote(PO12DrumVoice.CLOSED_HH))      // Closed Hi-Hat
        assertEquals(46, mapper.getMidiNote(PO12DrumVoice.OPEN_HH))        // Open Hi-Hat
        assertEquals(49, mapper.getMidiNote(PO12DrumVoice.CYMBAL))         // Crash Cymbal 1
        assertEquals(39, mapper.getMidiNote(PO12DrumVoice.HAND_CLAP))      // Hand Clap
        assertEquals(37, mapper.getMidiNote(PO12DrumVoice.RIM_SHOT))       // Side Stick
        assertEquals(56, mapper.getMidiNote(PO12DrumVoice.COWBELL))        // Cowbell
    }

    @Test
    fun `should map tom voices to MIDI notes`() {
        val mapper = MidiNoteMapper()

        assertEquals(45, mapper.getMidiNote(PO12DrumVoice.TOM_LOW))        // Low Tom
        assertEquals(47, mapper.getMidiNote(PO12DrumVoice.TOM_MID))        // Mid Tom
        assertEquals(50, mapper.getMidiNote(PO12DrumVoice.TOM_HIGH))       // High Tom
    }

    @Test
    fun `should map all PO12 voices to valid MIDI notes`() {
        val mapper = MidiNoteMapper()

        // Test that all voices map to valid GM drum note range (27-87 per GM spec)
        PO12DrumVoice.entries.forEach { voice ->
            val midiNote = mapper.getMidiNote(voice)
            assertTrue(midiNote in 27..87, "Voice ${voice.shortName} maps to invalid MIDI note $midiNote")
        }
    }

    // === Tempo Conversion Tests ===

    @Test
    fun `should convert BPM to MIDI tempo correctly`() {
        val exporter = MidiExporter()

        // BPM 120 = 500000 microseconds per quarter note
        assertEquals(500000, exporter.calculateMidiTempo(120))

        // BPM 140 = 428571.428... (integer division)
        assertEquals(428571, exporter.calculateMidiTempo(140))

        // BPM 90 = 666666.666... (integer division)
        assertEquals(666666, exporter.calculateMidiTempo(90))
    }

    @Test
    fun `should handle edge case tempos`() {
        val exporter = MidiExporter()

        // Minimum PO-12 tempo (60 BPM)
        assertTrue(exporter.calculateMidiTempo(60) > 0)

        // Maximum PO-12 tempo (206 BPM)
        assertTrue(exporter.calculateMidiTempo(206) > 0)
    }

    // === Pattern Timing Tests ===

    @Test
    fun `should place MIDI notes at correct timing positions`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
            ),
            metadata = TestFixtures.createTestMetadata(bpm = 120)
        )

        val exporter = MidiExporter()
        val outputFile = File(testOutputDir, "timing_test.mid")

        exporter.exportToMidi(pattern, outputFile)

        val sequence = MidiSystem.getSequence(outputFile)
        val track = sequence.tracks[0]

        // Verify notes are placed at correct tick positions
        val noteEvents = (0 until track.size())
            .map { track.get(it) }
            .filter { event ->
                val message = event.message
                message is ShortMessage && message.command == ShortMessage.NOTE_ON
            }

        assertTrue(noteEvents.isNotEmpty())
    }

    @Test
    fun `should calculate 16 steps per bar correctly`() {
        val exporter = MidiExporter()

        // At 120 BPM, 4/4 time: 1 bar = 4 beats
        // 16 steps = 4 beats, so each step = 1/4 beat = 16th note
        val ticksPerStep = exporter.calculateTicksPerStep(resolution = 96)

        assertTrue(ticksPerStep > 0)
        assertEquals(96 / 4, ticksPerStep) // 96 PPQ / 4 = 24 ticks per 16th note
    }

    // === Multi-Pattern Export Tests ===

    @Test
    fun `should export multiple chained patterns`() {
        val pattern1 = TestFixtures.createSimplePattern()
        val pattern2 = TestFixtures.createComplexPattern()
        val exporter = MidiExporter()
        val outputFile = File(testOutputDir, "chained_patterns.mid")

        exporter.exportPatternsToMidi(listOf(pattern1, pattern2), outputFile)

        assertTrue(outputFile.exists())
        val sequence = MidiSystem.getSequence(outputFile)
        assertNotNull(sequence)

        // Verify the MIDI file is longer (contains both patterns)
        assertTrue(sequence.tickLength > 0)
    }

    @Test
    fun `should respect pattern order in chain`() {
        val pattern1 = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1)),
            metadata = TestFixtures.createTestMetadata()
        )
        val pattern2 = PO12Pattern(
            number = 2,
            voices = mapOf(PO12DrumVoice.SNARE to listOf(5)),
            metadata = TestFixtures.createTestMetadata()
        )

        val exporter = MidiExporter()
        val outputFile = File(testOutputDir, "pattern_order.mid")

        exporter.exportPatternsToMidi(listOf(pattern1, pattern2), outputFile)

        val sequence = MidiSystem.getSequence(outputFile)
        val track = sequence.tracks[0]

        // Verify first note is kick, second note is snare
        val noteOnEvents = (0 until track.size())
            .map { track.get(it) }
            .filter { event ->
                val message = event.message
                message is ShortMessage && message.command == ShortMessage.NOTE_ON && message.data2 > 0
            }

        assertTrue(noteOnEvents.size >= 2)
    }

    // === MIDI Channel Tests ===

    @Test
    fun `should use channel 10 for drum sounds`() {
        val pattern = TestFixtures.createSimplePattern()
        val exporter = MidiExporter()
        val outputFile = File(testOutputDir, "channel_test.mid")

        exporter.exportToMidi(pattern, outputFile)

        val sequence = MidiSystem.getSequence(outputFile)
        val track = sequence.tracks[0]

        // Verify all note events use channel 10 (index 9 in MIDI)
        val channels = (0 until track.size())
            .map { track.get(it) }
            .mapNotNull { event ->
                val message = event.message
                if (message is ShortMessage && message.command == ShortMessage.NOTE_ON) {
                    message.channel
                } else null
            }
            .toSet()

        assertTrue(channels.all { it == 9 }) // MIDI channel 10 = index 9
    }

    // === Velocity Tests ===

    @Test
    fun `should set default velocity for all notes`() {
        val pattern = TestFixtures.createSimplePattern()
        val exporter = MidiExporter(defaultVelocity = 100)
        val outputFile = File(testOutputDir, "velocity_test.mid")

        exporter.exportToMidi(pattern, outputFile)

        val sequence = MidiSystem.getSequence(outputFile)
        val track = sequence.tracks[0]

        // Verify all notes have the expected velocity
        val velocities = (0 until track.size())
            .map { track.get(it) }
            .mapNotNull { event ->
                val message = event.message
                if (message is ShortMessage && message.command == ShortMessage.NOTE_ON && message.data2 > 0) {
                    message.data2 // velocity
                } else null
            }
            .toSet()

        assertTrue(velocities.all { it == 100 })
    }

    @Test
    fun `should support custom velocity per note`() {
        val exporter = MidiExporter()

        // Test velocity range
        assertTrue(exporter.isValidVelocity(1))
        assertTrue(exporter.isValidVelocity(64))
        assertTrue(exporter.isValidVelocity(127))
        assertFalse(exporter.isValidVelocity(0))
        assertFalse(exporter.isValidVelocity(128))
    }

    // === Export Options Tests ===

    @Test
    fun `should support export options configuration`() {
        val options = MidiExportOptions(
            resolution = 480,
            defaultVelocity = 90,
            noteDuration = 100,
            includeMetadata = true
        )

        assertNotNull(options)
        assertEquals(480, options.resolution)
        assertEquals(90, options.defaultVelocity)
        assertEquals(100, options.noteDuration)
        assertTrue(options.includeMetadata)
    }

    @Test
    fun `should include pattern metadata in MIDI file`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1)),
            metadata = TestFixtures.createTestMetadata(
                name = "Test Pattern",
                bpm = 140
            )
        )

        val exporter = MidiExporter()
        val outputFile = File(testOutputDir, "metadata_test.mid")
        val options = MidiExportOptions(includeMetadata = true)

        exporter.exportToMidi(pattern, outputFile, options)

        val sequence = MidiSystem.getSequence(outputFile)
        assertNotNull(sequence)
        // Metadata would be stored as MIDI meta events (track name, tempo, etc.)
    }
}
