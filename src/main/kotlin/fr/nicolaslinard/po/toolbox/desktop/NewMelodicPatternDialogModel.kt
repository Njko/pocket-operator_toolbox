package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Step
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.POVoice
import fr.nicolaslinard.po.toolbox.models.Po14PatternTemplate
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import java.util.TreeMap

class NewMelodicPatternDialogModel(existingPattern: PO14Pattern? = null) {

    var name: String = existingPattern?.metadata?.name ?: ""
    var patternNumber: Int = existingPattern?.number ?: 1
    var bpm: String = existingPattern?.metadata?.bpm?.toString() ?: ""
    var difficulty: String = existingPattern?.metadata?.difficulty?.displayName ?: "-"
    var sound: POVoice = existingPattern?.sound ?: PODevice.PO_14.voices.first()

    val isEditMode: Boolean = existingPattern != null

    private val _steps: TreeMap<Int, PO14Step> = TreeMap(existingPattern?.steps ?: emptyMap())

    val steps: Map<Int, PO14Step> get() = _steps

    fun setStep(step: Int, note: PO14Step) {
        require(step in 1..16) { "Step must be between 1 and 16" }
        _steps[step] = note
    }

    fun clearStep(step: Int) {
        _steps.remove(step)
    }

    fun isValid(): Boolean = name.trim().isNotEmpty()

    fun applyTemplate(template: Po14PatternTemplate) {
        _steps.clear()
        _steps.putAll(template.steps)
        sound = template.sound
        template.suggestedBPM?.let { bpm = it.toString() }
    }

    fun buildPattern(): PO14Pattern = PO14Pattern(
        steps = steps.toMap(),
        sound = sound,
        metadata = PatternMetadata(
            name = name.trim(),
            bpm = bpm.trim().toIntOrNull(),
            difficulty = Difficulty.fromString(difficulty),
            deviceModel = PODevice.PO_14.modelId
        ),
        number = patternNumber
    )
}
