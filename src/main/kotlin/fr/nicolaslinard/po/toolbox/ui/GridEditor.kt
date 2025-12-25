package fr.nicolaslinard.po.toolbox.ui

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice

class GridEditor(private val terminal: Terminal) {

    /**
     * Opens a text-based grid editor for a specific drum voice.
     * Returns the list of active step numbers (1-16), or empty list if cancelled.
     *
     * @param drumVoice The drum voice to edit
     * @param initialSteps Initial steps for this voice
     * @param contextVoices Other voices in the pattern for context display
     */
    fun edit(
        drumVoice: PO12DrumVoice,
        initialSteps: List<Int> = emptyList(),
        contextVoices: Map<PO12DrumVoice, List<Int>> = emptyMap()
    ): List<Int> {
        terminal.println((bold + cyan)("=== ${drumVoice.displayName} (Sound ${drumVoice.poNumber}) ==="))
        terminal.println()

        // Show current pattern
        renderGrid(initialSteps)
        terminal.println()

        terminal.println((dim)("Enter step numbers (1-16) separated by spaces or commas"))
        terminal.println((dim)("Example: 1 5 9 13  or  1,5,9,13"))
        terminal.println((dim)("Press Enter with no input to keep current pattern, or type 'clear' to remove all steps"))
        terminal.print((bold)("Steps: "))

        val input = readlnOrNull()?.trim() ?: return initialSteps

        // Handle special commands
        when {
            input.isEmpty() -> return initialSteps
            input.equals("clear", ignoreCase = true) -> return emptyList()
            input.equals("cancel", ignoreCase = true) -> {
                terminal.println((yellow)("Cancelled"))
                return initialSteps
            }
        }

        // Parse step numbers
        val steps = parseSteps(input)

        if (steps == null) {
            terminal.println((red)("Invalid input. Please enter numbers between 1 and 16."))
            return edit(drumVoice, initialSteps)
        }

        // Show result
        terminal.println()
        renderGrid(steps)
        terminal.println()

        return steps
    }

    private fun renderGrid(activeSteps: List<Int>) {
        // Step numbers
        terminal.print("Step:  ")
        for (i in 1..16) {
            terminal.print(String.format("%2d  ", i))
        }
        terminal.println()

        // Grid
        terminal.print("       ")
        for (i in 1..16) {
            val isActive = i in activeSteps
            val stepDisplay = if (isActive) "[●]" else "[ ]"
            val styled = if (isActive) (green)(stepDisplay) else (dim)(stepDisplay)
            terminal.print(styled)
            terminal.print(" ")
        }
        terminal.println()
    }

    private fun parseSteps(input: String): List<Int>? {
        // Split by spaces, commas, or both
        val parts = input.split(Regex("[,\\s]+"))
            .filter { it.isNotBlank() }

        val steps = mutableListOf<Int>()

        for (part in parts) {
            val num = part.toIntOrNull() ?: return null
            if (num !in 1..16) return null
            steps.add(num)
        }

        return steps.distinct().sorted()
    }
}
