package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.AnyPattern
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
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
        appendLine("device: \"${metadata.deviceModel}\"")
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

    /**
     * Writes a PO-14 pattern to a markdown file.
     * Returns the file that was created.
     */
    fun write(pattern: PO14Pattern, outputDir: File): File {
        val fileName = generateFileName(pattern.metadata.name)
        val file = File(outputDir, fileName)

        val content = buildString {
            appendFrontMatter(pattern.metadata, pattern.number)
            appendLine()
            appendTitle(pattern.metadata.name)
            appendDescription(pattern.metadata.description)
            appendLine()
            appendMelodicPatternSection(pattern)
            appendLine()
            appendMelodicProgrammingInstructions(pattern)
            appendNotes(pattern.metadata)
        }

        file.parentFile?.mkdirs()
        file.writeText(content)

        return file
    }

    /**
     * Writes any supported pattern type, dispatching to the matching overload.
     */
    fun write(pattern: AnyPattern, outputDir: File): File = when (pattern) {
        is PO12Pattern -> write(pattern, outputDir)
        is PO14Pattern -> write(pattern, outputDir)
    }

    private fun StringBuilder.appendMelodicPatternSection(pattern: PO14Pattern) {
        appendLine("## Pattern ${pattern.number}")
        appendLine()
        appendLine("**Sound:** ${pattern.sound.displayName} (Sound ${pattern.sound.number})")
        appendLine()
        appendLine("```")
        append("Step: ")
        for (i in 1..16) append(String.format("%4d", i))
        appendLine()
        append("Note: ")
        for (i in 1..16) {
            val text = pattern.getNote(i)?.noteText ?: "."
            append(String.format("%4s", text))
        }
        appendLine()
        appendLine("```")
        appendLine()
    }

    private fun StringBuilder.appendMelodicProgrammingInstructions(pattern: PO14Pattern) {
        appendLine("## PO-14 Programming Instructions")
        appendLine()
        appendLine("1. Select Pattern ${pattern.number} on your PO-14")
        appendLine("2. Hold Sound and press ${pattern.sound.number} to select ${pattern.sound.displayName}")
        appendLine("3. Press Write to enter recording mode")

        val activeSteps = pattern.steps.entries.sortedBy { it.key }
        if (activeSteps.isNotEmpty()) {
            appendLine("4. For each active step, hold the step and turn knob A to set its pitch:")
            activeSteps.forEach { (step, note) ->
                appendLine("   - Step $step: ${note.pitch.letter}${note.octave}")
            }
        }
        appendLine("5. Press Write again to exit recording mode")

        val sharpSteps = activeSteps.filter { it.value.halfToneUp }
        if (sharpSteps.isNotEmpty()) {
            appendLine("6. These steps need a black key (half note up). This can't be dialed in with knob A: " +
                "during playback, hold Style/FX and tap 15 (\"half note up\") exactly on each of these steps, " +
                "or punch it in live by holding Write and tapping the step at the right moment:")
            sharpSteps.forEach { (step, note) ->
                appendLine("   - Step $step -> ${note.noteText}")
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
