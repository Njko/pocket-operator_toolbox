package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.BuiltInTemplates
import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GeneratePatternDialogModelTest {

    // --- Initial state ---

    @Test
    fun `should start with no category selected`() {
        val model = GeneratePatternDialogModel()
        assertNull(model.selectedCategory)
    }

    @Test
    fun `should start with no template selected`() {
        val model = GeneratePatternDialogModel()
        assertNull(model.selectedTemplate)
    }

    @Test
    fun `should not be able to create without template`() {
        val model = GeneratePatternDialogModel()
        assertFalse(model.canCreate())
    }

    @Test
    fun `should expose all categories`() {
        val model = GeneratePatternDialogModel()
        assertEquals(BuiltInTemplates.CATEGORIES.size, model.categories.size)
        assertTrue(model.categories.containsValue("Rock"))
        assertTrue(model.categories.containsValue("Jazz"))
    }

    // --- Category selection ---

    @Test
    fun `should return templates when selecting valid category`() {
        val model = GeneratePatternDialogModel()
        val templates = model.selectCategory("Fondamentaux")
        assertTrue(templates.isNotEmpty())
        assertEquals("foundation", model.selectedCategory)
    }

    @Test
    fun `should return empty list for unknown category`() {
        val model = GeneratePatternDialogModel()
        val templates = model.selectCategory("Unknown")
        assertTrue(templates.isEmpty())
    }

    @Test
    fun `should clear template selection when changing category`() {
        val model = GeneratePatternDialogModel()
        val templates = model.selectCategory("Rock")
        model.selectTemplate(templates.first())
        assertTrue(model.canCreate())

        model.selectCategory("Jazz")
        assertNull(model.selectedTemplate)
        assertFalse(model.canCreate())
    }

    @Test
    fun `should return rock templates for Rock category`() {
        val model = GeneratePatternDialogModel()
        val templates = model.selectCategory("Rock")
        assertTrue(templates.all { it.category == "rock" })
    }

    @Test
    fun `should return electronic templates for Electronique category`() {
        val model = GeneratePatternDialogModel()
        val templates = model.selectCategory("Électronique")
        assertTrue(templates.all { it.category == "electronic" })
        assertTrue(templates.size >= 4) // House, Techno, D&B, Trance
    }

    // --- Template selection ---

    @Test
    fun `should be able to create after selecting template`() {
        val model = GeneratePatternDialogModel()
        val templates = model.selectCategory("Fondamentaux")
        model.selectTemplate(templates.first())
        assertTrue(model.canCreate())
    }

    @Test
    fun `should not be able to create after deselecting template`() {
        val model = GeneratePatternDialogModel()
        val templates = model.selectCategory("Fondamentaux")
        model.selectTemplate(templates.first())
        model.selectTemplate(null)
        assertFalse(model.canCreate())
    }

    // --- Build pattern ---

    @Test
    fun `should return null when building without template`() {
        val model = GeneratePatternDialogModel()
        assertNull(model.buildPattern())
    }

    @Test
    fun `should build pattern with template name`() {
        val model = GeneratePatternDialogModel()
        model.selectCategory("Fondamentaux")
        model.selectTemplate(BuiltInTemplates.FOUR_ON_FLOOR)

        val pattern = model.buildPattern()
        assertNotNull(pattern)
        assertEquals("Four on the Floor", pattern.metadata.name)
    }

    @Test
    fun `should build pattern with template BPM`() {
        val model = GeneratePatternDialogModel()
        model.selectTemplate(BuiltInTemplates.BASIC_TECHNO)

        val pattern = model.buildPattern()!!
        assertEquals(128, pattern.metadata.bpm)
    }

    @Test
    fun `should build pattern with template difficulty`() {
        val model = GeneratePatternDialogModel()
        model.selectTemplate(BuiltInTemplates.DRUM_AND_BASS)

        val pattern = model.buildPattern()!!
        assertEquals(Difficulty.ADVANCED, pattern.metadata.difficulty)
    }

    @Test
    fun `should build pattern with genre from category display name`() {
        val model = GeneratePatternDialogModel()
        model.selectCategory("Électronique")
        model.selectTemplate(BuiltInTemplates.HOUSE_CLASSIC)

        val pattern = model.buildPattern()!!
        assertTrue(pattern.metadata.genre.contains("Électronique"))
    }

    @Test
    fun `should build pattern with template voices`() {
        val model = GeneratePatternDialogModel()
        model.selectTemplate(BuiltInTemplates.BASIC_ROCK)

        val pattern = model.buildPattern()!!
        assertTrue(pattern.voices.containsKey(PO12DrumVoice.KICK))
        assertTrue(pattern.voices.containsKey(PO12DrumVoice.SNARE))
        assertTrue(pattern.voices.containsKey(PO12DrumVoice.CLOSED_HH))
        assertEquals(listOf(1, 9), pattern.voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should build pattern with number 1`() {
        val model = GeneratePatternDialogModel()
        model.selectTemplate(BuiltInTemplates.FOUR_ON_FLOOR)

        val pattern = model.buildPattern()!!
        assertEquals(1, pattern.number)
    }
}
