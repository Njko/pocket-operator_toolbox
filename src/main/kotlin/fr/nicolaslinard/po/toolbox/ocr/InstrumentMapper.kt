package fr.nicolaslinard.po.toolbox.ocr

/**
 * Maps common drum notation terms to PO-12 voice names.
 * Handles various notation conventions and abbreviations.
 */
class InstrumentMapper {

    companion object {
        // Mapping of notation terms to PO-12 voice short names
        private val INSTRUMENT_MAPPINGS = mapOf(
            // Bass Drum / Kick
            "bass drum" to "kick",
            "bd" to "kick",
            "kick" to "kick",
            "kick drum" to "kick",

            // Snare
            "snare" to "snare",
            "snare drum" to "snare",
            "sd" to "snare",
            "sn" to "snare",

            // Hi-Hats
            "closed hi-hat" to "closed-hh",
            "closed hihat" to "closed-hh",
            "chh" to "closed-hh",
            "hh" to "closed-hh",

            "open hi-hat" to "open-hh",
            "open hihat" to "open-hh",
            "ohh" to "open-hh",

            // Toms
            "low tom" to "tom-low",
            "lt" to "tom-low",
            "floor tom" to "tom-low",

            "mid tom" to "tom-mid",
            "mt" to "tom-mid",

            "high tom" to "tom-high",
            "ht" to "tom-high",
            "rack tom" to "tom-high",

            // Other Percussion
            "rim shot" to "rim",
            "rim" to "rim",
            "rs" to "rim",

            "hand clap" to "clap",
            "clap" to "clap",
            "cp" to "clap",

            "cowbell" to "cowbell",
            "cb" to "cowbell",

            "cymbal" to "cymbal",
            "crash" to "cymbal",
            "cy" to "cymbal",

            "ride" to "cymbal",
            "ride cymbal" to "cymbal"
        )
    }

    /**
     * Map a drum notation term to a PO-12 voice name.
     * @param notationTerm The term from OCR/notation (e.g., "BD", "Snare Drum")
     * @return PO-12 voice short name, or null if unknown
     */
    fun mapToVoice(notationTerm: String): String? {
        val normalized = notationTerm.lowercase().trim()
        return INSTRUMENT_MAPPINGS[normalized]
    }

    /**
     * Get all supported notation terms.
     */
    fun getSupportedTerms(): Set<String> {
        return INSTRUMENT_MAPPINGS.keys
    }

    /**
     * Check if a notation term is recognized.
     */
    fun isRecognized(notationTerm: String): Boolean {
        return mapToVoice(notationTerm) != null
    }
}
