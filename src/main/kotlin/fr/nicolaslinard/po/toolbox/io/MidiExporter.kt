package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import java.io.File
import javax.sound.midi.*

/**
 * Exports PO-12 patterns to MIDI format for DAW integration.
 * Converts drum patterns to standard MIDI files with proper timing and note mapping.
 */
class MidiExporter(
    private val defaultVelocity: Int = 100
) {
    private val noteMapper = MidiNoteMapper()

    companion object {
        private const val DRUM_CHANNEL = 9 // MIDI channel 10 (index 9) for drums
        private const val MICROSECONDS_PER_MINUTE = 60_000_000
        private const val DEFAULT_RESOLUTION = 96 // Pulses Per Quarter note (PPQ)
        private const val DEFAULT_NOTE_DURATION = 96 // Duration in ticks (1 quarter note)
    }

    /**
     * Export a single pattern to a MIDI file.
     */
    fun exportToMidi(
        pattern: PO12Pattern,
        outputFile: File,
        options: MidiExportOptions = MidiExportOptions()
    ) {
        val sequence = createSequence(listOf(pattern), options)
        saveMidiFile(sequence, outputFile)
    }

    /**
     * Export multiple chained patterns to a MIDI file.
     */
    fun exportPatternsToMidi(
        patterns: List<PO12Pattern>,
        outputFile: File,
        options: MidiExportOptions = MidiExportOptions()
    ) {
        val sequence = createSequence(patterns, options)
        saveMidiFile(sequence, outputFile)
    }

    /**
     * Create a MIDI sequence from patterns.
     */
    private fun createSequence(
        patterns: List<PO12Pattern>,
        options: MidiExportOptions
    ): Sequence {
        val sequence = Sequence(Sequence.PPQ, options.resolution)
        val track = sequence.createTrack()

        // Add tempo meta event
        if (patterns.isNotEmpty()) {
            val bpm = patterns.first().metadata.bpm ?: 120 // Default to 120 BPM
            addTempoEvent(track, 0, bpm)

            // Add track name if metadata is enabled
            if (options.includeMetadata) {
                addTrackName(track, patterns.first().metadata.name)
            }
        }

        // Convert each pattern to MIDI events
        var currentTick = 0L
        patterns.forEach { pattern ->
            currentTick = addPatternToTrack(track, pattern, currentTick, options)
        }

        // Add end of track marker
        addEndOfTrack(track, currentTick)

        return sequence
    }

    /**
     * Add a single pattern to the track starting at the given tick position.
     * Returns the tick position after this pattern.
     */
    private fun addPatternToTrack(
        track: Track,
        pattern: PO12Pattern,
        startTick: Long,
        options: MidiExportOptions
    ): Long {
        val ticksPerStep = calculateTicksPerStep(options.resolution)

        // Process each voice
        pattern.voices.forEach { (voice, steps) ->
            val midiNote = noteMapper.getMidiNote(voice)

            steps.forEach { step ->
                // Convert step (1-16) to tick position
                val stepTick = startTick + ((step - 1) * ticksPerStep)

                // Add NOTE_ON event
                addNoteEvent(
                    track,
                    stepTick,
                    ShortMessage.NOTE_ON,
                    midiNote,
                    options.defaultVelocity
                )

                // Add NOTE_OFF event
                addNoteEvent(
                    track,
                    stepTick + options.noteDuration,
                    ShortMessage.NOTE_OFF,
                    midiNote,
                    0
                )
            }
        }

        // Return the tick position after this pattern (16 steps)
        return startTick + (16 * ticksPerStep)
    }

    /**
     * Calculate ticks per step (16th note).
     * At 4/4 time: 1 bar = 4 beats = 16 steps
     * So each step = 1/4 beat = 16th note
     */
    fun calculateTicksPerStep(resolution: Int): Int {
        // Resolution is ticks per quarter note
        // 16th note = quarter note / 4
        return resolution / 4
    }

    /**
     * Convert BPM to MIDI tempo (microseconds per quarter note).
     */
    fun calculateMidiTempo(bpm: Int): Int {
        return MICROSECONDS_PER_MINUTE / bpm
    }

    /**
     * Validate velocity value (1-127).
     */
    fun isValidVelocity(velocity: Int): Boolean {
        return velocity in 1..127
    }

    /**
     * Add a note event to the track.
     */
    private fun addNoteEvent(
        track: Track,
        tick: Long,
        command: Int,
        note: Int,
        velocity: Int
    ) {
        val message = ShortMessage()
        message.setMessage(command, DRUM_CHANNEL, note, velocity)
        track.add(MidiEvent(message, tick))
    }

    /**
     * Add tempo meta event to track.
     */
    private fun addTempoEvent(track: Track, tick: Long, bpm: Int) {
        val tempo = calculateMidiTempo(bpm)
        val tempoBytes = byteArrayOf(
            ((tempo shr 16) and 0xFF).toByte(),
            ((tempo shr 8) and 0xFF).toByte(),
            (tempo and 0xFF).toByte()
        )

        val metaMessage = MetaMessage()
        metaMessage.setMessage(0x51, tempoBytes, 3) // 0x51 = Set Tempo
        track.add(MidiEvent(metaMessage, tick))
    }

    /**
     * Add track name meta event.
     */
    private fun addTrackName(track: Track, name: String) {
        val metaMessage = MetaMessage()
        val nameBytes = name.toByteArray()
        metaMessage.setMessage(0x03, nameBytes, nameBytes.size) // 0x03 = Track Name
        track.add(MidiEvent(metaMessage, 0))
    }

    /**
     * Add end of track marker.
     */
    private fun addEndOfTrack(track: Track, tick: Long) {
        val metaMessage = MetaMessage()
        metaMessage.setMessage(0x2F, byteArrayOf(), 0) // 0x2F = End of Track
        track.add(MidiEvent(metaMessage, tick))
    }

    /**
     * Save MIDI sequence to file.
     */
    private fun saveMidiFile(sequence: Sequence, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        MidiSystem.write(sequence, 1, outputFile) // Type 1 = multi-track
    }
}

/**
 * Configuration options for MIDI export.
 */
data class MidiExportOptions(
    val resolution: Int = 96,           // Pulses per quarter note (PPQ)
    val defaultVelocity: Int = 100,     // Default note velocity (1-127)
    val noteDuration: Int = 96,         // Note duration in ticks
    val includeMetadata: Boolean = true // Include pattern metadata in MIDI file
)
