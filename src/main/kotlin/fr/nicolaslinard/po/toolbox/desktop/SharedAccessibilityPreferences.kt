package fr.nicolaslinard.po.toolbox.desktop

object SharedAccessibilityPreferences {
    val instance = AccessibilityPreferences().apply {
        load()
    }
}
