package fr.nicolaslinard.po.toolbox.desktop

import kotlin.math.roundToInt

class ScaledSize(private val prefs: AccessibilityPreferences = SharedAccessibilityPreferences.instance) {

    private val m get() = prefs.fontSizeMultiplier

    fun scale(base: Double): Double = (base * m * 10).roundToInt() / 10.0

    // Step grid
    val stepWidth get() = scale(22.0)
    val beatSeparatorWidth get() = scale(4.0)
    val voiceLabelWidth get() = scale(130.0)

    // Toggle buttons (edit dialog)
    val toggleSize get() = scale(32.0)
    val editVoiceLabelWidth get() = scale(140.0)
    val editHeaderSpacerWidth get() = editVoiceLabelWidth + scale(8.0)
    val scrollMinHeight get() = scale(120.0)
    val spacing get() = scale(2.0)
    val padding get() = scale(16.0)
    val voiceLabelPadding get() = scale(6.0)

    // Dialogs
    val dialogWidth get() = scale(740.0)
    val dialogHeight get() = scale(550.0)

    // Main window
    val mainMinWidth get() = scale(900.0)
    val mainMinHeight get() = scale(600.0)
}
