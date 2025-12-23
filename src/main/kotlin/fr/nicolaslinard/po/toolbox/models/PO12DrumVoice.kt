package fr.nicolaslinard.po.toolbox.models

/**
 * Drum voices specific to the Pocket Operator PO-12 (Rhythm).
 * Other PO models (PO-14 Sub, PO-16 Factory, etc.) would have their own voice definitions.
 */
enum class PO12DrumVoice(val poNumber: Int, val displayName: String, val shortName: String) {
    KICK(1, "Bass Drum", "kick"),
    SNARE(2, "Snare", "snare"),
    CLOSED_HH(3, "Closed Hi-Hat", "closed-hh"),
    OPEN_HH(4, "Open Hi-Hat", "open-hh"),
    TOM_LOW(5, "Low Tom", "tom-low"),
    TOM_MID(6, "Mid Tom", "tom-mid"),
    TOM_HIGH(7, "High Tom", "tom-high"),
    RIM_SHOT(8, "Rim Shot", "rim"),
    HAND_CLAP(9, "Hand Clap", "clap"),
    COWBELL(10, "Cowbell", "cowbell"),
    CYMBAL(11, "Cymbal", "cymbal"),
    CLICK(12, "Click", "click"),
    NOISE(13, "Noise", "noise"),
    BLIP(14, "Blip", "blip"),
    TONE(15, "Tone", "tone"),
    STICKS(16, "Sticks", "sticks");

    companion object {
        fun fromShortName(name: String): PO12DrumVoice? =
            entries.find { it.shortName.equals(name, ignoreCase = true) }

        fun fromPONumber(number: Int): PO12DrumVoice? =
            entries.find { it.poNumber == number }
    }
}
