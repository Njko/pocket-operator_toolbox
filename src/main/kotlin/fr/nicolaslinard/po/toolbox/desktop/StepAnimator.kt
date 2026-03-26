package fr.nicolaslinard.po.toolbox.desktop

import javafx.animation.ScaleTransition
import javafx.scene.control.Label
import javafx.util.Duration

class StepAnimator(
    private val preferences: AccessibilityPreferences = AccessibilityPreferences()
) {

    companion object {
        const val HIT_STYLE_CLASS = "step-hit"
        const val VOICE_HIT_STYLE_CLASS = "voice-hit"
        const val ACTIVE_STYLE_CLASS = "step-active"
        private val PULSE_DURATION = Duration.millis(200.0)
        private const val PULSE_SCALE = 1.4
        private const val VOICE_PULSE_SCALE = 1.15

        fun isActiveStyleClass(styleClass: String): Boolean =
            styleClass == ACTIVE_STYLE_CLASS
    }

    val isReducedMotion: Boolean get() = preferences.reduceMotion

    private val activeTransitions = mutableListOf<Pair<Label, ScaleTransition>>()
    private val styledLabels = mutableListOf<Pair<Label, String>>()

    fun animateStep(labels: List<Label>) {
        labels.filter { label -> label.styleClass.any { isActiveStyleClass(it) } }
            .forEach { label -> pulseLabel(label, HIT_STYLE_CLASS, PULSE_SCALE) }
    }

    fun animateVoiceLabels(labels: List<Label>) {
        labels.forEach { label -> pulseLabel(label, VOICE_HIT_STYLE_CLASS, VOICE_PULSE_SCALE) }
    }

    fun clearAnimations() {
        activeTransitions.forEach { (label, transition) ->
            transition.stop()
            label.scaleX = 1.0
            label.scaleY = 1.0
        }
        activeTransitions.clear()
        styledLabels.forEach { (label, cssClass) ->
            label.styleClass.remove(cssClass)
        }
        styledLabels.clear()
    }

    private fun pulseLabel(label: Label, cssClass: String, scale: Double) {
        label.styleClass.add(cssClass)
        styledLabels.add(label to cssClass)

        if (preferences.reduceMotion) return

        val transition = ScaleTransition(PULSE_DURATION, label).apply {
            fromX = 1.0
            fromY = 1.0
            toX = scale
            toY = scale
            isAutoReverse = true
            cycleCount = 2
            setOnFinished {
                label.styleClass.remove(cssClass)
            }
        }
        activeTransitions.add(label to transition)
        transition.play()
    }
}
