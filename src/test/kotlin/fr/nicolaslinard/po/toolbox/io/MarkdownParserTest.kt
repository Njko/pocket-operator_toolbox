package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Step
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import fr.nicolaslinard.po.toolbox.models.Pitch
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class MarkdownParserTest {

    private val testDir = File("build/test-output/markdown-parser")
    private val writer = MarkdownWriter()
    private val parser = MarkdownParser()

    @BeforeTest
    fun setup() { testDir.mkdirs() }

    @AfterTest
    fun cleanup() { testDir.listFiles()?.forEach { it.delete() } }

    @Test
    fun `parses a PO12 pattern file without a device line as PO-12`() {
        val pattern = fr.nicolaslinard.po.toolbox.TestFixtures.createSimplePattern()
        val file = writer.write(pattern, testDir)
        // Simulate an old file saved before the device field existed.
        file.writeText(file.readText().lines().filterNot { it.trim().startsWith("device:") }.joinToString("\n"))

        val parsed = parser.parse(file)
        assertIs<PO12Pattern>(parsed)
    }

    @Test
    fun `round trips a PO12 pattern`() {
        val pattern = fr.nicolaslinard.po.toolbox.TestFixtures.createComplexPattern()
        val file = writer.write(pattern, testDir)

        val parsed = assertIs<PO12Pattern>(parser.parse(file))
        assertEquals(pattern.metadata.name, parsed.metadata.name)
        assertEquals(4, parsed.voices.size)
    }

    @Test
    fun `round trips a PO14 pattern`() {
        val pattern = PO14Pattern(
            steps = mapOf(
                1 to PO14Step(Pitch.C, 2),
                5 to PO14Step(Pitch.D, 2, halfToneUp = true),
                9 to PO14Step(Pitch.G, 2)
            ),
            sound = PODevice.PO_14.getVoiceByShortName("acid")!!,
            metadata = PatternMetadata(name = "Round Trip Bass", bpm = 130, deviceModel = "PO-14"),
            number = 3
        )
        val file = writer.write(pattern, testDir)

        val parsed = assertIs<PO14Pattern>(parser.parse(file))
        assertEquals("Round Trip Bass", parsed.metadata.name)
        assertEquals(130, parsed.metadata.bpm)
        assertEquals(3, parsed.number)
        assertEquals(PODevice.PO_14.getVoiceByShortName("acid"), parsed.sound)
        assertEquals(Pitch.C, parsed.getNote(1)?.pitch)
        assertEquals(2, parsed.getNote(1)?.octave)
        assertEquals(Pitch.D, parsed.getNote(5)?.pitch)
        assertEquals(true, parsed.getNote(5)?.halfToneUp)
        assertNull(parsed.getNote(2))
    }

    @Test
    fun `PO14 metadata reports PO-14 as the device model`() {
        val pattern = PO14Pattern(
            steps = emptyMap(),
            sound = PODevice.PO_14.voices.first(),
            metadata = PatternMetadata(name = "Empty Bass", deviceModel = "PO-14")
        )
        val file = writer.write(pattern, testDir)

        val parsed = parser.parse(file)
        assertEquals("PO-14", parsed.metadata.deviceModel)
    }
}
