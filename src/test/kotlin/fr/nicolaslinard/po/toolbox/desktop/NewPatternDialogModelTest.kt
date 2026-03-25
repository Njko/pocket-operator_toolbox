package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.TestFixtures
import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NewPatternDialogModelTest {

    // --- Validation ---

    @Test
    fun `should be invalid when name is empty`() {
        val model = NewPatternDialogModel()
        model.name = ""
        assertFalse(model.isValid())
    }

    @Test
    fun `should be invalid when name is only whitespace`() {
        val model = NewPatternDialogModel()
        model.name = "   "
        assertFalse(model.isValid())
    }

    @Test
    fun `should be valid when name is not empty`() {
        val model = NewPatternDialogModel()
        model.name = "My Pattern"
        assertTrue(model.isValid())
    }

    // --- Create mode ---

    @Test
    fun `should default to create mode`() {
        val model = NewPatternDialogModel()
        assertFalse(model.isEditMode)
    }

    @Test
    fun `should default to pattern number 1`() {
        val model = NewPatternDialogModel()
        assertEquals(1, model.patternNumber)
    }

    @Test
    fun `should default to empty name`() {
        val model = NewPatternDialogModel()
        assertEquals("", model.name)
    }

    @Test
    fun `should default to no difficulty`() {
        val model = NewPatternDialogModel()
        assertEquals("-", model.difficulty)
    }

    @Test
    fun `should start with no voices`() {
        val model = NewPatternDialogModel()
        assertTrue(model.voices.isEmpty())
    }

    // --- Edit mode ---

    @Test
    fun `should be in edit mode when existing pattern provided`() {
        val pattern = TestFixtures.createSimplePattern()
        val model = NewPatternDialogModel(pattern)
        assertTrue(model.isEditMode)
    }

    @Test
    fun `should pre-fill name from existing pattern`() {
        val pattern = TestFixtures.createSimplePattern(name = "Amen Break")
        val model = NewPatternDialogModel(pattern)
        assertEquals("Amen Break", model.name)
    }

    @Test
    fun `should pre-fill BPM from existing pattern`() {
        val pattern = TestFixtures.createSimplePattern(bpm = 174)
        val model = NewPatternDialogModel(pattern)
        assertEquals("174", model.bpm)
    }

    @Test
    fun `should pre-fill pattern number from existing pattern`() {
        val pattern = TestFixtures.createSimplePattern(patternNumber = 5)
        val model = NewPatternDialogModel(pattern)
        assertEquals(5, model.patternNumber)
    }

    @Test
    fun `should pre-fill difficulty from existing pattern`() {
        val pattern = TestFixtures.createSimplePattern()
        val model = NewPatternDialogModel(pattern)
        assertEquals("beginner", model.difficulty)
    }

    @Test
    fun `should pre-fill voices from existing pattern`() {
        val pattern = TestFixtures.createSimplePattern()
        val model = NewPatternDialogModel(pattern)
        assertEquals(2, model.voices.size)
        assertTrue(model.voices.containsKey(PO12DrumVoice.KICK))
        assertTrue(model.voices.containsKey(PO12DrumVoice.SNARE))
    }

    @Test
    fun `should pre-fill steps from existing pattern`() {
        val pattern = TestFixtures.createSimplePattern()
        val model = NewPatternDialogModel(pattern)
        assertEquals(listOf(1, 5, 9, 13), model.voices[PO12DrumVoice.KICK])
        assertEquals(listOf(5, 13), model.voices[PO12DrumVoice.SNARE])
    }

    // --- Voice management ---

    @Test
    fun `should add voice with empty steps`() {
        val model = NewPatternDialogModel()
        model.addVoice(PO12DrumVoice.KICK)
        assertTrue(model.voices.containsKey(PO12DrumVoice.KICK))
        assertTrue(model.voices[PO12DrumVoice.KICK]!!.isEmpty())
    }

    @Test
    fun `should add voice with predefined steps`() {
        val model = NewPatternDialogModel()
        model.addVoice(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))
        assertEquals(listOf(1, 5, 9, 13), model.voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should not add duplicate voice`() {
        val model = NewPatternDialogModel()
        model.addVoice(PO12DrumVoice.KICK, listOf(1, 5))
        model.addVoice(PO12DrumVoice.KICK, listOf(9, 13))
        assertEquals(listOf(1, 5), model.voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should remove voice`() {
        val model = NewPatternDialogModel()
        model.addVoice(PO12DrumVoice.KICK)
        model.removeVoice(PO12DrumVoice.KICK)
        assertFalse(model.voices.containsKey(PO12DrumVoice.KICK))
    }

    @Test
    fun `should update steps for existing voice`() {
        val model = NewPatternDialogModel()
        model.addVoice(PO12DrumVoice.KICK, listOf(1, 5))
        model.setSteps(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))
        assertEquals(listOf(1, 5, 9, 13), model.voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should keep voices sorted by PO number`() {
        val model = NewPatternDialogModel()
        model.addVoice(PO12DrumVoice.SNARE)
        model.addVoice(PO12DrumVoice.KICK)
        model.addVoice(PO12DrumVoice.CLOSED_HH)

        val voiceOrder = model.voices.keys.toList()
        assertEquals(PO12DrumVoice.KICK, voiceOrder[0])
        assertEquals(PO12DrumVoice.SNARE, voiceOrder[1])
        assertEquals(PO12DrumVoice.CLOSED_HH, voiceOrder[2])
    }

    @Test
    fun `should list available voices excluding added ones`() {
        val model = NewPatternDialogModel()
        model.addVoice(PO12DrumVoice.KICK)
        model.addVoice(PO12DrumVoice.SNARE)

        val available = model.availableVoices()
        assertEquals(14, available.size)
        assertFalse(available.contains(PO12DrumVoice.KICK))
        assertFalse(available.contains(PO12DrumVoice.SNARE))
    }

    // --- Build pattern ---

    @Test
    fun `should build pattern with correct name`() {
        val model = NewPatternDialogModel()
        model.name = "  My Pattern  "
        model.addVoice(PO12DrumVoice.KICK, listOf(1, 5, 9, 13))

        val pattern = model.buildPattern()
        assertEquals("My Pattern", pattern.metadata.name)
    }

    @Test
    fun `should build pattern with correct BPM`() {
        val model = NewPatternDialogModel()
        model.name = "Test"
        model.bpm = "140"

        val pattern = model.buildPattern()
        assertEquals(140, pattern.metadata.bpm)
    }

    @Test
    fun `should build pattern with null BPM when empty`() {
        val model = NewPatternDialogModel()
        model.name = "Test"
        model.bpm = ""

        val pattern = model.buildPattern()
        assertNull(pattern.metadata.bpm)
    }

    @Test
    fun `should build pattern with null BPM when invalid`() {
        val model = NewPatternDialogModel()
        model.name = "Test"
        model.bpm = "abc"

        val pattern = model.buildPattern()
        assertNull(pattern.metadata.bpm)
    }

    @Test
    fun `should build pattern with correct difficulty`() {
        val model = NewPatternDialogModel()
        model.name = "Test"
        model.difficulty = "intermediate"

        val pattern = model.buildPattern()
        assertEquals(Difficulty.INTERMEDIATE, pattern.metadata.difficulty)
    }

    @Test
    fun `should build pattern with null difficulty when dash`() {
        val model = NewPatternDialogModel()
        model.name = "Test"
        model.difficulty = "-"

        val pattern = model.buildPattern()
        assertNull(pattern.metadata.difficulty)
    }

    @Test
    fun `should build pattern with correct pattern number`() {
        val model = NewPatternDialogModel()
        model.name = "Test"
        model.patternNumber = 7

        val pattern = model.buildPattern()
        assertEquals(7, pattern.number)
    }

    @Test
    fun `should build pattern excluding voices with no active steps`() {
        val model = NewPatternDialogModel()
        model.name = "Test"
        model.addVoice(PO12DrumVoice.KICK, listOf(1, 5))
        model.addVoice(PO12DrumVoice.SNARE) // empty steps

        val pattern = model.buildPattern()
        assertEquals(1, pattern.voices.size)
        assertTrue(pattern.voices.containsKey(PO12DrumVoice.KICK))
    }

    @Test
    fun `should build pattern filtering out-of-range steps`() {
        val model = NewPatternDialogModel()
        model.name = "Test"
        model.setSteps(PO12DrumVoice.KICK, listOf(0, 1, 5, 17, 20))

        val pattern = model.buildPattern()
        assertEquals(listOf(1, 5), pattern.voices[PO12DrumVoice.KICK])
    }
}
