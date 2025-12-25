package fr.nicolaslinard.po.toolbox.ui

import com.github.ajalt.mordant.terminal.Terminal
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PatternEditHistory

/**
 * GREEN Phase - Minimal implementation to pass tests
 *
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

        // Main editing loop
        while (true) {
            // Read next key
            val key = inputReader.readKey() ?: continue

            when (key) {
                is Key.ArrowLeft -> {
                    cursorPosition = if (cursorPosition == 1) STEP_COUNT else cursorPosition - 1
                }
                is Key.ArrowRight -> {
                    cursorPosition = if (cursorPosition == STEP_COUNT) 1 else cursorPosition + 1
                }
                is Key.Space -> {
                    // Toggle current step
                    if (cursorPosition in activeSteps) {
                        activeSteps.remove(cursorPosition)
                    } else {
                        activeSteps.add(cursorPosition)
                    }
                }
                is Key.Enter -> {
                    // Complete editing
                    return activeSteps.sorted()
                }
                is Key.Escape -> {
                    // Cancel editing - return original steps
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
}
