package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.Separator
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.ToggleButton
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCombination
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import tornadofx.View
import tornadofx.borderpane
import tornadofx.splitpane

class MainView : View("Pocket Operator Toolbox") {

    private val controller: PatternController by inject()
    private val playbackController = SharedPlaybackController.instance

    private fun createNewPattern() {
        val result = NewPatternDialog().showMultiBar() ?: return
        controller.activeDialogResult = result
        controller.createPattern(result.firstPattern)
    }

    private fun createNewMelodicPattern() {
        val pattern = NewMelodicPatternDialog().show() ?: return
        controller.createPattern(pattern)
    }

    private fun generatePattern() {
        val pattern = GeneratePatternDialog().show() ?: return
        controller.createPattern(pattern)
    }

    private fun editSelectedPattern() {
        val summary = controller.selectedSummary.value ?: return
        when (val pattern = summary.pattern) {
            is PO12Pattern -> {
                val updated = NewPatternDialog(pattern).show() ?: return
                controller.updatePattern(summary.file, updated)
            }
            is PO14Pattern -> {
                val updated = NewMelodicPatternDialog(pattern).show() ?: return
                controller.updatePattern(summary.file, updated)
            }
        }
    }

    private fun openDocumentation(model: String) {
        val url = if (model.isEmpty()) "https://teenage.engineering/products/po"
                  else "https://teenage.engineering/guides/$model"
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI(url))
        } catch (_: Exception) {
            // Fallback silencieux si le navigateur n'est pas disponible
        }
    }

    private fun openAccessibilitySettings() {
        AccessibilityDialog().show()
    }

    private fun deleteSelectedPattern() {
        val summary = controller.selectedSummary.value ?: return
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Supprimer"
        alert.headerText = "Supprimer « ${summary.name} » ?"
        alert.contentText = "Cette action est irréversible."
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            controller.deleteSelectedPattern()
        }
    }

    override val root = borderpane {
        prefWidth = 1100.0
        prefHeight = 700.0

        top = VBox().apply {
            // Menu bar
            val deleteMenuItem = MenuItem("Supprimer").apply {
                accelerator = KeyCombination.keyCombination("Delete")
                disableProperty().bind(controller.selectedSummary.isNull)
                setOnAction { deleteSelectedPattern() }
            }

            val editMenuItem = MenuItem("Éditer").apply {
                accelerator = KeyCombination.keyCombination("Ctrl+E")
                disableProperty().bind(controller.selectedSummary.isNull)
                setOnAction { editSelectedPattern() }
            }

            children.add(MenuBar(
                Menu("Fichier").apply {
                    items.addAll(
                        MenuItem("Nouveau pattern PO-12 (Rhythm)").apply {
                            accelerator = KeyCombination.keyCombination("Ctrl+N")
                            setOnAction { createNewPattern() }
                        },
                        MenuItem("Nouveau pattern PO-14 (Sub)").apply {
                            accelerator = KeyCombination.keyCombination("Ctrl+Shift+N")
                            setOnAction { createNewMelodicPattern() }
                        },
                        MenuItem("Générer un pattern...").apply {
                            accelerator = KeyCombination.keyCombination("Ctrl+G")
                            setOnAction { generatePattern() }
                        },
                        editMenuItem,
                        deleteMenuItem,
                        SeparatorMenuItem(),
                        MenuItem("Actualiser").apply {
                            accelerator = KeyCombination.keyCombination("F5")
                            setOnAction { controller.loadPatterns() }
                        }
                    )
                },
                Menu("Accessibilité").apply {
                    items.add(MenuItem("Paramètres...").apply {
                        accelerator = KeyCombination.keyCombination("Ctrl+Shift+A")
                        setOnAction { openAccessibilitySettings() }
                    })
                },
                Menu("Aide").apply {
                    items.addAll(
                        Menu("Guides Pocket Operator").apply {
                            items.addAll(
                                MenuItem("PO-12 Rhythm (Drum machine)").apply {
                                    setOnAction { openDocumentation("po-12") }
                                },
                                MenuItem("PO-14 Sub (Synthé basse)").apply {
                                    setOnAction { openDocumentation("po-14") }
                                },
                                MenuItem("PO-16 Factory (Synthé lead)").apply {
                                    setOnAction { openDocumentation("po-16") }
                                },
                                SeparatorMenuItem(),
                                MenuItem("PO-20 Arcade (Chiptune)").apply {
                                    setOnAction { openDocumentation("po-20") }
                                },
                                MenuItem("PO-24 Office (Noise)").apply {
                                    setOnAction { openDocumentation("po-24") }
                                },
                                MenuItem("PO-28 Robot (Synthé 8-bit)").apply {
                                    setOnAction { openDocumentation("po-28") }
                                },
                                SeparatorMenuItem(),
                                MenuItem("PO-32 Tonic (Drum synth)").apply {
                                    setOnAction { openDocumentation("po-32") }
                                },
                                MenuItem("PO-33 K.O! (Sampler)").apply {
                                    setOnAction { openDocumentation("po-33") }
                                },
                                MenuItem("PO-35 Speak (Synthé vocal)").apply {
                                    setOnAction { openDocumentation("po-35") }
                                }
                            )
                        },
                        SeparatorMenuItem(),
                        MenuItem("Site Teenage Engineering").apply {
                            setOnAction { openDocumentation("") }
                        }
                    )
                }
            ))

            // Toolbar
            children.add(HBox(6.0).apply {
                styleClass.add("toolbar")
                alignment = Pos.CENTER_LEFT

                children.add(Button("Nouveau").apply {
                    styleClass.add("primary")
                    tooltip = Tooltip("Créer un nouveau pattern")
                    setOnAction { createNewPattern() }
                })

                children.add(Button("Générer").apply {
                    tooltip = Tooltip("Générer un pattern depuis un style musical")
                    setOnAction { generatePattern() }
                })

                children.add(Button("Éditer").apply {
                    tooltip = Tooltip("Éditer le pattern sélectionné")
                    disableProperty().bind(controller.selectedSummary.isNull)
                    setOnAction { editSelectedPattern() }
                })

                children.add(Separator(javafx.geometry.Orientation.VERTICAL))

                children.add(Button("Supprimer").apply {
                    styleClass.add("danger")
                    tooltip = Tooltip("Supprimer le pattern sélectionné")
                    disableProperty().bind(controller.selectedSummary.isNull)
                    setOnAction { deleteSelectedPattern() }
                })

                children.add(Separator(javafx.geometry.Orientation.VERTICAL))

                val playButton = Button("Play").apply {
                    styleClass.add("playback")
                    tooltip = Tooltip("Lire le pattern sélectionné")
                    disableProperty().bind(controller.selectedSummary.isNull)
                    setOnAction {
                        if (playbackController.isPlaying) {
                            playbackController.stop()
                            text = "Play"
                        } else {
                            val pattern = controller.selectedPattern.value ?: return@setOnAction
                            try {
                                val result = controller.activeDialogResult
                                if (result is PatternDialogResult.Chain) {
                                    playbackController.playChain(result.chain)
                                } else {
                                    playbackController.play(pattern)
                                }
                                text = "Stop"
                            } catch (e: Exception) {
                                Alert(Alert.AlertType.ERROR).apply {
                                    title = "Erreur de lecture"
                                    headerText = "Impossible de lire le pattern"
                                    contentText = "Le synthétiseur MIDI n'est pas disponible sur ce système."
                                }.showAndWait()
                            }
                        }
                    }
                }
                children.add(playButton)

                // Reset play button when playback ends naturally
                playbackController.onPlaybackStopped = {
                    javafx.application.Platform.runLater {
                        playButton.text = "Play"
                    }
                }

                children.add(ToggleButton("Loop").apply {
                    styleClass.add("loop-toggle")
                    tooltip = Tooltip("Lecture en boucle")
                    disableProperty().bind(controller.selectedSummary.isNull)
                    setOnAction { playbackController.toggleLoop() }
                })
            })
        }

        center = splitpane {
            add(PatternListView::class)
            add(PatternDetailView::class)
            setDividerPositions(0.32)
        }
    }

    override fun onDock() {
        controller.loadPatterns()
        controller.selectedPattern.addListener { _, _, _ ->
            playbackController.stop()
        }
    }
}
