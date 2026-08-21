package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.BuiltInPo14Templates
import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Step
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import fr.nicolaslinard.po.toolbox.models.Pitch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NewMelodicPatternDialogModelTest {

    @Test
    fun `defaults to create mode with an empty name and no notes`() {
        val model = NewMelodicPatternDialogModel()
        assertFalse(model.isEditMode)
        assertEquals("", model.name)
        assertTrue(model.steps.isEmpty())
    }

    @Test
    fun `is invalid when name is blank`() {
        val model = NewMelodicPatternDialogModel()
        model.name = "   "
        assertFalse(model.isValid())
    }

    @Test
    fun `is valid once a name is set`() {
        val model = NewMelodicPatternDialogModel()
        model.name = "My Bassline"
        assertTrue(model.isValid())
    }

    @Test
    fun `setStep adds a note and clearStep removes it`() {
        val model = NewMelodicPatternDialogModel()
        model.setStep(1, PO14Step(Pitch.C, 2))
        assertEquals(Pitch.C, model.steps[1]?.pitch)

        model.clearStep(1)
        assertNull(model.steps[1])
    }

    @Test
    fun `setStep rejects steps outside 1 to 16`() {
        val model = NewMelodicPatternDialogModel()
        assertTrue(runCatching { model.setStep(17, PO14Step(Pitch.C, 2)) }.isFailure)
    }

    @Test
    fun `applyTemplate replaces steps and sound and suggests a BPM`() {
        val model = NewMelodicPatternDialogModel()
        val template = BuiltInPo14Templates.OCTAVE_BASSLINE

        model.applyTemplate(template)

        assertEquals(template.steps, model.steps)
        assertEquals(template.sound, model.sound)
        assertEquals(template.suggestedBPM.toString(), model.bpm)
    }

    @Test
    fun `buildPattern produces a PO14Pattern with PO-14 as the device model`() {
        val model = NewMelodicPatternDialogModel()
        model.name = "Built Pattern"
        model.bpm = "128"
        model.setStep(1, PO14Step(Pitch.C, 2))

        val pattern = model.buildPattern()

        assertEquals("Built Pattern", pattern.metadata.name)
        assertEquals(128, pattern.metadata.bpm)
        assertEquals("PO-14", pattern.metadata.deviceModel)
        assertEquals(Pitch.C, pattern.getNote(1)?.pitch)
    }

    @Test
    fun `pre-fills from an existing pattern in edit mode`() {
        val existing = PO14Pattern(
            steps = mapOf(1 to PO14Step(Pitch.G, 1)),
            sound = PODevice.PO_14.voices.first(),
            metadata = PatternMetadata(name = "Existing", difficulty = Difficulty.INTERMEDIATE, deviceModel = "PO-14"),
            number = 5
        )
        val model = NewMelodicPatternDialogModel(existing)

        assertTrue(model.isEditMode)
        assertEquals("Existing", model.name)
        assertEquals(5, model.patternNumber)
        assertEquals("intermediate", model.difficulty)
        assertEquals(Pitch.G, model.steps[1]?.pitch)
    }
}
