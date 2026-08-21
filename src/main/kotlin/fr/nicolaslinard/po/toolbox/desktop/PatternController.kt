package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.AnyPattern
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import tornadofx.Controller
import java.io.File

class PatternController : Controller() {

    val patterns = FXCollections.observableArrayList<PatternSummary>()
    val selectedPattern = SimpleObjectProperty<AnyPattern?>()
    val selectedSummary = SimpleObjectProperty<PatternSummary?>()
    var activeDialogResult: PatternDialogResult? = null

    var repository: PatternRepository = FilePatternRepository()

    fun loadPatterns() {
        patterns.setAll(repository.loadAll())
    }

    fun selectPattern(summary: PatternSummary?) {
        selectedSummary.set(summary)
        selectedPattern.set(summary?.pattern)
        activeDialogResult = null
    }

    fun createPattern(pattern: AnyPattern) {
        repository.save(pattern)
        loadPatterns()
    }

    fun updatePattern(originalFile: File, updatedPattern: AnyPattern) {
        val newFile = repository.update(originalFile, updatedPattern)
        loadPatterns()
        val updated = patterns.find { it.file.absolutePath == newFile.absolutePath }
        selectPattern(updated)
    }

    fun deleteSelectedPattern() {
        val file = selectedSummary.value?.file ?: return
        repository.delete(file)
        selectedSummary.set(null)
        selectedPattern.set(null)
        loadPatterns()
    }
}

data class PatternSummary(val file: File, val pattern: AnyPattern) {
    val name: String get() = pattern.metadata.name
    val bpm: String get() = pattern.metadata.bpm?.toString() ?: "-"
    val genre: String get() = pattern.metadata.genre.joinToString(", ").ifBlank { "-" }
    val difficulty: String get() = pattern.metadata.difficulty?.displayName ?: "-"
    val voiceCount: String get() = when (pattern) {
        is PO12Pattern -> pattern.voices.size.toString()
        is PO14Pattern -> pattern.steps.size.toString()
    }
    val patternNumber: String get() = "#${pattern.number}"
}
