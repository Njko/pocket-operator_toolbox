package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PatternChain
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import java.util.TreeMap

class NewPatternDialogModel(existingPattern: PO12Pattern? = null) {

    var name: String = existingPattern?.metadata?.name ?: ""
    var patternNumber: Int = existingPattern?.number ?: 1
    var bpm: String = existingPattern?.metadata?.bpm?.toString() ?: ""
    var difficulty: String = existingPattern?.metadata?.difficulty?.displayName ?: "-"

    val isEditMode: Boolean = existingPattern != null

    var barCount: Int = 1
        set(value) {
            val clamped = value.coerceIn(1, 16)
            // Add new bars if needed
            while (_bars.size < clamped) {
                _bars.add(newBarMap())
            }
            field = clamped
        }

    var currentBarIndex: Int = 0
        private set

    // Each bar has its own set of voices
    private val _bars: MutableList<TreeMap<PO12DrumVoice, MutableList<Int>>> = mutableListOf(
        newBarMap().also { map ->
            existingPattern?.voices?.forEach { (voice, steps) ->
                map[voice] = steps.toMutableList()
            }
        }
    )

    private fun newBarMap(): TreeMap<PO12DrumVoice, MutableList<Int>> =
        TreeMap(compareBy { it.poNumber })

    val voices: Map<PO12DrumVoice, List<Int>> get() = _bars[currentBarIndex]

    fun switchBar(index: Int) {
        if (index in 0 until barCount) {
            currentBarIndex = index
        }
    }

    fun isValid(): Boolean = name.trim().isNotEmpty()

    fun addVoice(voice: PO12DrumVoice, steps: List<Int> = emptyList()) {
        val bar = _bars[currentBarIndex]
        if (!bar.containsKey(voice)) {
            bar[voice] = steps.toMutableList()
        }
    }

    fun removeVoice(voice: PO12DrumVoice) {
        _bars[currentBarIndex].remove(voice)
    }

    fun setSteps(voice: PO12DrumVoice, steps: List<Int>) {
        _bars[currentBarIndex][voice] = steps.toMutableList()
    }

    fun availableVoices(): List<PO12DrumVoice> =
        PO12DrumVoice.entries.filter { !_bars[currentBarIndex].containsKey(it) }

    fun buildPattern(): PO12Pattern = buildPatternForBar(0)

    fun buildResult(): PatternDialogResult {
        if (barCount == 1) {
            return PatternDialogResult.Single(buildPatternForBar(0))
        }
        val patterns = (0 until barCount).map { buildPatternForBar(it) }
        val sequence = (1..barCount).toList()
        val metadata = buildMetadata()
        val chain = PatternChain(
            name = name.trim(),
            patterns = patterns,
            sequence = sequence,
            metadata = metadata
        )
        return PatternDialogResult.Chain(chain)
    }

    private fun buildPatternForBar(barIndex: Int): PO12Pattern {
        val bar = _bars[barIndex]
        val activeVoices = bar
            .mapValues { (_, steps) -> steps.filter { it in 1..16 } }
            .filter { (_, steps) -> steps.isNotEmpty() }

        return PO12Pattern(
            voices = activeVoices,
            metadata = buildMetadata(),
            number = patternNumber + barIndex
        )
    }

    private fun buildMetadata(): PatternMetadata = PatternMetadata(
        name = name.trim(),
        bpm = bpm.trim().toIntOrNull(),
        difficulty = Difficulty.fromString(difficulty)
    )
}
