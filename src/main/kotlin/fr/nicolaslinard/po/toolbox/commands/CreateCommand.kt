package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.io.MarkdownWriter
import fr.nicolaslinard.po.toolbox.models.*
import fr.nicolaslinard.po.toolbox.ui.GridEditor
import fr.nicolaslinard.po.toolbox.ui.MultiVoiceRenderer
import java.io.File
import java.time.LocalDate

class CreateCommand : CliktCommand(name = "create") {
    override fun help(context: Context) = "Create a new PO-12 pattern interactively"

    private val outputPath by option(
        "--output", "-o",
        help = "Output directory for pattern files"
    ).default("patterns")

    private val patternNumber by option(
        "--pattern-number", "-p",
        help = "PO-12 pattern number (1-16)"
    ).int().default(1)

    private val terminal = Terminal()
    private val multiVoiceRenderer = MultiVoiceRenderer(terminal)

    override fun run() {
        terminal.println((bold + cyan)("=== PO-12 Pattern Creator ==="))
        terminal.println()

        // Gather metadata
        val metadata = gatherMetadata()

        terminal.println()
        terminal.println((bold)("Now let's program the drum voices."))
        terminal.println((dim)("You'll enter the steps for each drum sound you want to use."))
        terminal.println()

        // Collect pattern voices
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()
        val gridEditor = GridEditor(terminal)

        while (true) {
            // Show current pattern state (Phase 6.1: multi-voice preview)
            if (voices.isNotEmpty()) {
                multiVoiceRenderer.renderCompactGrid(voices)
            }

            // Ask which voice to program
            val voice = selectDrumVoice(voices.keys)
            if (voice == null) {
                // User is done
                break
            }

            // Edit the grid for this voice (Phase 6.1: pass context voices)
            val steps = gridEditor.edit(voice, voices[voice] ?: emptyList(), voices)

            if (steps.isNotEmpty()) {
                voices[voice] = steps
                terminal.println((green)("✓ ${voice.displayName}: ${steps.joinToString(", ")}"))
            } else {
                // Remove voice if all steps were cleared
                voices.remove(voice)
            }

            terminal.println()
        }

        if (voices.isEmpty()) {
            terminal.println((yellow)("No voices programmed. Pattern not saved."))
            return
        }

        // Create pattern
        val pattern = PO12Pattern(
            voices = voices,
            metadata = metadata,
            number = patternNumber
        )

        // Write to markdown
        val writer = MarkdownWriter()
        val outputDir = File(outputPath)
        val file = writer.write(pattern, outputDir)

        terminal.println()
        terminal.println((bold + green)("✓ Pattern saved to: ${file.path}"))
        terminal.println((dim)("You can view it on GitHub or edit it with: po-toolbox view ${file.path}"))
    }

    private fun prompt(message: String, validator: (String) -> Boolean = { true }): String? {
        while (true) {
            terminal.print((bold)(message) + ": ")
            val input = readlnOrNull() ?: return null
            if (validator(input)) {
                return input
            }
        }
    }

    private fun gatherMetadata(): PatternMetadata {
        val name = prompt("Pattern name") { it.isNotBlank() }
            ?: error("Pattern name is required")

        val description = prompt("Description (optional)") { true }

        val bpmString = prompt("BPM (optional)") { input ->
            input.isBlank() || input.toIntOrNull()?.let { it in 60..300 } == true
        }
        val bpm = bpmString?.toIntOrNull()

        val genreInput = prompt("Genre(s) (comma-separated, optional)") { true }
        val genres = genreInput?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()

        val difficultyInput = prompt("Difficulty (beginner/intermediate/advanced, optional)") { true }
        val difficulty = difficultyInput?.let { Difficulty.fromString(it) }

        val source = prompt("Source attribution (optional)") { true }

        val author = prompt("Author") { true }

        return PatternMetadata(
            name = name,
            description = description?.takeIf { it.isNotBlank() },
            bpm = bpm,
            genre = genres,
            difficulty = difficulty,
            sourceAttribution = source?.takeIf { it.isNotBlank() },
            author = author?.takeIf { it.isNotBlank() },
            dateCreated = LocalDate.now()
        )
    }

    private fun selectDrumVoice(existingVoices: Set<PO12DrumVoice>): PO12DrumVoice? {
        terminal.println((bold)("Select a drum voice to program (or press Enter to finish):"))
        terminal.println()

        val availableVoices = PO12DrumVoice.entries
        availableVoices.forEachIndexed { index, voice ->
            val marker = if (voice in existingVoices) (green)("●") else (dim)("○")
            terminal.println("  ${index + 1}. $marker ${voice.displayName} (Sound ${voice.poNumber})")
        }
        terminal.println()

        val input = prompt("Choice (1-16, or Enter to finish)") { true }

        if (input.isNullOrBlank()) {
            return null
        }

        val choice = input.toIntOrNull()
        return if (choice != null && choice in 1..16) {
            availableVoices[choice - 1]
        } else {
            terminal.println((red)("Invalid choice. Please enter a number between 1 and 16."))
            selectDrumVoice(existingVoices)
        }
    }
}
