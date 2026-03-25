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

    // --- Rock ---

    val ROCK_SHUFFLE = PatternTemplate(
        id = "rock-shuffle",
        name = "Rock Shuffle",
        description = "Shuffle rock with swung hi-hats",
        category = "rock",
        difficulty = Difficulty.INTERMEDIATE,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 9),
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 4, 5, 8, 9, 12, 13, 16)
        ),
        suggestedBPM = 130
    )

    val ROCK_HALFTIME = PatternTemplate(
        id = "rock-halftime",
        name = "Rock Half-Time",
        description = "Heavy half-time feel, snare on beat 3",
        category = "rock",
        difficulty = Difficulty.BEGINNER,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5),
            PO12DrumVoice.SNARE to listOf(9),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15)
        ),
        suggestedBPM = 75
    )

    // --- Electronic ---

    val HOUSE_CLASSIC = PatternTemplate(
        id = "house-classic",
        name = "Classic House",
        description = "Classic house beat with open hi-hats on off-beats",
        category = "electronic",
        difficulty = Difficulty.BEGINNER,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.HAND_CLAP to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15),
            PO12DrumVoice.OPEN_HH to listOf(3, 7, 11, 15)
        ),
        suggestedBPM = 124
    )

    val TECHNO_DRIVING = PatternTemplate(
        id = "techno-driving",
        name = "Driving Techno",
        description = "Hard-hitting techno with 16th hi-hats",
        category = "electronic",
        difficulty = Difficulty.INTERMEDIATE,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.HAND_CLAP to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to (1..16).toList(),
            PO12DrumVoice.CYMBAL to listOf(13)
        ),
        suggestedBPM = 138
    )

    val DRUM_AND_BASS = PatternTemplate(
        id = "drum-and-bass",
        name = "Drum & Bass",
        description = "Fast breakbeat with syncopated kick",
        category = "electronic",
        difficulty = Difficulty.ADVANCED,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 7, 10, 14),
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15),
            PO12DrumVoice.OPEN_HH to listOf(4, 12)
        ),
        suggestedBPM = 174
    )

    val TRANCE = PatternTemplate(
        id = "trance",
        name = "Trance",
        description = "Uplifting trance beat with off-beat bass",
        category = "electronic",
        difficulty = Difficulty.INTERMEDIATE,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.HAND_CLAP to listOf(5, 13),
            PO12DrumVoice.OPEN_HH to listOf(3, 7, 11, 15),
            PO12DrumVoice.CYMBAL to listOf(1)
        ),
        suggestedBPM = 140
    )

    // --- Hip-Hop ---

    val HIPHOP_BOOM_BAP = PatternTemplate(
        id = "hiphop-boom-bap",
        name = "Boom Bap",
        description = "Classic 90s hip-hop boom bap groove",
        category = "hip-hop",
        difficulty = Difficulty.INTERMEDIATE,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 8, 11),
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15),
            PO12DrumVoice.OPEN_HH to listOf(9)
        ),
        suggestedBPM = 92
    )

    val TRAP = PatternTemplate(
        id = "trap",
        name = "Trap",
        description = "Modern trap beat with rolling hi-hats",
        category = "hip-hop",
        difficulty = Difficulty.ADVANCED,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 9, 12),
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to (1..16).toList(),
            PO12DrumVoice.OPEN_HH to listOf(8, 16)
        ),
        suggestedBPM = 140
    )

    val LOFI_HIPHOP = PatternTemplate(
        id = "lofi-hiphop",
        name = "Lo-Fi Hip-Hop",
        description = "Laid-back lo-fi beat with swing feel",
        category = "hip-hop",
        difficulty = Difficulty.BEGINNER,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 8),
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 4, 5, 8, 9, 12, 13, 16)
        ),
        suggestedBPM = 80
    )

    // --- Funk / Soul ---

    val FUNK_CLASSIC = PatternTemplate(
        id = "funk-classic",
        name = "Classic Funk",
        description = "Syncopated funk groove à la James Brown",
        category = "funk-soul",
        difficulty = Difficulty.INTERMEDIATE,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 7, 11, 13),
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to (1..16).toList()
        ),
        suggestedBPM = 110
    )

    val DISCO = PatternTemplate(
        id = "disco",
        name = "Disco",
        description = "Classic disco groove with open hi-hats",
        category = "funk-soul",
        difficulty = Difficulty.BEGINNER,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.SNARE to listOf(5, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15),
            PO12DrumVoice.OPEN_HH to listOf(3, 7, 11, 15)
        ),
        suggestedBPM = 115
    )

    // --- Latin ---

    val BOSSA_NOVA = PatternTemplate(
        id = "bossa-nova",
        name = "Bossa Nova",
        description = "Brazilian bossa nova rhythm",
        category = "latin",
        difficulty = Difficulty.INTERMEDIATE,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 7, 10),
            PO12DrumVoice.RIM_SHOT to listOf(4, 7, 10, 13, 16),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15)
        ),
        suggestedBPM = 140
    )

    val REGGAETON = PatternTemplate(
        id = "reggaeton",
        name = "Reggaeton",
        description = "Dembow riddim pattern",
        category = "latin",
        difficulty = Difficulty.BEGINNER,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.SNARE to listOf(4, 8, 12, 16),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15)
        ),
        suggestedBPM = 95
    )

    val SAMBA = PatternTemplate(
        id = "samba",
        name = "Samba",
        description = "Brazilian samba groove",
        category = "latin",
        difficulty = Difficulty.ADVANCED,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 8, 13),
            PO12DrumVoice.SNARE to listOf(3, 7, 11, 15),
            PO12DrumVoice.CLOSED_HH to (1..16).toList(),
            PO12DrumVoice.RIM_SHOT to listOf(5, 13)
        ),
        suggestedBPM = 100
    )

    // --- Reggae / Dub ---

    val REGGAE_ONE_DROP = PatternTemplate(
        id = "reggae-one-drop",
        name = "Reggae One Drop",
        description = "Classic one drop reggae rhythm",
        category = "reggae",
        difficulty = Difficulty.BEGINNER,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(9),
            PO12DrumVoice.SNARE to listOf(9),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15),
            PO12DrumVoice.RIM_SHOT to listOf(5, 13)
        ),
        suggestedBPM = 78
    )

    val DUB = PatternTemplate(
        id = "dub",
        name = "Dub",
        description = "Heavy dub rhythm with sparse kick",
        category = "reggae",
        difficulty = Difficulty.INTERMEDIATE,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 12),
            PO12DrumVoice.SNARE to listOf(9),
            PO12DrumVoice.RIM_SHOT to listOf(5, 13),
            PO12DrumVoice.OPEN_HH to listOf(3, 7, 11, 15)
        ),
        suggestedBPM = 72
    )

    // --- Jazz ---

    val JAZZ_SWING = PatternTemplate(
        id = "jazz-swing",
        name = "Jazz Swing",
        description = "Swing jazz ride pattern with kick comps",
        category = "jazz",
        difficulty = Difficulty.INTERMEDIATE,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 10),
            PO12DrumVoice.CYMBAL to listOf(1, 4, 5, 8, 9, 12, 13, 16),
            PO12DrumVoice.CLOSED_HH to listOf(5, 13)
        ),
        suggestedBPM = 160
    )

    // --- Afro ---

    val AFROBEAT = PatternTemplate(
        id = "afrobeat",
        name = "Afrobeat",
        description = "West African afrobeat groove à la Tony Allen",
        category = "afro",
        difficulty = Difficulty.ADVANCED,
        voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 11, 14),
            PO12DrumVoice.SNARE to listOf(9),
            PO12DrumVoice.CLOSED_HH to (1..16).toList(),
            PO12DrumVoice.OPEN_HH to listOf(3, 7),
            PO12DrumVoice.STICKS to listOf(1, 4, 7, 10, 13, 16)
        ),
        suggestedBPM = 115
    )

    /**
     * All categories with display labels.
     */
    val CATEGORIES = linkedMapOf(
        "foundation" to "Fondamentaux",
        "rock" to "Rock",
        "electronic" to "Électronique",
        "hip-hop" to "Hip-Hop",
        "funk-soul" to "Funk / Soul",
        "latin" to "Latin",
        "reggae" to "Reggae / Dub",
        "jazz" to "Jazz",
        "afro" to "Afro"
    )

    fun all(): List<PatternTemplate> {
        return listOf(
            // Fondamentaux
            FOUR_ON_FLOOR, BASIC_ROCK, BASIC_BREAKBEAT, BASIC_HIPHOP, BASIC_TECHNO,
            // Rock
            ROCK_SHUFFLE, ROCK_HALFTIME,
            // Electronic
            HOUSE_CLASSIC, TECHNO_DRIVING, DRUM_AND_BASS, TRANCE,
            // Hip-Hop
            HIPHOP_BOOM_BAP, TRAP, LOFI_HIPHOP,
            // Funk / Soul
            FUNK_CLASSIC, DISCO,
            // Latin
            BOSSA_NOVA, REGGAETON, SAMBA,
            // Reggae / Dub
            REGGAE_ONE_DROP, DUB,
            // Jazz
            JAZZ_SWING,
            // Afro
            AFROBEAT
        )
    }

    fun byCategory(category: String): List<PatternTemplate> {
        return all().filter { it.category == category }
    }

    fun byDifficulty(difficulty: Difficulty): List<PatternTemplate> {
        return all().filter { it.difficulty == difficulty }
    }
}
