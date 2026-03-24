package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.dynamicContent
import tornadofx.hbox
import tornadofx.insets
import tornadofx.label
import tornadofx.scrollpane
import tornadofx.separator
import tornadofx.vbox
import tornadofx.vgrow

class PatternDetailView : View() {

    private val controller: PatternController by inject()

    override val root = scrollpane {
        isFitToWidth = true
        vgrow = Priority.ALWAYS

        vbox(12.0) {
            style = "-fx-padding: 16px;"

            dynamicContent(controller.selectedPattern) { pattern ->
                if (pattern == null) {
                    label("Sélectionner un pattern pour voir ses détails") {
                        style = "-fx-text-fill: gray;"
                    }
                } else {
                    renderPattern(pattern)
                }
            }
        }
    }

    private fun javafx.scene.layout.Pane.renderPattern(pattern: PO12Pattern) {
        // Titre
        label(pattern.metadata.name) {
            style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        }

        // Métadonnées
        hbox(10.0) {
            alignment = Pos.CENTER_LEFT
            pattern.metadata.bpm?.let {
                label("BPM : $it") { style = "-fx-font-weight: bold;" }
            }
            pattern.metadata.difficulty?.let {
                label("• ${it.displayName}")
            }
            if (pattern.metadata.genre.isNotEmpty()) {
                label("• ${pattern.metadata.genre.joinToString(", ")}")
            }
            pattern.metadata.author?.let {
                label("• par $it") { style = "-fx-text-fill: gray;" }
            }
        }

        separator()

        // Grille
        label("Grille de steps") {
            style = "-fx-font-weight: bold;"
        }

        // En-tête des steps
        hbox(2.0) {
            alignment = Pos.CENTER_LEFT
            label("".padEnd(14)) { prefWidth = 118.0 }
            (1..16).forEach { step ->
                label(step.toString()) {
                    prefWidth = 22.0
                    alignment = Pos.CENTER
                    style = "-fx-font-size: 10px; -fx-text-fill: gray; -fx-font-family: monospace;"
                }
            }
        }

        // Une ligne par voix active
        PO12DrumVoice.entries.forEach { voice ->
            val steps = pattern.getActiveSteps(voice)
            if (steps.isNotEmpty()) {
                hbox(2.0) {
                    alignment = Pos.CENTER_LEFT
                    label(voice.displayName) {
                        prefWidth = 118.0
                        style = "-fx-font-family: monospace; -fx-font-size: 11px;"
                    }
                    (1..16).forEach { step ->
                        val active = step in steps
                        label(if (active) "●" else "·") {
                            prefWidth = 22.0
                            alignment = Pos.CENTER
                            style = buildString {
                                append("-fx-font-family: monospace; -fx-font-size: 13px;")
                                if (active) append(" -fx-text-fill: #e07050; -fx-font-weight: bold;")
                                else append(" -fx-text-fill: #888888;")
                            }
                        }
                    }
                }
            }
        }

        separator()

        // Instructions de programmation
        label("Comment programmer sur le PO-12") {
            style = "-fx-font-weight: bold;"
        }

        PO12DrumVoice.entries.forEach { voice ->
            val steps = pattern.getActiveSteps(voice)
            if (steps.isNotEmpty()) {
                label("Son ${voice.poNumber} (${voice.displayName}) → steps : ${steps.joinToString(", ")}") {
                    style = "-fx-font-family: monospace; -fx-font-size: 11px;"
                }
            }
        }
    }
}
