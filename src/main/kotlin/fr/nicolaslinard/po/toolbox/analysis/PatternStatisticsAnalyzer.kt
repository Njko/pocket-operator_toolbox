package fr.nicolaslinard.po.toolbox.analysis

import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import kotlin.math.abs

/**
 * Analyzes statistics and metrics for drum patterns and pattern libraries.
 * Provides insights into rhythm complexity, voice usage, and pattern characteristics.
 */
class PatternStatisticsAnalyzer {

    /**
     * Analyze a single pattern and return comprehensive statistics.
     */
    fun analyze(pattern: PO12Pattern): PatternStatistics {
        val totalNotes = pattern.voices.values.sumOf { it.size }
        val voiceCount = pattern.voices.size
        val activeSteps = pattern.voices.values.flatten().toSet().size
        val density = if (totalNotes > 0) totalNotes.toDouble() / (voiceCount * 16) else 0.0
        val stepCoverage = activeSteps.toDouble() / 16.0
        val complexity = calculateComplexity(pattern)

        val voiceUsage = pattern.voices.mapValues { (_, steps) -> steps.size }

        return PatternStatistics(
            totalNotes = totalNotes,
            voiceCount = voiceCount,
            activeSteps = activeSteps,
            density = density,
            stepCoverage = stepCoverage,
            complexity = complexity,
            voiceUsage = voiceUsage
        )
    }

    /**
     * Calculate note density (notes per available slot).
     */
    fun calculateDensity(pattern: PO12Pattern): Double {
        val totalNotes = pattern.voices.values.sumOf { it.size }
        val voiceCount = pattern.voices.size

        if (voiceCount == 0) return 0.0

        return totalNotes.toDouble() / (voiceCount * 16)
    }

    /**
     * Calculate rhythm complexity score (0.0-1.0).
     * Based on number of voices, note distribution, and syncopation.
     */
    fun calculateComplexity(pattern: PO12Pattern): Double {
        if (pattern.voices.isEmpty()) return 0.0

        val voiceComplexity = pattern.voices.size.toDouble() / 16.0 // More voices = more complex
        val densityComplexity = calculateDensity(pattern) // More notes = more complex
        val syncopationComplexity = calculateSyncopation(pattern)

        return (voiceComplexity * 0.3 + densityComplexity * 0.4 + syncopationComplexity * 0.3)
            .coerceIn(0.0, 1.0)
    }

    /**
     * Calculate syncopation score (0.0-1.0).
     * Higher score means more off-beat rhythms.
     */
    fun calculateSyncopation(pattern: PO12Pattern): Double {
        val allSteps = pattern.voices.values.flatten()
        if (allSteps.isEmpty()) return 0.0

        // Strong beats are 1, 5, 9, 13 (quarter notes)
        val strongBeats = setOf(1, 5, 9, 13)
        val weakBeats = (1..16).toSet() - strongBeats

        val notesOnStrongBeats = allSteps.count { it in strongBeats }
        val notesOnWeakBeats = allSteps.count { it in weakBeats }

        // More notes on weak beats = more syncopation
        return notesOnWeakBeats.toDouble() / allSteps.size.toDouble()
    }

    /**
     * Detect if pattern has four-on-the-floor kick pattern.
     * Kick hits on beats 1, 5, 9, 13 (every quarter note).
     */
    fun isFourOnTheFloor(pattern: PO12Pattern): Boolean {
        val kickSteps = pattern.voices[PO12DrumVoice.KICK]?.toSet() ?: return false
        val fourOnFloorSteps = setOf(1, 5, 9, 13)

        return kickSteps.containsAll(fourOnFloorSteps)
    }

    /**
     * Detect if pattern has breakbeat characteristics.
     * Kick and snare with syncopated placement.
     */
    fun hasBreakbeatCharacteristics(pattern: PO12Pattern): Boolean {
        val kickSteps = pattern.voices[PO12DrumVoice.KICK] ?: return false
        val snareSteps = pattern.voices[PO12DrumVoice.SNARE] ?: return false

        // Breakbeats typically have:
        // - Kick and snare present
        // - Snare on backbeats (5, 13) or close
        // - Some syncopation

        if (kickSteps.isEmpty() || snareSteps.isEmpty()) return false

        val syncopation = calculateSyncopation(pattern)
        return syncopation > 0.3 // At least some syncopation
    }

    /**
     * Analyze a library of patterns and return aggregate statistics.
     */
    fun analyzeLibrary(library: List<PO12Pattern>): LibraryStatistics {
        if (library.isEmpty()) {
            return LibraryStatistics(
                totalPatterns = 0,
                averageDensity = 0.0,
                averageComplexity = 0.0,
                mostUsedVoices = emptyList(),
                bpmRange = BpmRange(0.0, 0.0, 0.0)
            )
        }

        val densities = library.map { calculateDensity(it) }
        val complexities = library.map { calculateComplexity(it) }

        // Count voice usage across all patterns
        val voiceFrequency = mutableMapOf<PO12DrumVoice, Int>()
        library.forEach { pattern ->
            pattern.voices.keys.forEach { voice ->
                voiceFrequency[voice] = (voiceFrequency[voice] ?: 0) + 1
            }
        }

        val mostUsedVoices = voiceFrequency.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }

        // Calculate BPM statistics
        val bpms = library.mapNotNull { it.metadata.bpm?.toDouble() }
        val bpmRange = if (bpms.isNotEmpty()) {
            BpmRange(
                min = bpms.minOrNull() ?: 0.0,
                max = bpms.maxOrNull() ?: 0.0,
                average = bpms.average()
            )
        } else {
            BpmRange(0.0, 0.0, 0.0)
        }

        return LibraryStatistics(
            totalPatterns = library.size,
            averageDensity = densities.average(),
            averageComplexity = complexities.average(),
            mostUsedVoices = mostUsedVoices,
            bpmRange = bpmRange
        )
    }

    /**
     * Calculate what percentile a pattern's density falls into within a library.
     * Returns value 0-100.
     */
    fun calculateDensityPercentile(pattern: PO12Pattern, library: List<PO12Pattern>): Double {
        if (library.isEmpty()) return 50.0

        val patternDensity = calculateDensity(pattern)
        val lowerCount = library.count { calculateDensity(it) < patternDensity }

        return (lowerCount.toDouble() / library.size.toDouble()) * 100.0
    }
}

/**
 * Statistics for a single pattern.
 */
data class PatternStatistics(
    val totalNotes: Int,
    val voiceCount: Int,
    val activeSteps: Int,
    val density: Double,
    val stepCoverage: Double,
    val complexity: Double,
    val voiceUsage: Map<PO12DrumVoice, Int>
)

/**
 * Aggregate statistics for a pattern library.
 */
data class LibraryStatistics(
    val totalPatterns: Int,
    val averageDensity: Double,
    val averageComplexity: Double,
    val mostUsedVoices: List<PO12DrumVoice>,
    val bpmRange: BpmRange
)

/**
 * BPM range and average for a library.
 */
data class BpmRange(
    val min: Double,
    val max: Double,
    val average: Double
)
