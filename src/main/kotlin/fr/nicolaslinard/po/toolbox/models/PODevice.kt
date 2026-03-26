package fr.nicolaslinard.po.toolbox.models

/**
 * Represents a voice/sound on a Pocket Operator device.
 */
data class POVoice(
    val number: Int,
    val displayName: String,
    val shortName: String
)

/**
 * Type of Pocket Operator device.
 */
enum class DeviceType(val label: String) {
    DRUM_MACHINE("Drum Machine"),
    BASS_SYNTH("Bass Synth"),
    LEAD_SYNTH("Lead Synth"),
    CHIPTUNE("Chiptune"),
    NOISE("Noise/Percussion"),
    SYNTH_8BIT("Synth 8-bit"),
    DRUM_SYNTH("Drum Synth"),
    SAMPLER("Sampler"),
    VOCAL_SYNTH("Vocal Synth")
}

/**
 * Registry of all supported Pocket Operator devices with their voices and MIDI mappings.
 */
enum class PODevice(
    val modelId: String,
    val deviceName: String,
    val type: DeviceType,
    val voiceCount: Int = 16,
    val stepCount: Int = 16,
    val bpmRange: IntRange = 60..206,
    val guideUrl: String,
    private val voiceList: List<POVoice>,
    private val midiNotes: Map<Int, Int> // voiceNumber -> MIDI note
) {
    PO_12(
        modelId = "PO-12", deviceName = "Rhythm", type = DeviceType.DRUM_MACHINE,
        guideUrl = "https://teenage.engineering/guides/po-12",
        voiceList = listOf(
            POVoice(1, "Bass Drum", "kick"), POVoice(2, "Snare", "snare"),
            POVoice(3, "Closed Hi-Hat", "closed-hh"), POVoice(4, "Open Hi-Hat", "open-hh"),
            POVoice(5, "Low Tom", "tom-low"), POVoice(6, "Mid Tom", "tom-mid"),
            POVoice(7, "High Tom", "tom-high"), POVoice(8, "Rim Shot", "rim"),
            POVoice(9, "Hand Clap", "clap"), POVoice(10, "Cowbell", "cowbell"),
            POVoice(11, "Cymbal", "cymbal"), POVoice(12, "Click", "click"),
            POVoice(13, "Noise", "noise"), POVoice(14, "Blip", "blip"),
            POVoice(15, "Tone", "tone"), POVoice(16, "Sticks", "sticks")
        ),
        midiNotes = mapOf(
            1 to 36, 2 to 38, 3 to 42, 4 to 46, 5 to 45, 6 to 47,
            7 to 50, 8 to 37, 9 to 39, 10 to 56, 11 to 49, 12 to 33,
            13 to 54, 14 to 76, 15 to 80, 16 to 31
        )
    ),
    PO_14(
        modelId = "PO-14", deviceName = "Sub", type = DeviceType.BASS_SYNTH,
        guideUrl = "https://teenage.engineering/guides/po-14",
        voiceList = listOf(
            POVoice(1, "Sine Bass", "sine"), POVoice(2, "Square Bass", "square"),
            POVoice(3, "Saw Bass", "saw"), POVoice(4, "Pulse Bass", "pulse"),
            POVoice(5, "FM Bass", "fm"), POVoice(6, "Acid Bass", "acid"),
            POVoice(7, "Wobble", "wobble"), POVoice(8, "Sub Drop", "drop"),
            POVoice(9, "Pluck", "pluck"), POVoice(10, "Growl", "growl"),
            POVoice(11, "Reese", "reese"), POVoice(12, "Dist Bass", "dist"),
            POVoice(13, "Rubber", "rubber"), POVoice(14, "Deep", "deep"),
            POVoice(15, "Buzz", "buzz"), POVoice(16, "Thump", "thump")
        ),
        midiNotes = mapOf(
            1 to 36, 2 to 37, 3 to 38, 4 to 39, 5 to 40, 6 to 41,
            7 to 42, 8 to 43, 9 to 44, 10 to 45, 11 to 46, 12 to 47,
            13 to 48, 14 to 49, 15 to 50, 16 to 51
        )
    ),
    PO_16(
        modelId = "PO-16", deviceName = "Factory", type = DeviceType.LEAD_SYNTH,
        guideUrl = "https://teenage.engineering/guides/po-16",
        voiceList = listOf(
            POVoice(1, "Organ", "organ"), POVoice(2, "Piano", "piano"),
            POVoice(3, "Strings", "strings"), POVoice(4, "Brass", "brass"),
            POVoice(5, "Guitar", "guitar"), POVoice(6, "Synth Lead", "synlead"),
            POVoice(7, "Pad", "pad"), POVoice(8, "Choir", "choir"),
            POVoice(9, "Bell", "bell"), POVoice(10, "Marimba", "marimba"),
            POVoice(11, "Kalimba", "kalimba"), POVoice(12, "Pluck", "pluck"),
            POVoice(13, "Sweep", "sweep"), POVoice(14, "Noise", "noise"),
            POVoice(15, "FX", "fx"), POVoice(16, "Chord", "chord")
        ),
        midiNotes = mapOf(
            1 to 60, 2 to 61, 3 to 62, 4 to 63, 5 to 64, 6 to 65,
            7 to 66, 8 to 67, 9 to 68, 10 to 69, 11 to 70, 12 to 71,
            13 to 72, 14 to 73, 15 to 74, 16 to 75
        )
    ),
    PO_20(
        modelId = "PO-20", deviceName = "Arcade", type = DeviceType.CHIPTUNE,
        guideUrl = "https://teenage.engineering/guides/po-20",
        voiceList = (1..16).map { POVoice(it, "Sound $it", "snd$it") },
        midiNotes = (1..16).associate { it to (59 + it) }
    ),
    PO_24(
        modelId = "PO-24", deviceName = "Office", type = DeviceType.NOISE,
        guideUrl = "https://teenage.engineering/guides/po-24",
        voiceList = (1..16).map { POVoice(it, "Sound $it", "snd$it") },
        midiNotes = (1..16).associate { it to (35 + it) }
    ),
    PO_28(
        modelId = "PO-28", deviceName = "Robot", type = DeviceType.SYNTH_8BIT,
        guideUrl = "https://teenage.engineering/guides/po-28",
        voiceList = (1..16).map { POVoice(it, "Sound $it", "snd$it") },
        midiNotes = (1..16).associate { it to (59 + it) }
    ),
    PO_32(
        modelId = "PO-32", deviceName = "Tonic", type = DeviceType.DRUM_SYNTH,
        guideUrl = "https://teenage.engineering/guides/po-32",
        voiceList = (1..16).map { POVoice(it, "Sound $it", "snd$it") },
        midiNotes = (1..16).associate { it to (35 + it) }
    ),
    PO_33(
        modelId = "PO-33", deviceName = "K.O!", type = DeviceType.SAMPLER,
        guideUrl = "https://teenage.engineering/guides/po-33",
        voiceList = (1..16).map { POVoice(it, "Sound $it", "snd$it") },
        midiNotes = (1..16).associate { it to (35 + it) }
    ),
    PO_35(
        modelId = "PO-35", deviceName = "Speak", type = DeviceType.VOCAL_SYNTH,
        guideUrl = "https://teenage.engineering/guides/po-35",
        voiceList = (1..16).map { POVoice(it, "Sound $it", "snd$it") },
        midiNotes = (1..16).associate { it to (59 + it) }
    );

    val voices: List<POVoice> get() = voiceList

    fun getMidiNote(voice: POVoice): Int = midiNotes[voice.number] ?: 60

    fun getVoiceByNumber(number: Int): POVoice? = voiceList.find { it.number == number }

    fun getVoiceByShortName(shortName: String): POVoice? = voiceList.find { it.shortName == shortName }

    val isDrumMachine: Boolean get() = type == DeviceType.DRUM_MACHINE || type == DeviceType.DRUM_SYNTH || type == DeviceType.NOISE

    companion object {
        fun fromModelId(id: String): PODevice? = entries.find { it.modelId == id }
    }
}
