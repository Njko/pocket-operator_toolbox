package fr.nicolaslinard.po.toolbox.ui

import com.github.ajalt.mordant.terminal.Terminal
import fr.nicolaslinard.po.toolbox.TestFixtures
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TDD RED Phase: Tests for MultiVoiceRenderer
 *
 * These tests define the behavior of the multi-voice compact grid renderer
 * that shows existing voices during pattern creation.
 */
class MultiVoiceRendererTest {

    @Test
    fun `should render empty output when no voices provided`() {
        val terminal = Terminal()
        val renderer = MultiVoiceRenderer(terminal)
        val voices = emptyMap<PO12DrumVoice, List<Int>>()

        // Should handle empty map gracefully without errors
        renderer.renderCompactGrid(voices)
        // No exception = success for empty case
    }

    @Test
    fun `should render single voice in compact format`() {
        val terminal = Terminal()
        val renderer = MultiVoiceRenderer(terminal)
        val voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
        )

        // Should render without errors
        renderer.renderCompactGrid(voices)
        // Implementation will be tested via visual output
    }

    @Test
    fun `should render multiple voices with distinct formatting`() {
        val terminal = Terminal()
        val renderer = MultiVoiceRenderer(terminal)
        val voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15)
        )

        // Should render all voices without errors
        renderer.renderCompactGrid(voices)
    }

    @Test
    fun `should align step numbers across all voices`() {
        val terminal = Terminal()
        val renderer = MultiVoiceRenderer(terminal)
        val voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.SNARE to listOf(5, 13)
        )

        // Should maintain proper alignment for step grid
        renderer.renderCompactGrid(voices)
        // Visual alignment will be tested manually, but should not throw
    }

    @Test
    fun `should truncate display when more than 5 voices`() {
        val terminal = Terminal()
        val renderer = MultiVoiceRenderer(terminal)
        val voices = mapOf(
            PO12DrumVoice.KICK to listOf(1),
            PO12DrumVoice.SNARE to listOf(2),
            PO12DrumVoice.CLOSED_HH to listOf(3),
            PO12DrumVoice.OPEN_HH to listOf(4),
            PO12DrumVoice.TOM_LOW to listOf(5),
            PO12DrumVoice.TOM_MID to listOf(6),
            PO12DrumVoice.TOM_HIGH to listOf(7)
        )

        // Should handle > 5 voices by truncating or showing subset
        renderer.renderCompactGrid(voices)
        // Should not crash with many voices
    }

    @Test
    fun `should show voice names in summary`() {
        val terminal = Terminal()
        val renderer = MultiVoiceRenderer(terminal)
        val voice = PO12DrumVoice.KICK
        val steps = listOf(1, 5, 9, 13)

        val summary = renderer.renderVoiceSummary(voice, steps)

        // Should include voice display name and steps
        assertTrue(summary.contains("Kick") || summary.contains("Bass Drum"))
        assertTrue(summary.contains("1"))
        assertTrue(summary.contains("5"))
        assertTrue(summary.contains("9"))
        assertTrue(summary.contains("13"))
    }

    @Test
    fun `should render voice summary with step count`() {
        val terminal = Terminal()
        val renderer = MultiVoiceRenderer(terminal)
        val voice = PO12DrumVoice.CLOSED_HH
        val steps = listOf(1, 3, 5, 7, 9, 11, 13, 15)

        val summary = renderer.renderVoiceSummary(voice, steps)

        // Summary should contain voice info
        assertTrue(summary.isNotEmpty())
        assertTrue(summary.contains("Closed Hi-Hat") || summary.contains("HH"))
    }

    @Test
    fun `should handle voices with no active steps`() {
        val terminal = Terminal()
        val renderer = MultiVoiceRenderer(terminal)
        val voices = mapOf(
            PO12DrumVoice.KICK to emptyList<Int>()
        )

        // Should handle empty step list gracefully
        renderer.renderCompactGrid(voices)
        // No exception = success
    }

    @Test
    fun `should render combined step visualization`() {
        val terminal = Terminal()
        val renderer = MultiVoiceRenderer(terminal)
        val voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.SNARE to listOf(5, 13)
        )

        // Should show combined view of all voices
        renderer.renderCombinedSteps(voices)
        // Implementation will handle rendering logic
    }

    @Test
    fun `should handle empty voices map in combined visualization`() {
        val terminal = Terminal()
        val renderer = MultiVoiceRenderer(terminal)
        val voices = emptyMap<PO12DrumVoice, List<Int>>()

        // Should handle empty map without errors
        renderer.renderCombinedSteps(voices)
        // No exception = success
    }
}
