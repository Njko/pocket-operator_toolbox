package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.table.table
import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import java.io.File

class ListCommand : CliktCommand(name = "list") {
    override fun help(context: Context) = "List all PO-12 patterns with optional filtering"

    private val directory by option(
        "--directory", "-d",
        help = "Directory containing pattern files"
    ).file(mustExist = true, canBeFile = false, mustBeReadable = true)
        .default(File("patterns"))

    private val genre by option(
        "--genre", "-g",
        help = "Filter by genre (case-insensitive partial match)"
    )

    private val difficulty by option(
        "--difficulty",
        help = "Filter by difficulty (beginner/intermediate/advanced)"
    )

    private val minBpm by option(
        "--min-bpm",
        help = "Minimum BPM"
    )

    private val maxBpm by option(
        "--max-bpm",
        help = "Maximum BPM"
    )

    private val terminal = Terminal()

    override fun run() {
        if (!directory.exists() || !directory.isDirectory) {
            terminal.println((red)("Error: Directory not found: ${directory.path}"))
            return
        }

        val patterns = loadPatterns()
        val filtered = applyFilters(patterns)

        if (filtered.isEmpty()) {
            terminal.println((yellow)("No patterns found matching criteria."))
            return
        }

        displayPatterns(filtered)
    }

    private fun loadPatterns(): List<Pair<File, PO12Pattern>> {
        val parser = MarkdownParser()
        val patterns = mutableListOf<Pair<File, PO12Pattern>>()

        directory.listFiles()?.forEach { file ->
            if (file.extension == "md" && file.name != "README.md") {
                try {
                    val pattern = parser.parse(file)
                    patterns.add(Pair(file, pattern))
                } catch (e: Exception) {
                    terminal.println((dim)("⚠ Skipped ${file.name}: ${e.message}"))
                }
            }
        }

        return patterns
    }

    private fun applyFilters(patterns: List<Pair<File, PO12Pattern>>): List<Pair<File, PO12Pattern>> {
        return patterns.filter { (_, pattern) ->
            // Genre filter
            if (genre != null) {
                val hasGenre = pattern.metadata.genre.any {
                    it.contains(genre!!, ignoreCase = true)
                }
                if (!hasGenre) return@filter false
            }

            // Difficulty filter
            if (difficulty != null) {
                val difficultyEnum = Difficulty.fromString(difficulty!!)
                if (pattern.metadata.difficulty != difficultyEnum) {
                    return@filter false
                }
            }

            // BPM filters
            if (minBpm != null || maxBpm != null) {
                val bpm = pattern.metadata.bpm ?: return@filter false
                if (minBpm != null && bpm < minBpm!!.toInt()) return@filter false
                if (maxBpm != null && bpm > maxBpm!!.toInt()) return@filter false
            }

            true
        }
    }

    private fun displayPatterns(patterns: List<Pair<File, PO12Pattern>>) {
        terminal.println()
        terminal.println((bold + cyan)("=== Pattern Library (${patterns.size} patterns) ==="))
        terminal.println()

        // Create table
        terminal.println(table {
            header {
                row((bold)("Name"), (bold)("Pattern #"), (bold)("BPM"), (bold)("Difficulty"), (bold)("Genres"), (bold)("Voices"))
            }
            body {
                patterns.sortedBy { it.second.metadata.name }.forEach { (file, pattern) ->
                    val metadata = pattern.metadata

                    row(
                        (cyan)(metadata.name),
                        pattern.number.toString(),
                        metadata.bpm?.toString() ?: "-",
                        when (metadata.difficulty) {
                            Difficulty.BEGINNER -> (green)(metadata.difficulty.displayName)
                            Difficulty.INTERMEDIATE -> (yellow)(metadata.difficulty?.displayName ?: "-")
                            Difficulty.ADVANCED -> (red)(metadata.difficulty?.displayName ?: "-")
                            null -> "-"
                        },
                        metadata.genre.take(2).joinToString(", ").let {
                            if (metadata.genre.size > 2) "$it..." else it
                        }.let { if (it.isBlank()) "-" else it },
                        pattern.voices.size.toString()
                    )
                }
            }
        })

        terminal.println()
        terminal.println((dim)("Use 'po-toolbox view <file>' to see pattern details"))
        terminal.println()
    }
}
