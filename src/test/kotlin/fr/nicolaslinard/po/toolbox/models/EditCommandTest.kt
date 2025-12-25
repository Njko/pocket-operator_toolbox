package fr.nicolaslinard.po.toolbox.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TDD RED Phase: Tests for EditCommand implementations
 *
 * Tests for AddVoiceCommand, RemoveVoiceCommand, and ModifyVoiceCommand
 * to ensure proper execution and undo behavior.
 */
class EditCommandTest {

    @Test
    fun `AddVoiceCommand should add voice to pattern`() {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()
        val command = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        command.execute(voices)

        assertTrue(voices.containsKey(PO12DrumVoice.KICK))
        assertEquals(listOf(1, 5, 9, 13), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `AddVoiceCommand undo should remove voice`() {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()
        val command = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        command.execute(voices)
        assertTrue(voices.containsKey(PO12DrumVoice.KICK))

        command.undo(voices)

        assertFalse(voices.containsKey(PO12DrumVoice.KICK))
    }

    @Test
    fun `RemoveVoiceCommand should remove voice from pattern`() {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
        )
        val command = RemoveVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        command.execute(voices)

        assertFalse(voices.containsKey(PO12DrumVoice.KICK))
    }

    @Test
    fun `RemoveVoiceCommand undo should restore voice`() {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
        )
        val command = RemoveVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        command.execute(voices)
        assertFalse(voices.containsKey(PO12DrumVoice.KICK))

        command.undo(voices)

        assertTrue(voices.containsKey(PO12DrumVoice.KICK))
        assertEquals(listOf(1, 5, 9, 13), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `ModifyVoiceCommand should update voice steps`() {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
        )
        val command = ModifyVoiceCommand(
            PO12DrumVoice.KICK,
            oldSteps = listOf(1, 5, 9, 13),
            newSteps = listOf(1, 9)
        )

        command.execute(voices)

        assertEquals(listOf(1, 9), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `ModifyVoiceCommand undo should restore old steps`() {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
        )
        val command = ModifyVoiceCommand(
            PO12DrumVoice.KICK,
            oldSteps = listOf(1, 5, 9, 13),
            newSteps = listOf(1, 9)
        )

        command.execute(voices)
        assertEquals(listOf(1, 9), voices[PO12DrumVoice.KICK])

        command.undo(voices)

        assertEquals(listOf(1, 5, 9, 13), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should describe AddVoiceCommand correctly`() {
        val command = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        val description = command.describe()

        assertTrue(description.contains("Add") || description.contains("add"))
        assertTrue(description.contains("Kick") || description.contains("Bass Drum"))
        assertTrue(description.contains("1") || description.contains("steps"))
    }

    @Test
    fun `should describe RemoveVoiceCommand correctly`() {
        val command = RemoveVoiceCommand(PO12DrumVoice.SNARE, listOf(5, 13))

        val description = command.describe()

        assertTrue(description.contains("Remove") || description.contains("remove"))
        assertTrue(description.contains("Snare"))
    }

    @Test
    fun `should describe ModifyVoiceCommand correctly`() {
        val command = ModifyVoiceCommand(
            PO12DrumVoice.CLOSED_HH,
            oldSteps = listOf(1, 3, 5, 7),
            newSteps = listOf(1, 5, 9, 13)
        )

        val description = command.describe()

        assertTrue(description.contains("Modif") || description.contains("modif") ||
                   description.contains("Update") || description.contains("update") ||
                   description.contains("Change") || description.contains("change"))
        assertTrue(description.contains("Closed Hi-Hat") || description.contains("HH"))
    }

    @Test
    fun `should handle undo on empty pattern`() {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()
        val command = RemoveVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        command.undo(voices)  // Should restore even if pattern is empty

        assertTrue(voices.containsKey(PO12DrumVoice.KICK))
        assertEquals(listOf(1, 5, 9, 13), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should handle redo after undo`() {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()
        val command = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        command.execute(voices)
        assertTrue(voices.containsKey(PO12DrumVoice.KICK))

        command.undo(voices)
        assertFalse(voices.containsKey(PO12DrumVoice.KICK))

        command.execute(voices)  // Redo by executing again
        assertTrue(voices.containsKey(PO12DrumVoice.KICK))
        assertEquals(listOf(1, 5, 9, 13), voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should preserve voice order after undo-redo`() {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()
        val command1 = AddVoiceCommand(PO12DrumVoice.KICK, listOf(1))
        val command2 = AddVoiceCommand(PO12DrumVoice.SNARE, listOf(5))
        val command3 = AddVoiceCommand(PO12DrumVoice.CLOSED_HH, listOf(1, 3))

        command1.execute(voices)
        command2.execute(voices)
        command3.execute(voices)

        command2.undo(voices)
        assertFalse(voices.containsKey(PO12DrumVoice.SNARE))

        command2.execute(voices)
        assertTrue(voices.containsKey(PO12DrumVoice.SNARE))

        // All three voices should be present
        assertEquals(3, voices.size)
    }
}
