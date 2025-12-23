package fr.nicolaslinard.po.toolbox.models

/**
 * Represents a pattern specific to the Pocket Operator PO-12 (Rhythm).
 * PO-12 has 16 patterns, each with 16 steps.
 */
data class PO12Pattern(
    val voices: Map<PO12DrumVoice, List<Int>>,  // PO12DrumVoice -> active steps (1-16)
    val metadata: PatternMetadata,
    val number: Int = 1                          // PO-12 pattern number (1-16)
) {
    init {
        require(number in 1..16) { "Pattern number must be between 1 and 16" }
        voices.values.forEach { steps ->
            require(steps.all { it in 1..16 }) { "All steps must be between 1 and 16" }
        }
    }

    fun getActiveSteps(voice: PO12DrumVoice): List<Int> = voices[voice] ?: emptyList()

    fun hasVoice(voice: PO12DrumVoice): Boolean = voices.containsKey(voice)
}
