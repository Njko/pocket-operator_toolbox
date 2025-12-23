package fr.nicolaslinard.po.toolbox.analysis

import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import kotlin.math.abs

/**
 * Analyzes similarity between drum patterns.
 * Provides multiple similarity metrics for pattern comparison and search.
 */
class PatternSimilarityAnalyzer {

    /**
     * Calculate overall similarity between two patterns.
     * Uses weighted combination of voice, step, and rhythm similarity.
     */
    fun calculateSimilarity(
        pattern1: PO12Pattern,
        pattern2: PO12Pattern,
        weights: SimilarityWeights = SimilarityWeights()
    ): Double {
        val voiceSim = calculateVoiceSimilarity(pattern1, pattern2)
        val stepSim = calculateStepSimilarity(pattern1, pattern2)
        val rhythmSim = calculateRhythmSimilarity(pattern1, pattern2)

        return (voiceSim * weights.voiceWeight) +
               (stepSim * weights.stepWeight) +
               (rhythmSim * weights.rhythmWeight)
    }

    /**
     * Calculate similarity based on which voices are used.
     * Returns ratio of common voices to total voices.
     */
    fun calculateVoiceSimilarity(pattern1: PO12Pattern, pattern2: PO12Pattern): Double {
        if (pattern1.voices.isEmpty() && pattern2.voices.isEmpty()) {
            return 1.0
        }
        if (pattern1.voices.isEmpty() || pattern2.voices.isEmpty()) {
            return 0.0
        }

        val voices1 = pattern1.voices.keys
        val voices2 = pattern2.voices.keys
        val intersection = voices1.intersect(voices2).size
        val union = voices1.union(voices2).size

        return intersection.toDouble() / union.toDouble()
    }

    /**
     * Calculate similarity based on step placements for matching voices.
     * Averages step similarity across common voices.
     */
    fun calculateStepSimilarity(pattern1: PO12Pattern, pattern2: PO12Pattern): Double {
        if (pattern1.voices.isEmpty() && pattern2.voices.isEmpty()) {
            return 1.0
        }
        if (pattern1.voices.isEmpty() || pattern2.voices.isEmpty()) {
            return 0.0
        }

        // Find common voices
        val commonVoices = pattern1.voices.keys.intersect(pattern2.voices.keys)

        if (commonVoices.isEmpty()) {
            return 0.0
        }

        // Calculate average similarity across common voices
        val similarities = commonVoices.map { voice ->
            val steps1 = pattern1.voices[voice]?.toSet() ?: emptySet()
            val steps2 = pattern2.voices[voice]?.toSet() ?: emptySet()

            // Calculate overlap coefficient: intersection / min(|set1|, |set2|)
            val matchingSteps = steps1.intersect(steps2).size
            val maxPossible = maxOf(steps1.size, steps2.size)

            if (maxPossible == 0) 1.0 else matchingSteps.toDouble() / maxPossible.toDouble()
        }

        return similarities.average()
    }

    /**
     * Calculate similarity based on rhythm patterns.
     * Compares step placement patterns regardless of which voice plays them.
     */
    fun calculateRhythmSimilarity(pattern1: PO12Pattern, pattern2: PO12Pattern): Double {
        if (pattern1.voices.isEmpty() && pattern2.voices.isEmpty()) {
            return 1.0
        }
        if (pattern1.voices.isEmpty() || pattern2.voices.isEmpty()) {
            return 0.0
        }

        // Create rhythm signature: which steps have any notes
        val rhythmSignature1 = createRhythmSignature(pattern1)
        val rhythmSignature2 = createRhythmSignature(pattern2)

        return jaccardSimilarity(rhythmSignature1, rhythmSignature2)
    }

    /**
     * Calculate similarity based on note density (total number of notes).
     */
    fun calculateDensitySimilarity(pattern1: PO12Pattern, pattern2: PO12Pattern): Double {
        val count1 = pattern1.voices.values.sumOf { it.size }
        val count2 = pattern2.voices.values.sumOf { it.size }

        if (count1 == 0 && count2 == 0) {
            return 1.0
        }

        val maxCount = maxOf(count1, count2)
        val minCount = minOf(count1, count2)

        return minCount.toDouble() / maxCount.toDouble()
    }

    /**
     * Find similar patterns in a collection.
     * Returns patterns sorted by similarity (descending).
     */
    fun findSimilar(
        target: PO12Pattern,
        library: List<PO12Pattern>,
        threshold: Double = 0.5,
        weights: SimilarityWeights = SimilarityWeights()
    ): List<SimilarityResult> {
        return library
            .map { pattern ->
                SimilarityResult(pattern, calculateSimilarity(target, pattern, weights))
            }
            .filter { it.similarity >= threshold }
            .sortedByDescending { it.similarity }
    }

    /**
     * Calculate Jaccard similarity between two sets.
     * Jaccard = |intersection| / |union|
     */
    fun jaccardSimilarity(set1: Set<Int>, set2: Set<Int>): Double {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0
        }
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0
        }

        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size

        return intersection.toDouble() / union.toDouble()
    }

    /**
     * Create rhythm signature for a pattern.
     * Returns set of steps that have at least one note.
     */
    private fun createRhythmSignature(pattern: PO12Pattern): Set<Int> {
        return pattern.voices.values.flatten().toSet()
    }
}

/**
 * Result of a similarity comparison.
 */
data class SimilarityResult(
    val pattern: PO12Pattern,
    val similarity: Double
)

/**
 * Weights for different similarity components.
 * All weights should sum to 1.0.
 */
data class SimilarityWeights(
    val voiceWeight: Double = 0.4,
    val stepWeight: Double = 0.4,
    val rhythmWeight: Double = 0.2
) {
    /**
     * Validate that weights sum to 1.0 (within tolerance).
     */
    fun isValid(): Boolean {
        val sum = voiceWeight + stepWeight + rhythmWeight
        return abs(sum - 1.0) < 0.001
    }
}
