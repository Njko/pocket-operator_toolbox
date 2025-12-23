package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import fr.nicolaslinard.po.toolbox.analysis.PatternSimilarityAnalyzer
import fr.nicolaslinard.po.toolbox.analysis.SimilarityWeights
import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import java.io.File

class SimilarCommand : CliktCommand(name = "similar") {
    override fun help(context: Context) =
        "Find similar patterns based on rhythm, voices, and step placement"

    private val targetFile by argument(
        help = "Target pattern file to find similarities for"
    ).file(mustExist = true, canBeDir = false, mustBeReadable = true)

    private val threshold by option(
        "-t", "--threshold",
        help = "Minimum similarity threshold (0.0-1.0)"
    ).double().default(0.5)

    private val limit by option(
        "-n", "--limit",
        help = "Maximum number of results to display"
    ).int().default(10)

    private val voiceWeight by option(
        "--voice-weight",
        help = "Weight for voice similarity (0.0-1.0)"
    ).double().default(0.4)

    private val stepWeight by option(
        "--step-weight",
        help = "Weight for step similarity (0.0-1.0)"
    ).double().default(0.4)

    private val rhythmWeight by option(
        "--rhythm-weight",
        help = "Weight for rhythm similarity (0.0-1.0)"
    ).double().default(0.2)

    private val terminal = Terminal()
    private val parser = MarkdownParser()
    private val analyzer = PatternSimilarityAnalyzer()

    override fun run() {
        terminal.println((bold + cyan)("Pattern Similarity Search"))
        terminal.println()

        // Validate inputs
        if (threshold < 0.0 || threshold > 1.0) {
            terminal.println((red)("Error: Threshold must be between 0.0 and 1.0"))
            return
        }

        try {
            // Parse target pattern
            terminal.println((bold)("Target pattern: ${targetFile.name}"))
            val targetPattern = parser.parse(targetFile)
            terminal.println("  ${targetPattern.metadata.name}")
            terminal.println("  Voices: ${targetPattern.voices.size}, " +
                    "Notes: ${targetPattern.voices.values.sumOf { it.size }}")
            terminal.println()

            // Create similarity weights
            val weights = SimilarityWeights(
                voiceWeight = voiceWeight,
                stepWeight = stepWeight,
                rhythmWeight = rhythmWeight
            )

            if (!weights.isValid()) {
                terminal.println((yellow)("Warning: Weights don't sum to 1.0, using defaults"))
                terminal.println()
            }

            // Find pattern library
            val patternsDir = File("patterns")
            if (!patternsDir.exists() || !patternsDir.isDirectory) {
                terminal.println((red)("Error: patterns/ directory not found"))
                return
            }

            // Load all patterns
            terminal.println((dim)("Scanning patterns directory..."))
            val library = patternsDir.listFiles { file ->
                file.extension == "md" && file.name != "README.md" && file != targetFile
            }?.map { file ->
                try {
                    parser.parse(file).also { it }
                } catch (e: Exception) {
                    terminal.println((yellow)("Warning: Could not parse ${file.name}"))
                    null
                }
            }?.filterNotNull() ?: emptyList()

            terminal.println("Found ${library.size} patterns to compare")
            terminal.println()

            // Find similar patterns
            terminal.println((bold)("Searching for similar patterns..."))
            terminal.println("─".repeat(70))
            terminal.println()

            val results = analyzer.findSimilar(targetPattern, library, threshold, weights)
                .take(limit)

            if (results.isEmpty()) {
                terminal.println((yellow)("No similar patterns found above ${threshold * 100}% threshold"))
                terminal.println((dim)("Try lowering the threshold with --threshold"))
                return
            }

            // Display results
            displayResults(results, targetPattern)

        } catch (e: Exception) {
            terminal.println((red)("Error: ${e.message}"))
            e.printStackTrace()
        }
    }

    private fun displayResults(
        results: List<fr.nicolaslinard.po.toolbox.analysis.SimilarityResult>,
        targetPattern: fr.nicolaslinard.po.toolbox.models.PO12Pattern
    ) {
        terminal.println((bold + green)("Found ${results.size} similar pattern(s):"))
        terminal.println()

        val resultsTable = table {
            header {
                row((bold)("Rank"), (bold)("Similarity"), (bold)("Pattern"),
                    (bold)("Voices"), (bold)("Notes"), (bold)("BPM"), (bold)("Difficulty"))
            }
            body {
                results.forEachIndexed { index, result ->
                    val pattern = result.pattern
                    val similarityPercent = String.format("%.1f%%", result.similarity * 100)
                    val similarityColor = when {
                        result.similarity >= 0.8 -> green
                        result.similarity >= 0.6 -> yellow
                        else -> white
                    }

                    row(
                        (index + 1).toString(),
                        similarityColor(similarityPercent),
                        pattern.metadata.name,
                        pattern.voices.size.toString(),
                        pattern.voices.values.sumOf { it.size }.toString(),
                        pattern.metadata.bpm?.toString() ?: "?",
                        pattern.metadata.difficulty?.displayName ?: "-"
                    )
                }
            }
        }

        terminal.println(resultsTable)
        terminal.println()

        // Show detailed breakdown for top result
        if (results.isNotEmpty()) {
            val topResult = results.first()
            showDetailedBreakdown(targetPattern, topResult.pattern, topResult.similarity)
        }

        terminal.println()
        terminal.println((dim)("Tip: Use --voice-weight, --step-weight, and --rhythm-weight"))
        terminal.println((dim)("     to adjust which aspects of similarity are most important."))
    }

    private fun showDetailedBreakdown(
        target: fr.nicolaslinard.po.toolbox.models.PO12Pattern,
        similar: fr.nicolaslinard.po.toolbox.models.PO12Pattern,
        overallSimilarity: Double
    ) {
        terminal.println((bold)("Detailed breakdown for top match:"))
        terminal.println()

        val voiceSim = analyzer.calculateVoiceSimilarity(target, similar)
        val stepSim = analyzer.calculateStepSimilarity(target, similar)
        val rhythmSim = analyzer.calculateRhythmSimilarity(target, similar)

        terminal.println("  Overall similarity: ${(bold)(String.format("%.1f%%", overallSimilarity * 100))}")
        terminal.println("  ├─ Voice similarity: ${String.format("%.1f%%", voiceSim * 100)}")
        terminal.println("  ├─ Step similarity: ${String.format("%.1f%%", stepSim * 100)}")
        terminal.println("  └─ Rhythm similarity: ${String.format("%.1f%%", rhythmSim * 100)}")
        terminal.println()

        // Show common voices
        val commonVoices = target.voices.keys.intersect(similar.voices.keys)
        if (commonVoices.isNotEmpty()) {
            terminal.println("  Common voices: ${commonVoices.map { it.displayName }.joinToString(", ")}")
        }
    }
}
