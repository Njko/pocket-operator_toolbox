package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import fr.nicolaslinard.po.toolbox.io.MarkdownWriter
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.ui.GridEditor
import java.io.File

class EditCommand : CliktCommand(name = "edit") {
    override fun help(context: Context) = "Edit an existing PO-12 pattern"

    private val patternFile by argument(
        name = "file",
        help = "Path to the pattern markdown file"
    ).file(mustExist = true, canBeDir = false, mustBeReadable = true, mustBeWritable = true)

    private val terminal = Terminal()

    override fun run() {
        try {
            // Parse existing pattern
            val parser = MarkdownParser()
            val pattern = parser.parse(patternFile)

            terminal.println((bold + cyan)("=== Editing: ${pattern.metadata.name} ==="))
            terminal.println()
            terminal.println((dim)("Current pattern:"))
            terminal.println()

            // Show current voices
            pattern.voices.keys.sortedBy { it.poNumber }.forEach { voice ->
                val steps = pattern.getActiveSteps(voice)
                terminal.println("${voice.displayName} (Sound ${voice.poNumber}): ${steps.joinToString(", ")}")
            }

            terminal.println()
            terminal.println((dim)("You can now modify drum voices."))
            terminal.println()

            // Edit voices
            val voices = pattern.voices.toMutableMap()
            val gridEditor = GridEditor(terminal)

            while (true) {
                val voice = selectDrumVoice(voices.keys)
                if (voice == null) {
                    break
                }

                val currentSteps = voices[voice] ?: emptyList()
                val newSteps = gridEditor.edit(voice, currentSteps)

                if (newSteps.isEmpty()) {
                    voices.remove(voice)
                    terminal.println((yellow)("✓ Removed ${voice.displayName}"))
                } else {
                    voices[voice] = newSteps
                    terminal.println((green)("✓ Updated ${voice.displayName}: ${newSteps.joinToString(", ")}"))
                }
                terminal.println()
            }

            if (voices.isEmpty()) {
                terminal.println((yellow)("No voices in pattern. Changes not saved."))
                return
            }

            // Create updated pattern
            val updatedPattern = pattern.copy(voices = voices)

            // Write back to file
            val writer = MarkdownWriter()
            writer.write(updatedPattern, patternFile.parentFile)

            terminal.println()
            terminal.println((bold + green)("✓ Pattern updated: ${patternFile.path}"))
        } catch (e: Exception) {
            terminal.println((red)("Error editing pattern: ${e.message}"))
            throw e
        }
    }

    private fun selectDrumVoice(existingVoices: Set<PO12DrumVoice>): PO12DrumVoice? {
        terminal.println((bold)("Select a drum voice to edit (or press Enter to finish):"))
        terminal.println()

        val availableVoices = PO12DrumVoice.entries
        availableVoices.forEachIndexed { index, voice ->
            val marker = if (voice in existingVoices) (green)("●") else (dim)("○")
            terminal.println("  ${index + 1}. $marker ${voice.displayName} (Sound ${voice.poNumber})")
        }
        terminal.println()

        terminal.print((bold)("Choice (1-16, or Enter to finish): "))
        val input = readlnOrNull()?.trim()

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
