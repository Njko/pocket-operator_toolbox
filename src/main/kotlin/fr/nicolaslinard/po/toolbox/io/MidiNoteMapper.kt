package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice

/**
 * Maps PO-12 drum voices to General MIDI drum note numbers.
 * Uses the GM Drum Map standard (channel 10).
 *
 * Reference: General MIDI Level 1 Percussion Key Map
 * https://www.midi.org/specifications/item/gm-level-1-sound-set
 */
class MidiNoteMapper {

    companion object {
        // General MIDI Drum Map (MIDI note numbers)
        private val VOICE_TO_MIDI_NOTE = mapOf(
            PO12DrumVoice.KICK to 36,           // Bass Drum 1 (Acoustic)
            PO12DrumVoice.SNARE to 38,          // Acoustic Snare
            PO12DrumVoice.CLOSED_HH to 42,      // Closed Hi-Hat
            PO12DrumVoice.OPEN_HH to 46,        // Open Hi-Hat
            PO12DrumVoice.TOM_LOW to 45,        // Low Tom
            PO12DrumVoice.TOM_MID to 47,        // Low-Mid Tom
            PO12DrumVoice.TOM_HIGH to 50,       // High Tom
            PO12DrumVoice.RIM_SHOT to 37,       // Side Stick / Rim Shot
            PO12DrumVoice.HAND_CLAP to 39,      // Hand Clap
            PO12DrumVoice.COWBELL to 56,        // Cowbell
            PO12DrumVoice.CYMBAL to 49,         // Crash Cymbal 1
            PO12DrumVoice.CLICK to 33,          // Metronome Click
            PO12DrumVoice.NOISE to 54,          // Tambourine (closest to noise)
            PO12DrumVoice.BLIP to 76,           // Hi Wood Block (short percussive sound)
            PO12DrumVoice.TONE to 80,           // Mute Triangle (tonal percussion)
            PO12DrumVoice.STICKS to 31          // Sticks (lowest GM drum note)
        )
    }

    /**
     * Get the MIDI note number for a PO-12 drum voice.
     * @param voice The PO-12 drum voice
     * @return MIDI note number (35-81 for GM drum sounds)
     */
    fun getMidiNote(voice: PO12DrumVoice): Int {
        return VOICE_TO_MIDI_NOTE[voice] ?: 38 // Default to snare if unknown
    }

    /**
     * Get all voice mappings for reference.
     */
    fun getAllMappings(): Map<PO12DrumVoice, Int> {
        return VOICE_TO_MIDI_NOTE.toMap()
    }

    /**
     * Get the GM drum name for a MIDI note number (for debugging/display).
     */
    fun getGMDrumName(midiNote: Int): String? {
        return when (midiNote) {
            31 -> "Sticks"
            33 -> "Metronome Click"
            36 -> "Bass Drum 1"
            37 -> "Side Stick"
            38 -> "Acoustic Snare"
            39 -> "Hand Clap"
            42 -> "Closed Hi-Hat"
            45 -> "Low Tom"
            46 -> "Open Hi-Hat"
            47 -> "Low-Mid Tom"
            49 -> "Crash Cymbal 1"
            50 -> "High Tom"
            54 -> "Tambourine"
            56 -> "Cowbell"
            76 -> "Hi Wood Block"
            80 -> "Mute Triangle"
            else -> null
        }
    }
}
