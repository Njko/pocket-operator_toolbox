package fr.nicolaslinard.po.toolbox.models

/**
 * A pre-built PO-14 bass pattern, mirroring [PatternTemplate] for the PO-12
 * but carrying a note grid instead of a per-voice step grid.
 */
data class Po14PatternTemplate(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: Difficulty,
    val sound: POVoice,
    val steps: Map<Int, PO14Step>,
    val suggestedBPM: Int? = null
)

/**
 * Built-in "classic" bass patterns for the PO-14 Sub, covering the same
 * kind of foundational grooves BuiltInTemplates offers for the PO-12.
 */
object BuiltInPo14Templates {

    private val sine = PODevice.PO_14.getVoiceByShortName("sine")!!
    private val acid = PODevice.PO_14.getVoiceByShortName("acid")!!
    private val subDrop = PODevice.PO_14.getVoiceByShortName("drop")!!

    val OCTAVE_BASSLINE = Po14PatternTemplate(
        id = "octave-bassline",
        name = "Octave Bassline",
        description = "Root note alternating with its octave, the classic four-on-the-floor bass companion",
        difficulty = Difficulty.BEGINNER,
        sound = sine,
        steps = mapOf(
            1 to PO14Step(Pitch.C, 2), 5 to PO14Step(Pitch.C, 3),
            9 to PO14Step(Pitch.C, 2), 13 to PO14Step(Pitch.C, 3)
        ),
        suggestedBPM = 120
    )

    val WALKING_BASS = Po14PatternTemplate(
        id = "walking-bass",
        name = "Simple Walking Bass",
        description = "Root-third-fifth-octave walk, one note per beat",
        difficulty = Difficulty.BEGINNER,
        sound = sine,
        steps = mapOf(
            1 to PO14Step(Pitch.C, 2), 5 to PO14Step(Pitch.E, 2),
            9 to PO14Step(Pitch.G, 2), 13 to PO14Step(Pitch.C, 3)
        ),
        suggestedBPM = 100
    )

    val ACID_16TH = Po14PatternTemplate(
        id = "acid-16th",
        name = "Acid 16th Bassline",
        description = "Syncopated 16th-note acid line with a live half-tone accent",
        difficulty = Difficulty.INTERMEDIATE,
        sound = acid,
        steps = mapOf(
            1 to PO14Step(Pitch.C, 2), 3 to PO14Step(Pitch.C, 2),
            5 to PO14Step(Pitch.C, 2), 7 to PO14Step(Pitch.D, 2, halfToneUp = true),
            8 to PO14Step(Pitch.C, 2), 11 to PO14Step(Pitch.C, 2),
            13 to PO14Step(Pitch.C, 2), 15 to PO14Step(Pitch.A, 1)
        ),
        suggestedBPM = 130
    )

    val SUB_DROP_HITS = Po14PatternTemplate(
        id = "sub-drop-hits",
        name = "Sub Drop Hits",
        description = "Sparse sub-bass hits on the downbeats, for dub/reggae-style patterns",
        difficulty = Difficulty.BEGINNER,
        sound = subDrop,
        steps = mapOf(
            1 to PO14Step(Pitch.C, 1), 9 to PO14Step(Pitch.C, 1)
        ),
        suggestedBPM = 78
    )

    fun all(): List<Po14PatternTemplate> = listOf(OCTAVE_BASSLINE, WALKING_BASS, ACID_16TH, SUB_DROP_HITS)
}
