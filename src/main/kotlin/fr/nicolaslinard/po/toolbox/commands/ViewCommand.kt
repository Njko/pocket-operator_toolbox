package fr.nicolaslinard.po.toolbox.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import java.io.File

class ViewCommand : CliktCommand(name = "view") {
    override fun help(context: Context) = "Display a PO-12 pattern from a markdown file"

    private val patternFile by argument(
        name = "file",
        help = "Path to the pattern markdown file"
    ).file(mustExist = true, canBeDir = false, mustBeReadable = true)

    private val terminal = Terminal()

    override fun run() {
        try {
            val parser = MarkdownParser()
            val pattern = parser.parse(patternFile)

            displayPattern(pattern)
        } catch (e: Exception) {
            terminal.println((red)("Error reading pattern: ${e.message}"))
            throw e
        }
    }

    private fun displayPattern(pattern: PO12Pattern) {
        terminal.println()
        terminal.println((bold + cyan)("=== ${pattern.metadata.name} ==="))
        terminal.println()

        // Metadata
        pattern.metadata.description?.let {
            terminal.println((dim)(it))
            terminal.println()
        }

        displayMetadata(pattern)
        terminal.println()

        // Pattern details
        terminal.println((bold)("Pattern ${pattern.number}"))
        terminal.println()

        val sortedVoices = pattern.voices.keys.sortedBy { it.poNumber }
        sortedVoices.forEach { voice ->
            val steps = pattern.getActiveSteps(voice)
            terminal.println((bold)("${voice.displayName} (Sound ${voice.poNumber}):"))
            displayStepGrid(steps)
            terminal.println((dim)("  Steps: ${steps.joinToString(", ")}"))
            terminal.println()
        }

        // Programming instructions
        terminal.println((bold + cyan)("PO-12 Programming Instructions:"))
        terminal.println((dim)("1. Select Pattern ${pattern.number} on your PO-12"))

        var step = 2
        sortedVoices.forEach { voice ->
            val steps = pattern.getActiveSteps(voice)
            if (steps.isNotEmpty()) {
                terminal.println((dim)("${step}. For ${voice.displayName} (button ${voice.poNumber}):"))
                terminal.println((dim)("   - Press and hold button ${voice.poNumber}"))
                terminal.println((dim)("   - Tap steps: ${steps.joinToString(", ")}"))
                step++
            }
        }
        terminal.println()
    }

    private fun displayMetadata(pattern: PO12Pattern) {
        val metadata = pattern.metadata

        metadata.bpm?.let {
            terminal.print((dim)("BPM: "))
            terminal.println((bold)("$it"))
        }

        if (metadata.genre.isNotEmpty()) {
            terminal.print((dim)("Genre: "))
            terminal.println(metadata.genre.joinToString(", "))
        }

        metadata.difficulty?.let {
            terminal.print((dim)("Difficulty: "))
            terminal.println((bold)(it.displayName))
        }

        metadata.sourceAttribution?.let {
            terminal.print((dim)("Source: "))
            terminal.println(it)
        }

        metadata.author?.let {
            terminal.print((dim)("Author: "))
            terminal.println(it)
        }
    }

    private fun displayStepGrid(activeSteps: List<Int>) {
        terminal.print("  ")
        for (i in 1..16) {
            val isActive = i in activeSteps
            val stepDisplay = if (isActive) "[●]" else "[ ]"
            val styled = if (isActive) (green)(stepDisplay) else (dim)(stepDisplay)
            terminal.print(styled)
            terminal.print(" ")
        }
        terminal.println()
    }
}
