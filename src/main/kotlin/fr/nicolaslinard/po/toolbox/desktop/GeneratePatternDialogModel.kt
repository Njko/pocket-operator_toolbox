package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.BuiltInTemplates
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import fr.nicolaslinard.po.toolbox.models.PatternTemplate

class GeneratePatternDialogModel {

    val categories: Map<String, String> = BuiltInTemplates.CATEGORIES

    var selectedCategory: String? = null
        private set

    var selectedTemplate: PatternTemplate? = null
        private set

    fun selectCategory(displayName: String): List<PatternTemplate> {
        val categoryKey = categories.entries
            .find { it.value == displayName }?.key
        selectedCategory = categoryKey
        selectedTemplate = null
        return if (categoryKey != null) BuiltInTemplates.byCategory(categoryKey) else emptyList()
    }

    fun selectTemplate(template: PatternTemplate?) {
        selectedTemplate = template
    }

    fun canCreate(): Boolean = selectedTemplate != null

    fun buildPattern(): PO12Pattern? {
        val template = selectedTemplate ?: return null
        return PO12Pattern(
            voices = template.voices,
            metadata = PatternMetadata(
                name = template.name,
                bpm = template.suggestedBPM,
                genre = listOf(
                    categories[template.category] ?: template.category
                ),
                difficulty = template.difficulty
            ),
            number = 1
        )
    }
}
