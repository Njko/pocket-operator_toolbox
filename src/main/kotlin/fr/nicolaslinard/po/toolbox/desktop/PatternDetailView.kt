package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.dynamicContent
import tornadofx.hbox
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
            style = "-fx-padding: 20px;"

            dynamicContent(controller.selectedPattern) { pattern ->
                if (pattern == null) {
                    label("Sélectionner un pattern pour voir ses détails") {
                        styleClass.add("muted")
                        style = "-fx-font-size: 14px; -fx-padding: 40 0 0 0;"
                    }
                } else {
                    renderPattern(pattern)
                }
            }
        }
    }

    private fun javafx.scene.layout.Pane.renderPattern(pattern: PO12Pattern) {
        // Title
        label(pattern.metadata.name) {
            styleClass.add("h1")
        }

        // Metadata
        hbox(10.0) {
            alignment = Pos.CENTER_LEFT
            pattern.metadata.bpm?.let {
                label("BPM : $it") { style = "-fx-font-weight: bold;" }
            }
            pattern.metadata.difficulty?.let {
                label("| ${it.displayName}") {
                    styleClass.add("difficulty-${it.displayName}")
                }
            }
            if (pattern.metadata.genre.isNotEmpty()) {
                label("| ${pattern.metadata.genre.joinToString(", ")}")
            }
            pattern.metadata.author?.let {
                label("| par $it") { styleClass.add("muted") }
            }
        }

        separator()

        // Grid
        label("Grille de steps") {
            styleClass.add("h2")
        }

        // Step header with beat grouping
        hbox(0.0) {
            alignment = Pos.CENTER_LEFT
            label("") { prefWidth = 130.0 }
            (1..16).forEach { step ->
                label(step.toString()) {
                    prefWidth = 22.0
                    alignment = Pos.CENTER
                    styleClass.add("step-header")
                }
                // Beat separator after every 4 steps
                if (step % 4 == 0 && step < 16) {
                    label("") {
                        prefWidth = 4.0
                        styleClass.add("beat-separator")
                    }
                }
            }
        }

        // Voice rows
        PO12DrumVoice.entries.forEach { voice ->
            val steps = pattern.getActiveSteps(voice)
            if (steps.isNotEmpty()) {
                hbox(0.0) {
                    alignment = Pos.CENTER_LEFT
                    label("${voice.displayName} (%02d)".format(voice.poNumber)) {
                        prefWidth = 130.0
                        alignment = Pos.CENTER_RIGHT
                        styleClass.add("voice-label")
                        style = "-fx-padding: 0 6 0 0;"
                    }
                    (1..16).forEach { step ->
                        val active = step in steps
                        label(if (active) "●" else "·") {
                            prefWidth = 22.0
                            alignment = Pos.CENTER
                            styleClass.add(if (active) "step-active" else "step-inactive")
                        }
                        if (step % 4 == 0 && step < 16) {
                            label("") {
                                prefWidth = 4.0
                                styleClass.add("beat-separator")
                            }
                        }
                    }
                }
            }
        }

        separator()

        // Programming instructions
        label("Comment programmer sur le PO-12") {
            styleClass.add("h2")
        }

        PO12DrumVoice.entries.forEach { voice ->
            val steps = pattern.getActiveSteps(voice)
            if (steps.isNotEmpty()) {
                label("Son %02d (%s) → steps : %s".format(voice.poNumber, voice.displayName, steps.joinToString(", "))) {
                    styleClass.add("voice-label")
                }
            }
        }
    }
}
