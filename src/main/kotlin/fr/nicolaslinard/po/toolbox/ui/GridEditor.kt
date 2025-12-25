package fr.nicolaslinard.po.toolbox.ui

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PatternEditHistory

/**
 * Editing mode for grid editor
 */
enum class EditMode {
    TEXT,        // Current text-based input (default)
    INTERACTIVE  // New arrow-key navigation mode
}

class GridEditor(private val terminal: Terminal) {

    /**
     * Opens a grid editor for a specific drum voice.
     * Returns the list of active step numbers (1-16), or empty list if cancelled.
     *
     * @param drumVoice The drum voice to edit
     * @param initialSteps Initial steps for this voice
     * @param contextVoices Other voices in the pattern for context display
     * @param mode Editing mode (TEXT or INTERACTIVE)
     * @param history Optional pattern edit history for undo/redo support
     */
    fun edit(
        drumVoice: PO12DrumVoice,
        initialSteps: List<Int> = emptyList(),
        contextVoices: Map<PO12DrumVoice, List<Int>> = emptyMap(),
        mode: EditMode = EditMode.TEXT,
        history: PatternEditHistory? = null
    ): List<Int> {
        return when (mode) {
            EditMode.TEXT -> editText(drumVoice, initialSteps, contextVoices)
            EditMode.INTERACTIVE -> {
                // Try JLine3 keyboard reader first, fallback to Mordant stub
                val inputReader = createKeyboardReader()
                if (inputReader.isInteractiveModeSupported()) {
                    // Use interactive editor with proper resource cleanup
                    try {
                        val interactiveEditor = InteractiveGridEditor(terminal, inputReader)
                        interactiveEditor.editInteractive(drumVoice, initialSteps, contextVoices, history)
                    } finally {
                        // CRITICAL: Close the keyboard reader to restore terminal to normal mode
                        // This ensures the next voice selection menu works properly
                        if (inputReader is java.io.Closeable) {
                            inputReader.close()
                        }
                    }
                } else {
                    // Fallback to text mode
                    terminal.println((yellow)("Interactive mode not supported, using text mode"))
                    editText(drumVoice, initialSteps, contextVoices)
                }
            }
        }
    }

    /**
     * Create a keyboard reader with JLine3 fallback.
     *
     * Tries to create JLine3KeyboardReader first for full interactive support.
     * Falls back to MordantKeyboardReader if JLine3 unavailable.
     */
    private fun createKeyboardReader(): KeyboardInputReader {
        return try {
            JLine3KeyboardReader()
        } catch (e: Exception) {
            terminal.println((yellow)("JLine3 not available: ${e.message}"))
            terminal.println((dim)("Falling back to stub keyboard reader"))
            MordantKeyboardReader()
        }
    }

    /**
     * Text-based editing (original implementation)
     */
    private fun editText(
        drumVoice: PO12DrumVoice,
        initialSteps: List<Int>,
        contextVoices: Map<PO12DrumVoice, List<Int>>
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
