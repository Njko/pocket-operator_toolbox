package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.*
import java.io.File
import java.time.LocalDate

class MarkdownParser {

    /**
     * Parses a markdown pattern file and returns the matching pattern type,
     * dispatched by the `device` frontmatter field (defaults to PO-12 for
     * files saved before that field existed).
     */
    fun parse(file: File): AnyPattern {
        val content = file.readText()
        val lines = content.lines()

        return if (parseDeviceModel(lines) == "PO-14") {
            parseMelodic(file, lines)
        } else {
            parseDrum(lines)
        }
    }

    private fun parseDrum(lines: List<String>): PO12Pattern {
        val metadata = parseFrontMatter(lines)
        val voices = parseVoices(lines)
        val patternNumber = parsePatternNumber(lines)

        return PO12Pattern(
            voices = voices,
            metadata = metadata,
            number = patternNumber
        )
    }

    private fun parseMelodic(file: File, lines: List<String>): PO14Pattern {
        val metadata = parseFrontMatter(lines)
        val patternNumber = parsePatternNumber(lines)
        val sound = parseSound(lines)
            ?: throw IllegalArgumentException("Missing or unrecognized Sound in ${file.name}")
        val steps = parseMelodicSteps(lines)

        return PO14Pattern(
            steps = steps,
            sound = sound,
            metadata = metadata,
            number = patternNumber
        )
    }

    private fun parseDeviceModel(lines: List<String>): String {
        lines.forEach { line ->
            val match = Regex("""^device:\s*"?([^"]+)"?\s*$""").find(line.trim())
            if (match != null) return match.groupValues[1].trim()
        }
        return "PO-12"
    }

    private fun parseSound(lines: List<String>): POVoice? {
        val line = lines.find { it.trim().startsWith("**Sound:**") } ?: return null
        val match = Regex("""\(Sound (\d+)\)""").find(line) ?: return null
        return PODevice.PO_14.getVoiceByNumber(match.groupValues[1].toInt())
    }

    private fun parseMelodicSteps(lines: List<String>): Map<Int, PO14Step> {
        val noteLine = lines.find { it.trim().startsWith("Note:") } ?: return emptyMap()
        val tokens = noteLine.substringAfter("Note:").trim().split(Regex("\\s+")).filter { it.isNotBlank() }

        val steps = mutableMapOf<Int, PO14Step>()
        tokens.forEachIndexed { index, token ->
            val step = index + 1
            if (step !in 1..16 || token == ".") return@forEachIndexed
            val match = Regex("""^([A-Ga-g])(#)?(\d+)$""").find(token) ?: return@forEachIndexed
            val (letter, sharp, octaveText) = match.destructured
            val pitch = Pitch.fromLetter(letter) ?: return@forEachIndexed
            steps[step] = PO14Step(pitch = pitch, octave = octaveText.toInt(), halfToneUp = sharp == "#")
        }
        return steps
    }

    private fun parseFrontMatter(lines: List<String>): PatternMetadata {
        val frontMatterStart = lines.indexOfFirst { it.trim() == "---" }
        val frontMatterEnd = lines.drop(frontMatterStart + 1).indexOfFirst { it.trim() == "---" } + frontMatterStart + 1

        if (frontMatterStart == -1 || frontMatterEnd == -1) {
            throw IllegalArgumentException("Invalid markdown: missing frontmatter")
        }

        val frontMatter = lines.subList(frontMatterStart + 1, frontMatterEnd)
        val map = mutableMapOf<String, String>()

        frontMatter.forEach { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                map[parts[0].trim()] = parts[1].trim()
            }
        }

        return PatternMetadata(
            name = extractQuotedValue(map["name"]) ?: throw IllegalArgumentException("Missing name in frontmatter"),
            description = extractQuotedValue(map["description"]),
            bpm = map["bpm"]?.toIntOrNull(),
            genre = parseGenreList(map["genre"]),
            difficulty = map["difficulty"]?.let { Difficulty.fromString(it) },
            sourceAttribution = extractQuotedValue(map["source"]),
            author = extractQuotedValue(map["author"]),
            dateCreated = map["date"]?.let { LocalDate.parse(it) } ?: LocalDate.now(),
            deviceModel = extractQuotedValue(map["device"]) ?: "PO-12"
        )
    }

    private fun parseVoices(lines: List<String>): Map<PO12DrumVoice, List<Int>> {
        val voices = mutableMapOf<PO12DrumVoice, List<Int>>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Look for voice headers like "### Bass Drum (Sound 1)"
            if (line.startsWith("###")) {
                val soundNumberMatch = Regex("""Sound (\d+)""").find(line)
                if (soundNumberMatch != null) {
                    val soundNumber = soundNumberMatch.groupValues[1].toInt()
                    val voice = PO12DrumVoice.fromPONumber(soundNumber)

                    if (voice != null) {
                        // Find the grid in the next few lines
                        val steps = parseStepGrid(lines, i)
                        if (steps.isNotEmpty()) {
                            voices[voice] = steps
                        }
                    }
                }
            }
            i++
        }

        return voices
    }

    private fun parseStepGrid(lines: List<String>, startIndex: Int): List<Int> {
        // Look for the grid line after the header (within next 5 lines)
        for (i in startIndex until minOf(startIndex + 10, lines.size)) {
            val line = lines[i]
            if (line.contains("[●]") || line.contains("[ ]")) {
                return extractActiveSteps(line)
            }
        }
        return emptyList()
    }

    private fun extractActiveSteps(gridLine: String): List<Int> {
        val steps = mutableListOf<Int>()

        // Find all [●] and [ ] patterns
        val pattern = Regex("\\[(●| )\\]")
        val matches = pattern.findAll(gridLine)

        var stepNumber = 1
        matches.forEach { match ->
            if (match.value == "[●]") {
                steps.add(stepNumber)
            }
            stepNumber++
        }

        return steps
    }

    private fun parsePatternNumber(lines: List<String>): Int {
        // Look for "pattern_numbers: [1]" in frontmatter
        lines.forEach { line ->
            val match = Regex("""pattern_numbers:\s*\[(\d+)]""").find(line)
            if (match != null) {
                return match.groupValues[1].toInt()
            }
        }
        return 1 // Default to pattern 1
    }

    private fun extractQuotedValue(value: String?): String? {
        if (value == null) return null
        val trimmed = value.trim()
        return if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed.substring(1, trimmed.length - 1)
                .replace("\\\"", "\"")
        } else {
            trimmed
        }
    }

    private fun parseGenreList(value: String?): List<String> {
        if (value == null) return emptyList()
        val trimmed = value.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()

        val content = trimmed.substring(1, trimmed.length - 1)
        return content.split(",")
            .map { extractQuotedValue(it.trim()) ?: "" }
            .filter { it.isNotBlank() }
    }
}
