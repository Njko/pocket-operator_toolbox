package fr.nicolaslinard.po.toolbox.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TDD RED Phase: Tests for PatternTemplate
 *
 * Tests pattern template model and built-in template definitions.
 * Verifies template metadata, voice patterns, and filtering capabilities.
 */
class PatternTemplateTest {

    @Test
    fun `should have four-on-the-floor template`() {
        val template = BuiltInTemplates.FOUR_ON_FLOOR

        assertEquals("four-on-the-floor", template.id)
        assertEquals("Four on the Floor", template.name)
        assertEquals("foundation", template.category)
        assertEquals(Difficulty.BEGINNER, template.difficulty)
        assertTrue(template.voices.containsKey(PO12DrumVoice.KICK))
        assertEquals(listOf(1, 5, 9, 13), template.voices[PO12DrumVoice.KICK])
    }

    @Test
    fun `should have basic rock template`() {
        val template = BuiltInTemplates.BASIC_ROCK

        assertEquals("basic-rock", template.id)
        assertEquals("Basic Rock", template.name)
        assertEquals("genre", template.category)
        assertEquals(Difficulty.BEGINNER, template.difficulty)
        // Rock pattern: kick on 1 and 9, snare on 5 and 13
        assertTrue(template.voices.containsKey(PO12DrumVoice.KICK))
        assertTrue(template.voices.containsKey(PO12DrumVoice.SNARE))
    }

    @Test
    fun `should have breakbeat template`() {
        val template = BuiltInTemplates.BASIC_BREAKBEAT

        assertEquals("basic-breakbeat", template.id)
        assertEquals("Basic Breakbeat", template.name)
        assertEquals("genre", template.category)
        assertTrue(template.difficulty == Difficulty.INTERMEDIATE || template.difficulty == Difficulty.ADVANCED)
        // Breakbeat should have kick, snare, and hi-hats
        assertTrue(template.voices.size >= 3)
    }

    @Test
    fun `should have hip-hop template`() {
        val template = BuiltInTemplates.BASIC_HIPHOP

        assertEquals("basic-hiphop", template.id)
        assertEquals("Basic Hip-Hop", template.name)
        assertEquals("genre", template.category)
        // Hip-hop typically has kick and snare
        assertTrue(template.voices.containsKey(PO12DrumVoice.KICK))
        assertTrue(template.voices.containsKey(PO12DrumVoice.SNARE))
    }

    @Test
    fun `should have techno template`() {
        val template = BuiltInTemplates.BASIC_TECHNO

        assertEquals("basic-techno", template.id)
        assertEquals("Basic Techno", template.name)
        assertEquals("genre", template.category)
        // Techno should have kick on all 4 beats
        assertTrue(template.voices.containsKey(PO12DrumVoice.KICK))
    }

    @Test
    fun `should list all built-in templates`() {
        val templates = BuiltInTemplates.all()

        assertTrue(templates.size >= 5, "Should have at least 5 built-in templates")
        assertTrue(templates.any { it.id == "four-on-the-floor" })
        assertTrue(templates.any { it.id == "basic-rock" })
        assertTrue(templates.any { it.id == "basic-breakbeat" })
    }

    @Test
    fun `should filter templates by category`() {
        val foundationTemplates = BuiltInTemplates.byCategory("foundation")
        val genreTemplates = BuiltInTemplates.byCategory("genre")

        assertTrue(foundationTemplates.isNotEmpty(), "Should have foundation templates")
        assertTrue(genreTemplates.isNotEmpty(), "Should have genre templates")
        assertTrue(foundationTemplates.all { it.category == "foundation" })
        assertTrue(genreTemplates.all { it.category == "genre" })
    }

    @Test
    fun `should filter templates by difficulty`() {
        val beginnerTemplates = BuiltInTemplates.byDifficulty(Difficulty.BEGINNER)
        val intermediateTemplates = BuiltInTemplates.byDifficulty(Difficulty.INTERMEDIATE)

        assertTrue(beginnerTemplates.isNotEmpty(), "Should have beginner templates")
        assertTrue(beginnerTemplates.all { it.difficulty == Difficulty.BEGINNER })
        // All intermediate templates should have INTERMEDIATE difficulty
        assertTrue(intermediateTemplates.all { it.difficulty == Difficulty.INTERMEDIATE })
    }

    @Test
    fun `should validate template has valid steps`() {
        val templates = BuiltInTemplates.all()

        for (template in templates) {
            for ((voice, steps) in template.voices) {
                // All steps should be 1-16
                assertTrue(steps.all { it in 1..16 },
                    "${template.id}: All steps should be 1-16 for ${voice.displayName}")
                // Steps should be sorted and unique
                assertEquals(steps.sorted().distinct(), steps,
                    "${template.id}: Steps should be sorted and unique for ${voice.displayName}")
            }
        }
    }

    @Test
    fun `should have suggested BPM for templates`() {
        val template = BuiltInTemplates.FOUR_ON_FLOOR

        assertNotNull(template.suggestedBPM, "Four-on-the-floor should have suggested BPM")
        assertTrue(template.suggestedBPM!! in 60..300, "BPM should be valid range")
    }
}
