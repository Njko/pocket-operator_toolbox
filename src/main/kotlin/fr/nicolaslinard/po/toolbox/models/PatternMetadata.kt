package fr.nicolaslinard.po.toolbox.models

import java.time.LocalDate

data class PatternMetadata(
    val name: String,
    val description: String? = null,
    val bpm: Int? = null,
    val genre: List<String> = emptyList(),
    val difficulty: Difficulty? = null,
    val sourceAttribution: String? = null,
    val author: String? = null,
    val dateCreated: LocalDate = LocalDate.now()
)

enum class Difficulty(val displayName: String) {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    companion object {
        fun fromString(value: String): Difficulty? =
            entries.find { it.displayName.equals(value, ignoreCase = true) }
    }
}
