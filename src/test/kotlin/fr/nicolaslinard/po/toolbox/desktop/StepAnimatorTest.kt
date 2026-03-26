package fr.nicolaslinard.po.toolbox.desktop

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

/**
 * TDD: Tests for StepAnimator logic.
 * Tests the label classification logic without requiring JavaFX runtime.
 */
class StepAnimatorTest {

    @Test
    fun `should identify active label by style class`() {
        assertTrue(StepAnimator.isActiveStyleClass("step-active"))
    }

    @Test
    fun `should not identify inactive label as active`() {
        assertFalse(StepAnimator.isActiveStyleClass("step-inactive"))
    }

    @Test
    fun `should not identify header as active`() {
        assertFalse(StepAnimator.isActiveStyleClass("step-header"))
    }

    @Test
    fun `should filter active classes from mixed list`() {
        val classes = listOf("step-active", "step-inactive", "step-header", "step-active")
        val activeCount = classes.count { StepAnimator.isActiveStyleClass(it) }
        assertEquals(2, activeCount)
    }

    @Test
    fun `should define hit style class constant`() {
        assertEquals("step-hit", StepAnimator.HIT_STYLE_CLASS)
    }

    @Test
    fun `should define active style class constant`() {
        assertEquals("step-active", StepAnimator.ACTIVE_STYLE_CLASS)
    }

    @Test
    fun `should define voice hit style class constant`() {
        assertEquals("voice-hit", StepAnimator.VOICE_HIT_STYLE_CLASS)
    }
}
