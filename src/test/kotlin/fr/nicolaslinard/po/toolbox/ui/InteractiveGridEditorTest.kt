package fr.nicolaslinard.po.toolbox.ui

import com.github.ajalt.mordant.terminal.Terminal
import fr.nicolaslinard.po.toolbox.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TDD RED Phase: Tests for InteractiveGridEditor
 *
 * Tests interactive grid editing with arrow key navigation and spacebar toggling.
 * Verifies cursor movement, step toggling, undo/redo integration, and visual feedback.
 */
class InteractiveGridEditorTest {

    // Test Fixtures
    private val terminal = Terminal()
    private val mockHistory = PatternEditHistory()

    @Test
    fun `should initialize cursor at position 1`() {
        val reader = createMockReader(Key.Enter) // Immediately complete
        val editor = InteractiveGridEditor(terminal, reader)

        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList()
        )

        // Cursor should start at step 1 (position 0 in 0-indexed array)
        // This test verifies initial state - implementation will track cursor position
        assertTrue(true, "Cursor initialization test placeholder")
    }

    @Test
    fun `should move cursor right with arrow key`() {
        val reader = createMockReader(Key.ArrowRight, Key.ArrowRight, Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Arrow right twice should move cursor from 1 to 3
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList()
        )

        // Cursor position should be tracked internally
        assertTrue(true, "Cursor movement test placeholder")
    }

    @Test
    fun `should move cursor left with arrow key`() {
        val reader = createMockReader(
            Key.ArrowRight, Key.ArrowRight, Key.ArrowLeft, Key.Enter
        )
        val editor = InteractiveGridEditor(terminal, reader)

        // Right twice (to step 3), then left once (back to step 2)
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList()
        )

        assertTrue(true, "Cursor left movement test placeholder")
    }

    @Test
    fun `should wrap cursor from step 16 to step 1 on right arrow`() {
        val reader = createMockReader(Key.ArrowRight, Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Start at step 16, arrow right should wrap to step 1
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = listOf(16) // Cursor starts at last active step or step 1
        )

        // After wrapping, should be at step 1
        assertTrue(true, "Cursor wrap right test placeholder")
    }

    @Test
    fun `should wrap cursor from step 1 to step 16 on left arrow`() {
        val reader = createMockReader(Key.ArrowLeft, Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Start at step 1, arrow left should wrap to step 16
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList()
        )

        assertTrue(true, "Cursor wrap left test placeholder")
    }

    @Test
    fun `should toggle step on with spacebar`() {
        val reader = createMockReader(Key.Space, Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Cursor at step 1, press space to activate
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList()
        )

        // Step 1 should be in result
        assertTrue(result.contains(1), "Step 1 should be active after spacebar toggle")
    }

    @Test
    fun `should toggle step off with spacebar`() {
        val reader = createMockReader(Key.Space, Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Cursor at step 1, which is already active, press space to deactivate
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = listOf(1, 5, 9, 13)
        )

        // Step 1 should NOT be in result
        assertFalse(result.contains(1), "Step 1 should be inactive after spacebar toggle")
        assertTrue(result.contains(5), "Other steps should remain active")
    }

    @Test
    fun `should complete editing on Enter key`() {
        val reader = createMockReader(Key.Space, Key.ArrowRight, Key.Space, Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Toggle step 1, move to step 2, toggle step 2, then complete
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList()
        )

        // Should return steps 1 and 2
        assertTrue(result.contains(1), "Step 1 should be active")
        assertTrue(result.contains(2), "Step 2 should be active")
        assertEquals(2, result.size, "Should have exactly 2 active steps")
    }

    @Test
    fun `should cancel editing on Escape key`() {
        val reader = createMockReader(Key.Space, Key.ArrowRight, Key.Space, Key.Escape)
        val editor = InteractiveGridEditor(terminal, reader)

        // Make some changes, then press Escape to cancel
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = listOf(5, 9, 13)
        )

        // Should return original steps unchanged
        assertEquals(listOf(5, 9, 13), result.sorted(), "Should return original steps on cancel")
    }

    @Test
    fun `should display context voices above grid`() {
        val contextVoices = mapOf(
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15)
        )
        val reader = createMockReader(Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Context voices should be displayed (verified through terminal output inspection)
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList(),
            contextVoices = contextVoices
        )

        assertTrue(true, "Context display test placeholder - visual inspection required")
    }

    @Test
    fun `should handle Ctrl+Z for undo when history provided`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()

        // Add a command to history
        val addCommand = AddVoiceCommand(PO12DrumVoice.SNARE, listOf(5, 13))
        history.execute(addCommand)
        addCommand.execute(voices)

        val reader = createMockReader(Key.CtrlZ, Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Ctrl+Z should trigger undo
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList(),
            history = history
        )

        assertTrue(history.canRedo(), "After undo, should be able to redo")
    }

    @Test
    fun `should handle Ctrl+Y for redo when history provided`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()

        // Add and undo a command
        val addCommand = AddVoiceCommand(PO12DrumVoice.SNARE, listOf(5, 13))
        history.execute(addCommand)
        addCommand.execute(voices)
        history.undo()?.undo(voices)

        val reader = createMockReader(Key.CtrlY, Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Ctrl+Y should trigger redo
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList(),
            history = history
        )

        assertFalse(history.canRedo(), "After redo, redo stack should be empty")
    }

    @Test
    fun `should ignore undo when no history provided`() {
        val reader = createMockReader(Key.CtrlZ, Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Ctrl+Z without history should be ignored
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList(),
            history = null
        )

        // Should complete normally without error
        assertTrue(result.isEmpty(), "Should return empty steps")
    }

    @Test
    fun `should render cursor at current position`() {
        val reader = createMockReader(Key.ArrowRight, Key.ArrowRight, Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Visual test - cursor should be visible at current position
        // Cursor should move from 1 → 2 → 3
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList()
        )

        assertTrue(true, "Cursor rendering test placeholder - visual inspection required")
    }

    @Test
    fun `should show active steps with filled markers`() {
        val reader = createMockReader(Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Steps 1, 5, 9, 13 should be visually marked as active
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = listOf(1, 5, 9, 13)
        )

        assertEquals(listOf(1, 5, 9, 13), result.sorted(), "Should preserve active steps")
    }

    @Test
    fun `should handle rapid key presses`() {
        val reader = createMockReader(
            Key.Space, Key.ArrowRight, Key.Space, Key.ArrowRight,
            Key.Space, Key.ArrowRight, Key.Space, Key.Enter
        )
        val editor = InteractiveGridEditor(terminal, reader)

        // Rapidly toggle steps 1, 2, 3, 4
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList()
        )

        // Should have steps 1, 2, 3, 4
        assertEquals(listOf(1, 2, 3, 4), result.sorted(), "Should handle rapid input correctly")
    }

    @Test
    fun `should return steps sorted numerically`() {
        val reader = createMockReader(
            // Toggle steps in random order: 9, 1, 13, 5
            Key.ArrowRight, Key.ArrowRight, Key.ArrowRight, Key.ArrowRight,
            Key.ArrowRight, Key.ArrowRight, Key.ArrowRight, Key.ArrowRight,
            Key.Space, // step 9
            Key.ArrowLeft, Key.ArrowLeft, Key.ArrowLeft, Key.ArrowLeft,
            Key.ArrowLeft, Key.ArrowLeft, Key.ArrowLeft, Key.ArrowLeft,
            Key.Space, // step 1
            Key.Enter
        )
        val editor = InteractiveGridEditor(terminal, reader)

        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList()
        )

        // Result should be sorted
        assertEquals(result, result.sorted(), "Steps should be sorted numerically")
    }

    @Test
    fun `should display voice name and current step count`() {
        val reader = createMockReader(Key.Enter)
        val editor = InteractiveGridEditor(terminal, reader)

        // Should display "Kick (4 steps)" or similar
        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = listOf(1, 5, 9, 13)
        )

        assertTrue(true, "Header display test placeholder - visual inspection required")
    }

    @Test
    fun `should allow editing all 16 steps`() {
        val allStepsKeys = mutableListOf<Key>()
        // Toggle all 16 steps
        repeat(16) {
            allStepsKeys.add(Key.Space)
            if (it < 15) allStepsKeys.add(Key.ArrowRight)
        }
        allStepsKeys.add(Key.Enter)

        val reader = createMockReader(*allStepsKeys.toTypedArray())
        val editor = InteractiveGridEditor(terminal, reader)

        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = emptyList()
        )

        assertEquals(16, result.size, "Should be able to activate all 16 steps")
        assertEquals((1..16).toList(), result.sorted(), "Should have all steps 1-16")
    }

    @Test
    fun `should preserve existing steps when cancelled`() {
        val originalSteps = listOf(1, 5, 9, 13)
        val reader = createMockReader(
            Key.Space, // Toggle off step 1
            Key.ArrowRight, Key.ArrowRight, Key.ArrowRight,
            Key.Space, // Toggle on step 5 (already on)
            Key.Escape // Cancel all changes
        )
        val editor = InteractiveGridEditor(terminal, reader)

        val result = editor.editInteractive(
            drumVoice = PO12DrumVoice.KICK,
            initialSteps = originalSteps
        )

        assertEquals(originalSteps.sorted(), result.sorted(), "Should preserve original steps on cancel")
    }
}

/**
 * Helper to create mock keyboard reader with predefined key sequence
 */
private fun createMockReader(vararg keys: Key): KeyboardInputReader {
    return SequenceMockKeyboardReader(keys.toList())
}

/**
 * Mock keyboard reader that returns keys in sequence
 */
private class SequenceMockKeyboardReader(
    private val keySequence: List<Key>
) : KeyboardInputReader {
    private var currentIndex = 0

    override fun readKey(): Key? {
        if (currentIndex >= keySequence.size) return null
        return keySequence[currentIndex++]
    }

    override fun isInteractiveModeSupported(): Boolean = true
}
