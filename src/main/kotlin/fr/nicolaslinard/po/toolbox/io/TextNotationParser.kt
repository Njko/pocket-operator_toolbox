package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice

/**
 * Parses text notation for quick pattern entry.
 * Format: "voice: step1,step2,step3" or "voice: step1 step2 step3"
 *
 * Examples:
 *   kick: 1,5,9,13
 *   snare: 3 7 11 15
 *   closed-hh: 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16
 */
class TextNotationParser {

    /**
     * Parses a single line of text notation.
     * Returns a Pair of (PO12DrumVoice, List<Int>) or null if parsing fails.
     */
    fun parseLine(line: String): Pair<PO12DrumVoice, List<Int>>? {
        val trimmed = line.trim()
        if (trimmed.isBlank() || trimmed.startsWith("#")) {
            return null // Skip empty lines and comments
        }

        val parts = trimmed.split(":", limit = 2)
        if (parts.size != 2) {
            return null
        }

        val voiceName = parts[0].trim()
        val stepsStr = parts[1].trim()

        // Try to find voice by short name
        val voice = PO12DrumVoice.fromShortName(voiceName) ?: return null

        // Parse steps
        val steps = parseSteps(stepsStr) ?: return null

        return Pair(voice, steps)
    }

    /**
     * Parses multiple lines of text notation.
     * Returns a map of PO12DrumVoice to step lists.
     */
    fun parseMultiLine(text: String): Map<PO12DrumVoice, List<Int>> {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()

        text.lines().forEach { line ->
            parseLine(line)?.let { (voice, steps) ->
                voices[voice] = steps
            }
        }

        return voices
    }

    /**
     * Parses a file containing text notation.
     */
    fun parseFile(file: java.io.File): Map<PO12DrumVoice, List<Int>> {
        return parseMultiLine(file.readText())
    }

    private fun parseSteps(stepsStr: String): List<Int>? {
        // Split by spaces, commas, or both
        val parts = stepsStr.split(Regex("[,\\s]+"))
            .filter { it.isNotBlank() }

        val steps = mutableListOf<Int>()

        for (part in parts) {
            val num = part.toIntOrNull() ?: return null
            if (num !in 1..16) return null
            steps.add(num)
        }

        return steps.distinct().sorted()
    }

    /**
     * Formats a pattern to text notation.
     */
    fun format(voices: Map<PO12DrumVoice, List<Int>>): String {
        return voices.entries
            .sortedBy { it.key.poNumber }
            .joinToString("\n") { (voice, steps) ->
                "${voice.shortName}: ${steps.joinToString(", ")}"
            }
    }
}
