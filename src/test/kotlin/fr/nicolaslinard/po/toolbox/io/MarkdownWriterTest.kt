package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.AnyPattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Step
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import fr.nicolaslinard.po.toolbox.models.Pitch
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class MarkdownWriterTest {

    private val testOutputDir = File("build/test-output/markdown-writer")
    private val writer = MarkdownWriter()

    @BeforeTest
    fun setup() { testOutputDir.mkdirs() }

    @AfterTest
    fun cleanup() { testOutputDir.listFiles()?.forEach { it.delete() } }

    private fun samplePo14Pattern(): PO14Pattern = PO14Pattern(
        steps = mapOf(
            1 to PO14Step(Pitch.C, 2),
            5 to PO14Step(Pitch.D, 2, halfToneUp = true),
            9 to PO14Step(Pitch.G, 2)
        ),
        sound = PODevice.PO_14.getVoiceByShortName("acid")!!,
        metadata = PatternMetadata(name = "Acid Test Line", bpm = 130, deviceModel = "PO-14"),
        number = 1
    )

    @Test
    fun `writes device field in frontmatter for a PO12 pattern`() {
        val pattern = fr.nicolaslinard.po.toolbox.TestFixtures.createSimplePattern()
        val file = writer.write(pattern, testOutputDir)
        assertTrue(file.readText().contains("device: \"PO-12\""))
    }

    @Test
    fun `writes device field in frontmatter for a PO14 pattern`() {
        val file = writer.write(samplePo14Pattern(), testOutputDir)
        assertTrue(file.readText().contains("device: \"PO-14\""))
    }

    @Test
    fun `writes the selected sound name and number`() {
        val file = writer.write(samplePo14Pattern(), testOutputDir)
        assertTrue(file.readText().contains("Acid Bass (Sound 6)"))
    }

    @Test
    fun `writes a note token per active step and a rest marker per empty step`() {
        val file = writer.write(samplePo14Pattern(), testOutputDir)
        val content = file.readText()
        assertTrue(content.contains("C2"))
        assertTrue(content.contains("D#2"))
        assertTrue(content.contains("G2"))
        assertTrue(content.contains("."))
    }

    @Test
    fun `lists sharp steps in the programming instructions`() {
        val file = writer.write(samplePo14Pattern(), testOutputDir)
        assertTrue(file.readText().contains("half note up"))
    }

    @Test
    fun `dispatches AnyPattern write to the correct overload`() {
        val po14: AnyPattern = samplePo14Pattern()
        val file = writer.write(po14, testOutputDir)
        assertTrue(file.readText().contains("device: \"PO-14\""))
    }
}
