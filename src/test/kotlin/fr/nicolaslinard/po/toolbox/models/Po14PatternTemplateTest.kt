package fr.nicolaslinard.po.toolbox.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Po14PatternTemplateTest {

    @Test
    fun `provides at least four built-in templates`() {
        assertTrue(BuiltInPo14Templates.all().size >= 4)
    }

    @Test
    fun `every template has unique id, at least one note, and a valid sound`() {
        val templates = BuiltInPo14Templates.all()
        assertEquals(templates.size, templates.map { it.id }.distinct().size)
        templates.forEach { template ->
            assertTrue(template.steps.isNotEmpty(), "${template.id} has no notes")
            assertTrue(template.steps.keys.all { it in 1..16 }, "${template.id} has an out-of-range step")
            assertTrue(PODevice.PO_14.voices.contains(template.sound), "${template.id} uses an unknown sound")
        }
    }

    @Test
    fun `octave bassline alternates the root note across octaves`() {
        val template = BuiltInPo14Templates.all().first { it.id == "octave-bassline" }
        assertEquals(Pitch.C, template.steps[1]?.pitch)
        assertEquals(Pitch.C, template.steps[5]?.pitch)
    }
}
