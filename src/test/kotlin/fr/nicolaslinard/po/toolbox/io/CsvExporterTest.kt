package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.TestFixtures
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import java.io.File
import kotlin.test.*

/**
 * TDD: RED phase - Tests for CSV export functionality
 *
 * Feature: Export patterns to CSV format for spreadsheet analysis
 * Enables analysis in Excel, Google Sheets, and other tools.
 */
class CsvExporterTest {

    private val testOutputDir = File("build/test-output/csv")

    @BeforeTest
    fun setup() {
        testOutputDir.mkdirs()
    }

    @AfterTest
    fun cleanup() {
        testOutputDir.listFiles()?.forEach { it.delete() }
    }

    // === Basic CSV Export Tests ===

    @Test
    fun `should export pattern to valid CSV`() {
        val pattern = TestFixtures.createSimplePattern()
        val exporter = CsvExporter()
        val outputFile = File(testOutputDir, "test_pattern.csv")

        exporter.export(pattern, outputFile)

        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0)

        val lines = outputFile.readLines()
        assertTrue(lines.isNotEmpty())
    }

    @Test
    fun `should include CSV header row`() {
        val pattern = TestFixtures.createSimplePattern()
        val exporter = CsvExporter()
        val outputFile = File(testOutputDir, "header.csv")

        exporter.export(pattern, outputFile)

        val lines = outputFile.readLines()
        val header = lines.first()

        assertTrue(header.contains("Voice"))
        assertTrue(header.contains("Step"))
    }

    @Test
    fun `should export one row per note`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 5, 9, 13), // 4 notes
                PO12DrumVoice.SNARE to listOf(5, 13) // 2 notes
            ), // Total: 6 notes
            metadata = TestFixtures.createTestMetadata()
        )
        val exporter = CsvExporter()
        val outputFile = File(testOutputDir, "rows.csv")

        exporter.export(pattern, outputFile)

        val lines = outputFile.readLines()
        // Header + 6 data rows
        assertEquals(7, lines.size)
    }

    @Test
    fun `should include voice information in CSV`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1)),
            metadata = TestFixtures.createTestMetadata()
        )
        val exporter = CsvExporter()
        val outputFile = File(testOutputDir, "voice_info.csv")

        exporter.export(pattern, outputFile)

        val lines = outputFile.readLines()
        val dataRow = lines[1] // First data row after header

        assertTrue(dataRow.contains("kick") || dataRow.contains("KICK"))
        assertTrue(dataRow.contains("Bass Drum"))
    }

    @Test
    fun `should include step numbers in CSV`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)),
            metadata = TestFixtures.createTestMetadata()
        )
        val exporter = CsvExporter()
        val outputFile = File(testOutputDir, "steps.csv")

        exporter.export(pattern, outputFile)

        val lines = outputFile.readLines()
        val steps = lines.drop(1).map { line ->
            line.split(",").last().trim().toInt()
        }

        assertTrue(steps.containsAll(listOf(1, 5, 9, 13)))
    }

    // === Grid Format Tests ===

    @Test
    fun `should export pattern in grid format`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
                PO12DrumVoice.SNARE to listOf(5, 13)
            ),
            metadata = TestFixtures.createTestMetadata()
        )
        val exporter = CsvExporter()
        val outputFile = File(testOutputDir, "grid.csv")

        exporter.exportGrid(pattern, outputFile)

        val lines = outputFile.readLines()
        val header = lines.first()

        // Header should have Voice + 16 step columns
        assertTrue(header.split(",").size >= 17)
    }

    @Test
    fun `should mark active steps in grid format`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5)),
            metadata = TestFixtures.createTestMetadata()
        )
        val exporter = CsvExporter()
        val outputFile = File(testOutputDir, "grid_marks.csv")

        exporter.exportGrid(pattern, outputFile)

        val lines = outputFile.readLines()
        val kickRow = lines[1] // First data row

        // Should have marks for steps 1 and 5
        val cells = kickRow.split(",")
        assertEquals("X", cells[1].trim()) // Step 1 (column index 1)
        assertEquals("X", cells[5].trim()) // Step 5 (column index 5)
    }

    // === Multiple Pattern Export Tests ===

    @Test
    fun `should export multiple patterns to single CSV`() {
        val patterns = listOf(
            TestFixtures.createSimplePattern(),
            TestFixtures.createComplexPattern()
        )
        val exporter = CsvExporter()
        val outputFile = File(testOutputDir, "multiple.csv")

        exporter.exportMultiple(patterns, outputFile)

        val lines = outputFile.readLines()
        assertTrue(lines.size > 2) // Header + multiple patterns
    }

    @Test
    fun `should include pattern number in multi-pattern export`() {
        val patterns = listOf(
            PO12Pattern(number = 1, voices = mapOf(PO12DrumVoice.KICK to listOf(1)), metadata = TestFixtures.createTestMetadata()),
            PO12Pattern(number = 2, voices = mapOf(PO12DrumVoice.SNARE to listOf(5)), metadata = TestFixtures.createTestMetadata())
        )
        val exporter = CsvExporter()
        val outputFile = File(testOutputDir, "pattern_numbers.csv")

        exporter.exportMultiple(patterns, outputFile)

        val lines = outputFile.readLines()
        val header = lines.first()
        assertTrue(header.contains("Pattern"))
    }

    // === Metadata Export Tests ===

    @Test
    fun `should support metadata export option`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1)),
            metadata = TestFixtures.createTestMetadata(
                name = "Test Pattern",
                bpm = 120
            )
        )
        val exporter = CsvExporter(includeMetadata = true)
        val outputFile = File(testOutputDir, "with_metadata.csv")

        exporter.export(pattern, outputFile)

        val content = outputFile.readText()
        assertTrue(content.contains("Test Pattern"))
        assertTrue(content.contains("120"))
    }

    // === Empty Pattern Tests ===

    @Test
    fun `should handle empty pattern gracefully`() {
        val pattern = TestFixtures.createEmptyPattern()
        val exporter = CsvExporter()
        val outputFile = File(testOutputDir, "empty.csv")

        exporter.export(pattern, outputFile)

        val lines = outputFile.readLines()
        // Should have header but no data rows
        assertEquals(1, lines.size)
    }

    // === CSV Escaping Tests ===

    @Test
    fun `should properly escape CSV values`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1)),
            metadata = TestFixtures.createTestMetadata(
                name = "Pattern, with comma"
            )
        )
        val exporter = CsvExporter(includeMetadata = true)
        val outputFile = File(testOutputDir, "escaping.csv")

        exporter.export(pattern, outputFile)

        val content = outputFile.readText()
        // Commas in values should be escaped with quotes
        assertTrue(content.contains("\"Pattern, with comma\""))
    }
}
