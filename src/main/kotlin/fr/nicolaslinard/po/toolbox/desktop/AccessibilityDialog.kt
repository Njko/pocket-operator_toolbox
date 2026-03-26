package fr.nicolaslinard.po.toolbox.desktop

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class AccessibilityDialog {

    private val prefs = SharedAccessibilityPreferences.instance

    fun show() {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Accessibilité — Paramètres"
        dialog.headerText = "Personnalisez votre expérience"

        val themeCombo = ComboBox<String>().apply {
            items.addAll("Sombre", "Clair", "Contraste élevé")
            value = when (prefs.theme) {
                "light" -> "Clair"
                "high-contrast" -> "Contraste élevé"
                else -> "Sombre"
            }
            accessibleText = "Thème de l'application"
        }

        val colorBlindCombo = ComboBox<String>().apply {
            items.addAll("Aucun", "Deutéranopie", "Protanopie", "Tritanopie")
            value = when (prefs.colorBlindMode) {
                "deuteranopia" -> "Deutéranopie"
                "protanopia" -> "Protanopie"
                "tritanopia" -> "Tritanopie"
                else -> "Aucun"
            }
            accessibleText = "Mode daltonisme"
        }

        val motionToggle = CheckBox("Réduire les animations").apply {
            isSelected = prefs.reduceMotion
            accessibleText = "Réduire les animations de l'interface"
        }

        val fontSlider = Slider(0.8, 2.0, prefs.fontSizeMultiplier).apply {
            isShowTickLabels = true
            isShowTickMarks = true
            majorTickUnit = 0.2
            blockIncrement = 0.1
            accessibleText = "Taille du texte"
        }
        val fontLabel = Label("${(prefs.fontSizeMultiplier * 100).toInt()}%")
        fontSlider.valueProperty().addListener { _, _, newVal ->
            fontLabel.text = "${(newVal.toDouble() * 100).toInt()}%"
        }

        val grid = GridPane().apply {
            hgap = 12.0
            vgap = 14.0
            padding = Insets(20.0)

            add(Label("Thème :"), 0, 0)
            add(themeCombo, 1, 0)

            add(Label("Daltonisme :"), 0, 1)
            add(colorBlindCombo, 1, 1)

            add(motionToggle, 0, 2, 2, 1)

            add(Label("Taille du texte :"), 0, 3)
            add(HBox(8.0, fontSlider, fontLabel).apply { alignment = Pos.CENTER_LEFT }, 1, 3)
        }

        val resetButton = Button("Réinitialiser").apply {
            setOnAction {
                prefs.reset()
                themeCombo.value = "Sombre"
                colorBlindCombo.value = "Aucun"
                motionToggle.isSelected = false
                fontSlider.value = 1.0
            }
        }

        val content = VBox(12.0, grid, Separator(), HBox(resetButton).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(0.0, 0.0, 0.0, 20.0)
        })

        dialog.dialogPane.content = content
        dialog.dialogPane.buttonTypes.addAll(ButtonType.APPLY, ButtonType.CANCEL)

        val result = dialog.showAndWait()
        if (result.isPresent && result.get() == ButtonType.APPLY) {
            prefs.theme = when (themeCombo.value) {
                "Clair" -> "light"
                "Contraste élevé" -> "high-contrast"
                else -> "dark"
            }
            prefs.colorBlindMode = when (colorBlindCombo.value) {
                "Deutéranopie" -> "deuteranopia"
                "Protanopie" -> "protanopia"
                "Tritanopie" -> "tritanopia"
                else -> "none"
            }
            prefs.reduceMotion = motionToggle.isSelected
            prefs.fontSizeMultiplier = fontSlider.value
            prefs.save()
            applyPreferences()
        }
    }

    private fun applyPreferences() {
        val stage = javafx.stage.Window.getWindows().firstOrNull() as? javafx.stage.Stage ?: return
        val scene = stage.scene ?: return
        ThemeManager.apply(scene, prefs)
    }
}
