package fr.nicolaslinard.po.toolbox.desktop

import org.json.JSONObject
import java.io.File

class AccessibilityPreferences {

    companion object {
        val VALID_THEMES = listOf("dark", "light", "high-contrast")
        val VALID_COLOR_BLIND_MODES = listOf("none", "deuteranopia", "protanopia", "tritanopia")
        const val MIN_FONT_SIZE = 0.8
        const val MAX_FONT_SIZE = 2.0

        private val defaultPrefsDir = File(System.getProperty("user.home"), ".po-toolbox")
        val defaultFile = File(defaultPrefsDir, "preferences.json")
    }

    var theme: String = "dark"
        set(value) { field = if (value in VALID_THEMES) value else field }

    var colorBlindMode: String = "none"
        set(value) { field = if (value in VALID_COLOR_BLIND_MODES) value else field }

    var reduceMotion: Boolean = false

    var fontSizeMultiplier: Double = 1.0
        set(value) { field = value.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE) }

    fun save(file: File = defaultFile) {
        file.parentFile?.mkdirs()
        val json = JSONObject().apply {
            put("theme", theme)
            put("colorBlindMode", colorBlindMode)
            put("reduceMotion", reduceMotion)
            put("fontSizeMultiplier", fontSizeMultiplier)
        }
        file.writeText(json.toString(2))
    }

    fun load(file: File = defaultFile) {
        if (!file.exists()) return
        try {
            val json = JSONObject(file.readText())
            theme = json.optString("theme", "dark")
            colorBlindMode = json.optString("colorBlindMode", "none")
            reduceMotion = json.optBoolean("reduceMotion", false)
            fontSizeMultiplier = json.optDouble("fontSizeMultiplier", 1.0)
        } catch (_: Exception) {
            // Corrupt file — keep defaults
        }
    }

    fun reset() {
        theme = "dark"
        colorBlindMode = "none"
        reduceMotion = false
        fontSizeMultiplier = 1.0
    }
}
