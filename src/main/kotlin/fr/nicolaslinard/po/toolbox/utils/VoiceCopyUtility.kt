package fr.nicolaslinard.po.toolbox.utils

import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import java.io.File

/**
 * GREEN Phase - Minimal implementation to pass tests
 *
 * Utility for copying voices between patterns.
 * Enables loading voices from existing pattern files.
 */
class VoiceCopyUtility(private val patternsDirectory: File) {

    private val parser = MarkdownParser()

    /**
     * List all available patterns in the directory
     */
    fun listAvailablePatterns(): List<PatternSummary> {
        if (!patternsDirectory.exists() || !patternsDirectory.isDirectory) {
            return emptyList()
        }

        return patternsDirectory.listFiles { file ->
            file.extension == "md"
        }?.mapNotNull { file ->
            try {
                val pattern = parser.parse(file)
                PatternSummary(
                    file = file,
                    name = pattern.metadata.name,
                    voices = pattern.voices.keys.toList()
                )
            } catch (e: Exception) {
                // Skip corrupt files
                null
            }
        } ?: emptyList()
    }

    /**
     * Load a specific voice from a pattern file
     * Returns null if voice not found in pattern
     */
    fun loadVoiceFromPattern(file: File, voice: PO12DrumVoice): List<Int>? {
        return try {
            val pattern = parser.parse(file)
            pattern.voices[voice]
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Copy a voice from source pattern to target pattern
     * Can copy to same or different voice
     */
    fun copyVoiceBetweenPatterns(
        sourceFile: File,
        sourceVoice: PO12DrumVoice,
        targetPattern: MutableMap<PO12DrumVoice, List<Int>>,
        targetVoice: PO12DrumVoice
    ) {
        val steps = loadVoiceFromPattern(sourceFile, sourceVoice)
        if (steps != null) {
            targetPattern[targetVoice] = steps
        }
    }
}

/**
 * Summary information about a pattern file
 */
data class PatternSummary(
    val file: File,
    val name: String,
    val voices: List<PO12DrumVoice>
)
