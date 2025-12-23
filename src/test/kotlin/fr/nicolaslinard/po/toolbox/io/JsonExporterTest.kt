package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.TestFixtures
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import kotlin.test.*

/**
 * TDD: RED phase - Tests for JSON export functionality
 *
 * Feature: Export patterns to JSON format for programmatic integration
 * Enables data exchange with other tools and platforms.
 */
class JsonExporterTest {

    private val testOutputDir = File("build/test-output/json")

    @BeforeTest
    fun setup() {
        testOutputDir.mkdirs()
    }

    @AfterTest
    fun cleanup() {
        testOutputDir.listFiles()?.forEach { it.delete() }
    }

    // === Basic JSON Export Tests ===

    @Test
    fun `should export pattern to valid JSON`() {
        val pattern = TestFixtures.createSimplePattern()
        val exporter = JsonExporter()
        val outputFile = File(testOutputDir, "test_pattern.json")

        exporter.export(pattern, outputFile)

        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0)

        // Verify it's valid JSON
        val json = JSONObject(outputFile.readText())
        assertNotNull(json)
    }

    @Test
    fun `should include pattern number in JSON`() {
        val pattern = PO12Pattern(
            number = 5,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)),
            metadata = TestFixtures.createTestMetadata()
        )
        val exporter = JsonExporter()
        val outputFile = File(testOutputDir, "pattern_number.json")

        exporter.export(pattern, outputFile)

        val json = JSONObject(outputFile.readText())
        assertEquals(5, json.getInt("patternNumber"))
    }

    @Test
    fun `should include metadata in JSON`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1)),
            metadata = TestFixtures.createTestMetadata(
                name = "Test Pattern",
                bpm = 120
            )
        )
        val exporter = JsonExporter()
        val outputFile = File(testOutputDir, "metadata.json")

        exporter.export(pattern, outputFile)

        val json = JSONObject(outputFile.readText())
        val metadata = json.getJSONObject("metadata")
        assertEquals("Test Pattern", metadata.getString("name"))
        assertEquals(120, metadata.getInt("bpm"))
    }

    @Test
    fun `should include voices array in JSON`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
                PO12DrumVoice.SNARE to listOf(5, 13)
            ),
            metadata = TestFixtures.createTestMetadata()
        )
        val exporter = JsonExporter()
        val outputFile = File(testOutputDir, "voices.json")

        exporter.export(pattern, outputFile)

        val json = JSONObject(outputFile.readText())
        val voices = json.getJSONArray("voices")
        assertEquals(2, voices.length())
    }

    @Test
    fun `should include voice details in JSON`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)),
            metadata = TestFixtures.createTestMetadata()
        )
        val exporter = JsonExporter()
        val outputFile = File(testOutputDir, "voice_details.json")

        exporter.export(pattern, outputFile)

        val json = JSONObject(outputFile.readText())
        val voices = json.getJSONArray("voices")
        val kickVoice = voices.getJSONObject(0)

        assertEquals("kick", kickVoice.getString("shortName"))
        assertEquals("Bass Drum", kickVoice.getString("displayName"))

        val steps = kickVoice.getJSONArray("steps")
        assertEquals(4, steps.length())
        assertEquals(1, steps.getInt(0))
    }

    // === Pretty Printing Tests ===

    @Test
    fun `should support pretty-printed JSON`() {
        val pattern = TestFixtures.createSimplePattern()
        val exporter = JsonExporter(prettyPrint = true)
        val outputFile = File(testOutputDir, "pretty.json")

        exporter.export(pattern, outputFile)

        val content = outputFile.readText()
        assertTrue(content.contains("\n")) // Has newlines
        assertTrue(content.contains("  ")) // Has indentation
    }

    @Test
    fun `should support compact JSON`() {
        val pattern = TestFixtures.createSimplePattern()
        val exporter = JsonExporter(prettyPrint = false)
        val outputFile = File(testOutputDir, "compact.json")

        exporter.export(pattern, outputFile)

        val content = outputFile.readText()
        assertFalse(content.contains("\n  ")) // No indented newlines
    }

    // === Multiple Pattern Export Tests ===

    @Test
    fun `should export multiple patterns to JSON array`() {
        val patterns = listOf(
            TestFixtures.createSimplePattern(),
            TestFixtures.createComplexPattern()
        )
        val exporter = JsonExporter()
        val outputFile = File(testOutputDir, "multiple.json")

        exporter.exportMultiple(patterns, outputFile)

        val json = JSONArray(outputFile.readText())
        assertEquals(2, json.length())
    }

    // === Empty Pattern Tests ===

    @Test
    fun `should handle empty pattern gracefully`() {
        val pattern = TestFixtures.createEmptyPattern()
        val exporter = JsonExporter()
        val outputFile = File(testOutputDir, "empty.json")

        exporter.export(pattern, outputFile)

        val json = JSONObject(outputFile.readText())
        val voices = json.getJSONArray("voices")
        assertEquals(0, voices.length())
    }

    // === Round-trip Tests ===

    @Test
    fun `should support round-trip export and import`() {
        val original = TestFixtures.createSimplePattern()
        val exporter = JsonExporter()
        val importer = JsonImporter()
        val outputFile = File(testOutputDir, "roundtrip.json")

        exporter.export(original, outputFile)
        val imported = importer.import(outputFile)

        assertEquals(original.number, imported.number)
        assertEquals(original.voices.size, imported.voices.size)
        assertEquals(original.metadata.name, imported.metadata.name)
    }
}
