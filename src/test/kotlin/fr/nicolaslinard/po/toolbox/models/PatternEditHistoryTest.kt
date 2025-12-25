package fr.nicolaslinard.po.toolbox.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * TDD RED Phase: Tests for PatternEditHistory
 *
 * These tests define the behavior of the undo/redo history management
 * using the Command Pattern for reversible operations.
 */
class PatternEditHistoryTest {

    @Test
    fun `should start with empty history`() {
        val history = PatternEditHistory()

        assertFalse(history.canUndo())
        assertFalse(history.canRedo())
        assertNull(history.getUndoDescription())
        assertNull(history.getRedoDescription())
    }

    @Test
    fun `should execute add voice command`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()
        val command = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        history.execute(command)
        command.execute(voices)

        assertTrue(voices.containsKey(PO12DrumVoice.KICK))
        assertEquals(listOf(1, 5, 9, 13), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should execute remove voice command`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
        )
        val command = RemoveVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        history.execute(command)
        command.execute(voices)

        assertFalse(voices.containsKey(PO12DrumVoice.KICK))
    }

    @Test
    fun `should execute modify voice command`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
        )
        val command = ModifyVoiceCommand(
            PO12DrumVoice.KICK,
            oldSteps = listOf(1, 5, 9, 13),
            newSteps = listOf(1, 9)
        )

        history.execute(command)
        command.execute(voices)

        assertEquals(listOf(1, 9), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should undo add voice command`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()
        val command = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        history.execute(command)
        command.execute(voices)
        assertTrue(voices.containsKey(PO12DrumVoice.KICK))

        val undoCommand = history.undo()
        undoCommand?.undo(voices)

        assertFalse(voices.containsKey(PO12DrumVoice.KICK))
    }

    @Test
    fun `should undo remove voice command`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
        )
        val command = RemoveVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        history.execute(command)
        command.execute(voices)
        assertFalse(voices.containsKey(PO12DrumVoice.KICK))

        val undoCommand = history.undo()
        undoCommand?.undo(voices)

        assertTrue(voices.containsKey(PO12DrumVoice.KICK))
        assertEquals(listOf(1, 5, 9, 13), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should undo modify voice command`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
        )
        val command = ModifyVoiceCommand(
            PO12DrumVoice.KICK,
            oldSteps = listOf(1, 5, 9, 13),
            newSteps = listOf(1, 9)
        )

        history.execute(command)
        command.execute(voices)
        assertEquals(listOf(1, 9), voices[PO12DrumVoice.KICK])

        val undoCommand = history.undo()
        undoCommand?.undo(voices)

        assertEquals(listOf(1, 5, 9, 13), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should redo after undo`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()
        val command = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        history.execute(command)
        command.execute(voices)

        val undoCommand = history.undo()
        undoCommand?.undo(voices)
        assertFalse(voices.containsKey(PO12DrumVoice.KICK))

        val redoCommand = history.redo()
        redoCommand?.execute(voices)

        assertTrue(voices.containsKey(PO12DrumVoice.KICK))
        assertEquals(listOf(1, 5, 9, 13), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should clear redo history after new command`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()
        val command1 = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))
        val command2 = AddVoiceCommand(PO12DrumVoice.SNARE, listOf(5, 13))

        history.execute(command1)
        history.undo()
        assertTrue(history.canRedo())

        history.execute(command2)
        assertFalse(history.canRedo())
    }

    @Test
    fun `should not undo when history is empty`() {
        val history = PatternEditHistory()

        val undoCommand = history.undo()

        assertNull(undoCommand)
        assertFalse(history.canUndo())
    }

    @Test
    fun `should not redo when no redo available`() {
        val history = PatternEditHistory()
        val command = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        history.execute(command)

        val redoCommand = history.redo()

        assertNull(redoCommand)
        assertFalse(history.canRedo())
    }

    @Test
    fun `should limit history to max size`() {
        val history = PatternEditHistory(maxHistorySize = 3)
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()

        // Add 4 commands (exceeds max of 3)
        history.execute(AddVoiceCommand(PO12DrumVoice.KICK, listOf(1)))
        history.execute(AddVoiceCommand(PO12DrumVoice.SNARE, listOf(5)))
        history.execute(AddVoiceCommand(PO12DrumVoice.CLOSED_HH, listOf(1, 3)))
        history.execute(AddVoiceCommand(PO12DrumVoice.OPEN_HH, listOf(7)))

        // Should only be able to undo 3 times (oldest command dropped)
        var undoCount = 0
        while (history.canUndo()) {
            history.undo()
            undoCount++
        }

        assertEquals(3, undoCount)
    }

    @Test
    fun `should provide undo description`() {
        val history = PatternEditHistory()
        val command = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        history.execute(command)

        val description = history.getUndoDescription()

        assertTrue(description != null && description.isNotEmpty())
        assertTrue(description!!.contains("Kick") || description.contains("Bass Drum"))
    }

    @Test
    fun `should provide redo description`() {
        val history = PatternEditHistory()
        val command = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        history.execute(command)
        history.undo()

        val description = history.getRedoDescription()

        assertTrue(description != null && description.isNotEmpty())
        assertTrue(description!!.contains("Kick") || description.contains("Bass Drum"))
    }

    @Test
    fun `should handle multiple sequential undos`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()

        history.execute(AddVoiceCommand(PO12DrumVoice.KICK, listOf(1)))
        history.execute(AddVoiceCommand(PO12DrumVoice.SNARE, listOf(5)))
        history.execute(AddVoiceCommand(PO12DrumVoice.CLOSED_HH, listOf(1, 3)))

        // Execute all commands
        AddVoiceCommand(PO12DrumVoice.KICK, listOf(1)).execute(voices)
        AddVoiceCommand(PO12DrumVoice.SNARE, listOf(5)).execute(voices)
        AddVoiceCommand(PO12DrumVoice.CLOSED_HH, listOf(1, 3)).execute(voices)

        // Undo all 3
        history.undo()?.undo(voices)
        history.undo()?.undo(voices)
        history.undo()?.undo(voices)

        assertEquals(0, voices.size)
    }

    @Test
    fun `should handle multiple sequential redos`() {
        val history = PatternEditHistory()
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()

        history.execute(AddVoiceCommand(PO12DrumVoice.KICK, listOf(1)))
        history.execute(AddVoiceCommand(PO12DrumVoice.SNARE, listOf(5)))

        AddVoiceCommand(PO12DrumVoice.KICK, listOf(1)).execute(voices)
        AddVoiceCommand(PO12DrumVoice.SNARE, listOf(5)).execute(voices)

        // Undo both
        history.undo()?.undo(voices)
        history.undo()?.undo(voices)

        // Redo both
        history.redo()?.execute(voices)
        history.redo()?.execute(voices)

        assertEquals(2, voices.size)
        assertTrue(voices.containsKey(PO12DrumVoice.KICK))
        assertTrue(voices.containsKey(PO12DrumVoice.SNARE))
    }

    @Test
    fun `should clear entire history`() {
        val history = PatternEditHistory()

        history.execute(AddVoiceCommand(PO12DrumVoice.KICK, listOf(1)))
        history.execute(AddVoiceCommand(PO12DrumVoice.SNARE, listOf(5)))
        assertTrue(history.canUndo())

        history.clear()

        assertFalse(history.canUndo())
        assertFalse(history.canRedo())
        assertNull(history.getUndoDescription())
    }
}
