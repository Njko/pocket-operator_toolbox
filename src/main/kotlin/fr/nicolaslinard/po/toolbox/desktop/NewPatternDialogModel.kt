package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import java.util.TreeMap

class NewPatternDialogModel(existingPattern: PO12Pattern? = null) {

    var name: String = existingPattern?.metadata?.name ?: ""
    var patternNumber: Int = existingPattern?.number ?: 1
    var bpm: String = existingPattern?.metadata?.bpm?.toString() ?: ""
    var difficulty: String = existingPattern?.metadata?.difficulty?.displayName ?: "-"

    val isEditMode: Boolean = existingPattern != null

    // Voices sorted by PO number
    private val _voices: TreeMap<PO12DrumVoice, MutableList<Int>> =
        TreeMap(compareBy { it.poNumber })

    val voices: Map<PO12DrumVoice, List<Int>> get() = _voices

    init {
        existingPattern?.voices?.forEach { (voice, steps) ->
            _voices[voice] = steps.toMutableList()
        }
    }

    fun isValid(): Boolean = name.trim().isNotEmpty()

    fun addVoice(voice: PO12DrumVoice, steps: List<Int> = emptyList()) {
        if (!_voices.containsKey(voice)) {
            _voices[voice] = steps.toMutableList()
        }
    }

    fun removeVoice(voice: PO12DrumVoice) {
        _voices.remove(voice)
    }

    fun setSteps(voice: PO12DrumVoice, steps: List<Int>) {
        _voices[voice] = steps.toMutableList()
    }

    fun availableVoices(): List<PO12DrumVoice> =
        PO12DrumVoice.entries.filter { !_voices.containsKey(it) }

    fun buildPattern(): PO12Pattern {
        val activeVoices = _voices
            .mapValues { (_, steps) -> steps.filter { it in 1..16 } }
            .filter { (_, steps) -> steps.isNotEmpty() }

        return PO12Pattern(
            voices = activeVoices,
            metadata = PatternMetadata(
                name = name.trim(),
                bpm = bpm.trim().toIntOrNull(),
                difficulty = Difficulty.fromString(difficulty)
            ),
            number = patternNumber
        )
    }
}
