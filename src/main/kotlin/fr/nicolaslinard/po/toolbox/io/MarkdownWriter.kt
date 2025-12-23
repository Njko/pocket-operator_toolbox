package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import java.io.File

class MarkdownWriter {

    /**
     * Writes a PO-12 pattern to a markdown file.
     * Returns the file that was created.
     */
    fun write(pattern: PO12Pattern, outputDir: File): File {
        val fileName = generateFileName(pattern.metadata.name)
        val file = File(outputDir, fileName)

        val content = buildString {
            appendFrontMatter(pattern.metadata, pattern.number)
            appendLine()
            appendTitle(pattern.metadata.name)
            appendDescription(pattern.metadata.description)
            appendLine()
            appendPatternSection(pattern)
            appendLine()
            appendProgrammingInstructions(pattern)
            appendNotes(pattern.metadata)
        }

        file.parentFile?.mkdirs()
        file.writeText(content)

        return file
    }

    private fun StringBuilder.appendFrontMatter(metadata: PatternMetadata, patternNumber: Int) {
        appendLine("---")
        appendLine("name: \"${metadata.name}\"")

        metadata.description?.let {
            appendLine("description: \"${it.replace("\"", "\\\"")}\"")
        }

        metadata.bpm?.let {
            appendLine("bpm: $it")
        }

        if (metadata.genre.isNotEmpty()) {
            appendLine("genre: [${metadata.genre.joinToString(", ") { "\"$it\"" }}]")
        }

        metadata.difficulty?.let {
            appendLine("difficulty: ${it.displayName}")
        }

        metadata.sourceAttribution?.let {
            appendLine("source: \"${it.replace("\"", "\\\"")}\"")
        }

        metadata.author?.let {
            appendLine("author: \"$it\"")
        }

        appendLine("date: ${metadata.dateCreated}")
        appendLine("pattern_numbers: [$patternNumber]")
        appendLine("chain_sequence: null")
        appendLine("---")
    }

    private fun StringBuilder.appendTitle(name: String) {
        appendLine("# $name")
        appendLine()
    }

    private fun StringBuilder.appendDescription(description: String?) {
        description?.let {
            appendLine(it)
            appendLine()
        }
    }

    private fun StringBuilder.appendPatternSection(pattern: PO12Pattern) {
        appendLine("## Pattern ${pattern.number}")
        appendLine()

        // Sort voices by PO number for consistent output
        val sortedVoices = pattern.voices.keys.sortedBy { it.poNumber }

        sortedVoices.forEach { voice ->
            val steps = pattern.getActiveSteps(voice)
            appendLine("### ${voice.displayName} (Sound ${voice.poNumber})")
            appendLine("```")
            appendStepGrid(steps)
            appendLine("```")
            appendLine()
        }
    }

    private fun StringBuilder.appendStepGrid(activeSteps: List<Int>) {
        // Step numbers
        append("Step:  ")
        for (i in 1..16) {
            append(String.format("%2d  ", i))
        }
        appendLine()

        // Grid
        append("      ")
        for (i in 1..16) {
            if (i in activeSteps) {
                append("[●] ")
            } else {
                append("[ ] ")
            }
        }
        appendLine()
    }

    private fun StringBuilder.appendProgrammingInstructions(pattern: PO12Pattern) {
        appendLine("## PO-12 Programming Instructions")
        appendLine()
        appendLine("1. Select Pattern ${pattern.number} on your PO-12")

        var instructionNumber = 2
        pattern.voices.keys.sortedBy { it.poNumber }.forEach { voice ->
            val steps = pattern.getActiveSteps(voice)
            if (steps.isNotEmpty()) {
                appendLine("${instructionNumber}. For ${voice.displayName} (button ${voice.poNumber}):")
                appendLine("   - Press and hold button ${voice.poNumber}")
                appendLine("   - Tap steps: ${steps.joinToString(", ")}")
                instructionNumber++
            }
        }
    }

    private fun StringBuilder.appendNotes(metadata: PatternMetadata) {
        val notes = mutableListOf<String>()

        metadata.bpm?.let {
            notes.add("Set tempo to $it BPM for authentic feel")
        }

        metadata.sourceAttribution?.let {
            notes.add("Original: $it")
        }

        if (notes.isNotEmpty()) {
            appendLine()
            appendLine("## Notes")
            notes.forEach { note ->
                appendLine("- $note")
            }
        }
    }

    private fun generateFileName(name: String): String {
        // Convert name to lowercase, replace spaces and special chars with hyphens
        val slug = name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
        return "$slug.md"
    }
}
