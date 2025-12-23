package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import java.io.File

/**
 * Exports PO-12 patterns to CSV format for spreadsheet analysis.
 * CSV output enables analysis in Excel, Google Sheets, and other tools.
 */
class CsvExporter(
    private val includeMetadata: Boolean = false
) {

    /**
     * Export a single pattern to CSV file (list format: one row per note).
     */
    fun export(pattern: PO12Pattern, outputFile: File) {
        outputFile.parentFile?.mkdirs()

        val lines = mutableListOf<String>()

        // Header
        if (includeMetadata) {
            lines.add("Pattern,Voice Short Name,Voice Display Name,Step,Name,BPM")
        } else {
            lines.add("Voice Short Name,Voice Display Name,Step")
        }

        // Data rows
        pattern.voices.forEach { (voice, steps) ->
            steps.forEach { step ->
                val row = if (includeMetadata) {
                    listOf(
                        pattern.number.toString(),
                        voice.shortName,
                        voice.displayName,
                        step.toString(),
                        escapeCsv(pattern.metadata.name),
                        pattern.metadata.bpm?.toString() ?: ""
                    )
                } else {
                    listOf(
                        voice.shortName,
                        voice.displayName,
                        step.toString()
                    )
                }
                lines.add(row.joinToString(","))
            }
        }

        outputFile.writeText(lines.joinToString("\n"))
    }

    /**
     * Export pattern in grid format (voices as rows, steps as columns).
     */
    fun exportGrid(pattern: PO12Pattern, outputFile: File) {
        outputFile.parentFile?.mkdirs()

        val lines = mutableListOf<String>()

        // Header: Voice + Step 1-16
        val header = mutableListOf("Voice")
        header.addAll((1..16).map { "Step $it" })
        lines.add(header.joinToString(","))

        // Data rows: one per voice
        pattern.voices.forEach { (voice, steps) ->
            val row = mutableListOf(voice.displayName)
            val stepSet = steps.toSet()

            // Add X for active steps, empty for inactive
            for (step in 1..16) {
                row.add(if (step in stepSet) "X" else "")
            }

            lines.add(row.joinToString(","))
        }

        outputFile.writeText(lines.joinToString("\n"))
    }

    /**
     * Export multiple patterns to single CSV file.
     */
    fun exportMultiple(patterns: List<PO12Pattern>, outputFile: File) {
        outputFile.parentFile?.mkdirs()

        val lines = mutableListOf<String>()

        // Header
        lines.add("Pattern,Voice Short Name,Voice Display Name,Step")

        // Data rows
        patterns.forEach { pattern ->
            pattern.voices.forEach { (voice, steps) ->
                steps.forEach { step ->
                    val row = listOf(
                        pattern.number.toString(),
                        voice.shortName,
                        voice.displayName,
                        step.toString()
                    )
                    lines.add(row.joinToString(","))
                }
            }
        }

        outputFile.writeText(lines.joinToString("\n"))
    }

    /**
     * Escape CSV values (wrap in quotes if contains comma, quote, or newline).
     */
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
