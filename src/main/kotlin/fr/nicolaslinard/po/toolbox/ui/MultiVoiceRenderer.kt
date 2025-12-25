package fr.nicolaslinard.po.toolbox.ui

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice

/**
 * REFACTOR Phase - Improved implementation with extracted constants
 * Renders compact multi-voice preview during pattern creation
 */
class MultiVoiceRenderer(private val terminal: Terminal) {

    companion object {
        private const val MAX_VOICES_TO_SHOW = 5
        private const val VOICE_NAME_WIDTH = 6
        private const val STEP_COUNT = 16
    }

    /**
     * Renders a compact grid showing multiple voices simultaneously.
     * Limits display to MAX_VOICES_TO_SHOW to prevent clutter.
     */
    fun renderCompactGrid(voices: Map<PO12DrumVoice, List<Int>>) {
        if (voices.isEmpty()) {
            return  // Nothing to render
        }

        terminal.println((bold)("Current Pattern:"))
        terminal.println()

        val voicesToShow = voices.entries.take(MAX_VOICES_TO_SHOW)

        for ((voice, steps) in voicesToShow) {
            val summary = renderVoiceSummary(voice, steps)
            terminal.println(summary)
        }

        if (voices.size > MAX_VOICES_TO_SHOW) {
            terminal.println((dim)("... and ${voices.size - MAX_VOICES_TO_SHOW} more voice(s)"))
        }

        terminal.println()
    }

    /**
     * Renders a single line summary for a voice.
     * Format: "Voice Name: 1, 5, 9, 13"
     */
    fun renderVoiceSummary(voice: PO12DrumVoice, steps: List<Int>): String {
        val stepsStr = if (steps.isEmpty()) {
            "(no steps)"
        } else {
            steps.joinToString(", ")
        }
        return "${voice.displayName}: $stepsStr"
    }

    /**
     * Renders a combined visualization showing all voices on a grid.
     */
    fun renderCombinedSteps(voices: Map<PO12DrumVoice, List<Int>>) {
        if (voices.isEmpty()) {
            return  // Nothing to render
        }

        terminal.println((bold)("Combined Grid:"))
        terminal.println()

        // Step numbers header
        renderStepNumbersHeader()

        // Each voice as a row
        val voicesToShow = voices.entries.take(MAX_VOICES_TO_SHOW)
        for ((voice, steps) in voicesToShow) {
            renderVoiceGridRow(voice, steps)
        }

        renderMoreVoicesIndicator(voices.size)
        terminal.println()
    }

    private fun renderStepNumbersHeader() {
        terminal.print("Step:  ")
        for (i in 1..STEP_COUNT) {
            terminal.print(String.format("%2d  ", i))
        }
        terminal.println()
    }

    private fun renderVoiceGridRow(voice: PO12DrumVoice, steps: List<Int>) {
        // Voice name (truncate to fit)
        val name = voice.displayName.take(VOICE_NAME_WIDTH).padEnd(VOICE_NAME_WIDTH)
        terminal.print("$name ")

        // Grid for this voice
        for (i in 1..STEP_COUNT) {
            val isActive = i in steps
            val stepDisplay = if (isActive) "[●]" else "[ ]"
            val styled = if (isActive) (green)(stepDisplay) else (dim)(stepDisplay)
            terminal.print(styled)
            terminal.print(" ")
        }
        terminal.println()
    }

    private fun renderMoreVoicesIndicator(totalVoices: Int) {
        if (totalVoices > MAX_VOICES_TO_SHOW) {
            terminal.println((dim)("... and ${totalVoices - MAX_VOICES_TO_SHOW} more voice(s)"))
        }
    }
}
