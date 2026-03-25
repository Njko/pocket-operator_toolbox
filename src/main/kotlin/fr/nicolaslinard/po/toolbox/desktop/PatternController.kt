package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import fr.nicolaslinard.po.toolbox.io.MarkdownWriter
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import tornadofx.Controller
import java.io.File

class PatternController : Controller() {

    val patterns = FXCollections.observableArrayList<PatternSummary>()
    val selectedPattern = SimpleObjectProperty<PO12Pattern?>()
    val selectedSummary = SimpleObjectProperty<PatternSummary?>()

    private val parser = MarkdownParser()
    private val writer = MarkdownWriter()
    private val patternsDir = File("patterns")

    fun loadPatterns() {
        patterns.clear()
        if (!patternsDir.exists()) return

        patternsDir.listFiles { f -> f.extension == "md" }
            ?.sortedBy { it.name }
            ?.forEach { file ->
                try {
                    val pattern = parser.parse(file)
                    patterns.add(PatternSummary(file, pattern))
                } catch (_: Exception) {
                    // Skip malformed files silently
                }
            }
    }

    fun selectPattern(summary: PatternSummary?) {
        selectedSummary.set(summary)
        selectedPattern.set(summary?.pattern)
    }

    fun createPattern(pattern: PO12Pattern) {
        patternsDir.mkdirs()
        writer.write(pattern, patternsDir)
        loadPatterns()
    }

    fun updatePattern(originalFile: File, updatedPattern: PO12Pattern) {
        originalFile.delete()
        writer.write(updatedPattern, patternsDir)
        loadPatterns()
        // Re-select the updated pattern by name
        val updated = patterns.find { it.name == updatedPattern.metadata.name }
        selectPattern(updated)
    }

    fun deleteSelectedPattern() {
        selectedSummary.value?.file?.delete()
        selectedSummary.set(null)
        selectedPattern.set(null)
        loadPatterns()
    }
}

data class PatternSummary(val file: File, val pattern: PO12Pattern) {
    val name: String get() = pattern.metadata.name
    val bpm: String get() = pattern.metadata.bpm?.toString() ?: "-"
    val genre: String get() = pattern.metadata.genre.joinToString(", ").ifBlank { "-" }
    val difficulty: String get() = pattern.metadata.difficulty?.displayName ?: "-"
    val voiceCount: String get() = pattern.voices.size.toString()
    val patternNumber: String get() = "#${pattern.number}"
}
