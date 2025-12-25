package fr.nicolaslinard.po.toolbox.models

/**
 * RED Phase - Stub implementation for compilation
 *
 * Template for pre-defined drum patterns.
 * Provides common patterns for different genres and styles.
 */
data class PatternTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: String,  // "foundation", "genre", "fill"
    val difficulty: Difficulty,
    val voices: Map<PO12DrumVoice, List<Int>>,
    val suggestedBPM: Int? = null
)

/**
 * GREEN Phase - Minimal implementation to pass tests
 *
 * Built-in pattern templates for common drum patterns
 */
object BuiltInTemplates {

    val FOUR_ON_FLOOR = PatternTemplate(
        id = "four-on-the-floor",
        name = "Four on the Floor",
        description = "Basic house/disco pattern with kick on every beat",
        category = "foundation",
        difficulty = Difficulty.BEGINNER,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15)
        ),
        suggestedBPM = 120
    )

    val BASIC_ROCK = PatternTemplate(
        id = "basic-rock",
        name = "Basic Rock",
        description = "Classic rock beat with kick, snare, and hi-hats",
        category = "genre",
        difficulty = Difficulty.BEGINNER,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 9),
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15)
        ),
        suggestedBPM = 120
    )

    val BASIC_BREAKBEAT = PatternTemplate(
        id = "basic-breakbeat",
        name = "Basic Breakbeat",
        description = "Syncopated breakbeat pattern",
        category = "genre",
        difficulty = Difficulty.INTERMEDIATE,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 7, 11),
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15)
        ),
        suggestedBPM = 140
    )

    val BASIC_HIPHOP = PatternTemplate(
        id = "basic-hiphop",
        name = "Basic Hip-Hop",
        description = "Classic hip-hop groove",
        category = "genre",
        difficulty = Difficulty.BEGINNER,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 11),
            PO12DrumVoice.SNARE to listOf(5, 13)
        ),
        suggestedBPM = 90
    )

    val BASIC_TECHNO = PatternTemplate(
        id = "basic-techno",
        name = "Basic Techno",
        description = "Four-on-the-floor techno with hi-hats and claps",
        category = "genre",
        difficulty = Difficulty.BEGINNER,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.CLOSED_HH to listOf(3, 7, 11, 15),
            PO12DrumVoice.HAND_CLAP to listOf(5, 13)
        ),
        suggestedBPM = 128
    )

    fun all(): List<PatternTemplate> {
        return listOf(
            FOUR_ON_FLOOR,
            BASIC_ROCK,
            BASIC_BREAKBEAT,
            BASIC_HIPHOP,
            BASIC_TECHNO
        )
    }

    fun byCategory(category: String): List<PatternTemplate> {
        return all().filter { it.category == category }
    }

    fun byDifficulty(difficulty: Difficulty): List<PatternTemplate> {
        return all().filter { it.difficulty == difficulty }
    }
}
