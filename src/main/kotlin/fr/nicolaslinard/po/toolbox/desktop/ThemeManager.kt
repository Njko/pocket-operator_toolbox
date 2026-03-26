package fr.nicolaslinard.po.toolbox.desktop

import javafx.scene.Scene

object ThemeManager {

    private val themeFiles = mapOf(
        "dark" to "/css/app.css",
        "light" to "/css/theme-light.css",
        "high-contrast" to "/css/theme-high-contrast.css"
    )

    private val colorBlindFiles = mapOf(
        "deuteranopia" to "/css/colorblind-deuteranopia.css",
        "protanopia" to "/css/colorblind-protanopia.css",
        "tritanopia" to "/css/colorblind-tritanopia.css"
    )

    fun apply(scene: Scene, prefs: AccessibilityPreferences) {
        scene.stylesheets.clear()

        // Base theme
        val themeCss = themeFiles[prefs.theme] ?: themeFiles["dark"]!!
        val themeUrl = javaClass.getResource(themeCss)?.toExternalForm()
        if (themeUrl != null) scene.stylesheets.add(themeUrl)

        // Color blind overlay
        if (prefs.colorBlindMode != "none") {
            val cbCss = colorBlindFiles[prefs.colorBlindMode]
            if (cbCss != null) {
                val cbUrl = javaClass.getResource(cbCss)?.toExternalForm()
                if (cbUrl != null) scene.stylesheets.add(cbUrl)
            }
        }

        // Font size
        scene.root.style = "-fx-font-size: ${(12 * prefs.fontSizeMultiplier).toInt()}px;"
    }
}
