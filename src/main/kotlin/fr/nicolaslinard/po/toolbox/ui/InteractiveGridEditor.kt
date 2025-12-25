package fr.nicolaslinard.po.toolbox.ui

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PatternEditHistory

/**
 * Interactive grid editor with arrow key navigation and spacebar toggling.
 * Provides visual cursor and real-time step preview during editing.
 */
class InteractiveGridEditor(
    private val terminal: Terminal,
    private val inputReader: KeyboardInputReader
) {
    companion object {
        private const val STEP_COUNT = 16
    }

    /**
     * Edit drum voice steps interactively with arrow key navigation.
     *
     * @param drumVoice The drum voice being edited
     * @param initialSteps Initial active steps
     * @param contextVoices Other voices for context display
     * @param history Optional edit history for undo/redo support
     * @return List of active step numbers (1-16)
     */
    fun editInteractive(
        drumVoice: PO12DrumVoice,
        initialSteps: List<Int> = emptyList(),
        contextVoices: Map<PO12DrumVoice, List<Int>> = emptyMap(),
        history: PatternEditHistory? = null
    ): List<Int> {
        var cursorPosition = 1 // 1-based step number (1-16)
        val activeSteps = initialSteps.toMutableSet()
        val originalSteps = initialSteps.toList() // For cancel

        // Display initial grid
        renderGridWithCursor(drumVoice, activeSteps, cursorPosition, contextVoices)

        // Main editing loop
        while (true) {
            // Read next key (blocking with small timeout)
            val key = inputReader.readKey()

            if (key == null) {
                // No key available, wait a bit to avoid busy loop
                Thread.sleep(50)
                continue
            }

            when (key) {
                is Key.ArrowLeft -> {
                    cursorPosition = if (cursorPosition == 1) STEP_COUNT else cursorPosition - 1
                    renderGridWithCursor(drumVoice, activeSteps, cursorPosition, contextVoices)
                }
                is Key.ArrowRight -> {
                    cursorPosition = if (cursorPosition == STEP_COUNT) 1 else cursorPosition + 1
                    renderGridWithCursor(drumVoice, activeSteps, cursorPosition, contextVoices)
                }
                is Key.Space -> {
                    // Toggle current step
                    if (cursorPosition in activeSteps) {
                        activeSteps.remove(cursorPosition)
                    } else {
                        activeSteps.add(cursorPosition)
                    }
                    renderGridWithCursor(drumVoice, activeSteps, cursorPosition, contextVoices)
                }
                is Key.Enter -> {
                    // Complete editing
                    terminal.println()
                    terminal.println((green)("✓ Pattern saved"))
                    terminal.println()
                    return activeSteps.sorted()
                }
                is Key.Escape -> {
                    // Cancel editing - return original steps
                    terminal.println()
                    terminal.println((yellow)("✗ Cancelled"))
                    terminal.println()
                    return originalSteps
                }
                is Key.CtrlZ -> {
                    // Undo if history available
                    history?.let {
                        if (it.canUndo()) {
                            it.undo()
                            // Note: Undo affects pattern-level state
                            // The pattern state is managed outside this editor
                        }
                    }
                }
                is Key.CtrlY -> {
                    // Redo if history available
                    history?.let {
                        if (it.canRedo()) {
                            it.redo()
                            // Note: Redo affects pattern-level state
                            // The pattern state is managed outside this editor
                        }
                    }
                }
                is Key.Character -> {
                    // Ignore character keys
                }
            }
        }
    }

    /**
     * Render the grid with visual cursor highlighting the current position.
     */
    private fun renderGridWithCursor(
        drumVoice: PO12DrumVoice,
        activeSteps: Set<Int>,
        cursorPosition: Int,
        contextVoices: Map<PO12DrumVoice, List<Int>>
    ) {
        // Clear screen (move cursor to top)
        terminal.print("\u001b[2J\u001b[H")

        // Header
        terminal.println((bold + cyan)("=== Interactive Editor: ${drumVoice.displayName} (Sound ${drumVoice.poNumber}) ==="))
        terminal.println()

        // Show context voices if any
        if (contextVoices.isNotEmpty()) {
            terminal.println((dim)("Context (other voices):"))
            contextVoices.forEach { (voice, steps) ->
                if (steps.isNotEmpty()) {
                    terminal.println((dim)("  ${voice.displayName}: ${steps.joinToString(", ")}"))
                }
            }
            terminal.println()
        }

        // Step numbers
        terminal.print("Step:  ")
        for (i in 1..STEP_COUNT) {
            val stepText = String.format("%2d  ", i)
            if (i == cursorPosition) {
                terminal.print((bold + yellow)(stepText))
            } else {
                terminal.print(stepText)
            }
        }
        terminal.println()

        // Grid with cursor
        terminal.print("       ")
        for (i in 1..STEP_COUNT) {
            val isActive = i in activeSteps
            val isCursor = i == cursorPosition

            val stepDisplay = if (isActive) "[●]" else "[ ]"

            val styled = when {
                isCursor && isActive -> (bold + yellow)(stepDisplay)
                isCursor -> (bold + yellow)(stepDisplay)
                isActive -> (green)(stepDisplay)
                else -> (dim)(stepDisplay)
            }

            terminal.print(styled)
            terminal.print(" ")
        }
        terminal.println()
        terminal.println()

        // Instructions
        terminal.println((dim)("Controls:"))
        terminal.println((dim)("  ← →   : Move cursor"))
        terminal.println((dim)("  Space : Toggle step"))
        terminal.println((dim)("  Enter : Save"))
        terminal.println((dim)("  Esc   : Cancel"))
    }
}
