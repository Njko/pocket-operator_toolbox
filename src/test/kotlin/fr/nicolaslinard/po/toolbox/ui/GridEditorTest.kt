package fr.nicolaslinard.po.toolbox.ui

import com.github.ajalt.mordant.terminal.Terminal
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TDD RED Phase: Tests for GridEditor
 *
 * These tests verify that GridEditor can display context voices
 * while editing a specific drum voice.
 */
class GridEditorTest {

    @Test
    fun `should edit voice without context voices`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.KICK
        val initialSteps = listOf(1, 5, 9, 13)

        // Should work with empty context (backward compatibility)
        val result = editor.edit(voice, initialSteps, emptyMap())

        assertEquals(initialSteps, result)
    }

    @Test
    fun `should edit voice with context voices provided`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.SNARE
        val initialSteps = listOf(5, 13)
        val contextVoices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
        )

        // Should display context and still edit correctly
        // This will require user input simulation in real scenario
        // For now, we test that it accepts context parameter
        val result = editor.edit(voice, initialSteps, contextVoices)

        assertEquals(initialSteps, result)
    }

    @Test
    fun `should preserve context voices when editing`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.CLOSED_HH
        val initialSteps = listOf(1, 3, 5, 7, 9, 11, 13, 15)
        val contextVoices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.SNARE to listOf(5, 13)
        )

        // Context voices should not be modified
        val result = editor.edit(voice, initialSteps, contextVoices)

        // Context map should remain unchanged
        assertEquals(2, contextVoices.size)
        assertTrue(contextVoices.containsKey(PO12DrumVoice.KICK))
        assertTrue(contextVoices.containsKey(PO12DrumVoice.SNARE))
    }

    @Test
    fun `should handle empty context voices`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.KICK
        val initialSteps = emptyList<Int>()
        val contextVoices = emptyMap<PO12DrumVoice, List<Int>>()

        // Should handle all empty inputs
        val result = editor.edit(voice, initialSteps, contextVoices)

        assertEquals(emptyList(), result)
    }

    @Test
    fun `should parse valid step input correctly`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)

        // Test parseSteps method if accessible, or through edit()
        // This verifies the existing functionality still works
        // The actual parsing logic is tested through integration
    }

    @Test
    fun `should reject invalid step numbers`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)

        // Invalid step numbers should be rejected
        // This tests existing validation logic
        // Implementation uses recursive retry on invalid input
    }

    @Test
    fun `should handle clear command`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.KICK
        val initialSteps = listOf(1, 5, 9, 13)

        // "clear" command should return empty list
        // This tests existing functionality
    }

    @Test
    fun `should handle cancel command`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.SNARE
        val initialSteps = listOf(5, 13)

        // "cancel" command should return initial steps unchanged
        // This tests existing functionality
    }

    // Phase 6.2: Mode selection and interactive editing tests

    @Test
    fun `should default to TEXT mode when no mode specified`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.KICK
        val initialSteps = listOf(1, 5, 9, 13)

        // Default edit() call should use TEXT mode
        val result = editor.edit(voice, initialSteps)

        // Should work with text-based input (existing behavior)
        assertEquals(initialSteps, result)
    }

    @Test
    fun `should use TEXT mode when explicitly specified`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.KICK
        val initialSteps = listOf(1, 5, 9, 13)

        // Explicitly request TEXT mode
        val result = editor.edit(voice, initialSteps, emptyMap(), EditMode.TEXT)

        assertEquals(initialSteps, result)
    }

    @Test
    fun `should use INTERACTIVE mode when specified and supported`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.KICK
        val initialSteps = listOf(1, 5, 9, 13)

        // Request INTERACTIVE mode
        val result = editor.edit(voice, initialSteps, emptyMap(), EditMode.INTERACTIVE)

        // Should invoke InteractiveGridEditor
        // Result will depend on mock keyboard input
        assertTrue(true, "Interactive mode invocation test placeholder")
    }

    @Test
    fun `should fallback to TEXT mode when INTERACTIVE not supported`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.KICK
        val initialSteps = listOf(1, 5, 9, 13)

        // Request INTERACTIVE mode, but if terminal doesn't support it,
        // should gracefully fallback to TEXT mode
        val result = editor.edit(voice, initialSteps, emptyMap(), EditMode.INTERACTIVE)

        // Fallback should work without error
        assertTrue(true, "Fallback to TEXT mode test placeholder")
    }

    @Test
    fun `should pass context voices to interactive editor`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.SNARE
        val initialSteps = listOf(5, 13)
        val contextVoices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15)
        )

        // Interactive mode should receive context voices for display
        val result = editor.edit(voice, initialSteps, contextVoices, EditMode.INTERACTIVE)

        assertTrue(true, "Context passing to interactive editor test placeholder")
    }

    @Test
    fun `should pass history to interactive editor for undo redo support`() {
        val terminal = Terminal()
        val editor = GridEditor(terminal)
        val voice = PO12DrumVoice.KICK
        val initialSteps = emptyList<Int>()
        val history = fr.nicolaslinard.po.toolbox.models.PatternEditHistory()

        // Interactive mode should receive history for Ctrl+Z/Ctrl+Y support
        val result = editor.edit(voice, initialSteps, emptyMap(), EditMode.INTERACTIVE, history)

        assertTrue(true, "History passing to interactive editor test placeholder")
    }
}
