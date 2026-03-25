package fr.nicolaslinard.po.toolbox.desktop

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.Separator
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import tornadofx.View
import tornadofx.borderpane
import tornadofx.splitpane

class MainView : View("PO-12 Toolbox") {

    private val controller: PatternController by inject()

    private fun createNewPattern() {
        val pattern = NewPatternDialog().show() ?: return
        controller.createPattern(pattern)
    }

    private fun generatePattern() {
        val pattern = GeneratePatternDialog().show() ?: return
        controller.createPattern(pattern)
    }

    private fun editSelectedPattern() {
        val summary = controller.selectedSummary.value ?: return
        val updatedPattern = NewPatternDialog(summary.pattern).show() ?: return
        controller.updatePattern(summary.file, updatedPattern)
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
                disableProperty().bind(controller.selectedSummary.isNull)
                setOnAction { deleteSelectedPattern() }
            }

            val editMenuItem = MenuItem("Éditer").apply {
                disableProperty().bind(controller.selectedSummary.isNull)
                setOnAction { editSelectedPattern() }
            }

            children.add(MenuBar(
                Menu("Fichier").apply {
                    items.addAll(
                        MenuItem("Nouveau pattern").apply {
                            setOnAction { createNewPattern() }
                        },
                        MenuItem("Générer un pattern...").apply {
                            setOnAction { generatePattern() }
                        },
                        editMenuItem,
                        deleteMenuItem,
                        MenuItem("Actualiser").apply {
                            setOnAction { controller.loadPatterns() }
                        }
                    )
                }
            ))

            // Toolbar
            children.add(HBox(4.0).apply {
                padding = Insets(4.0, 8.0, 4.0, 8.0)
                alignment = Pos.CENTER_LEFT
                style = "-fx-background-color: #2e2e2e; -fx-border-color: #444444; -fx-border-width: 0 0 1 0;"

                children.add(Button("➕ Nouveau").apply {
                    tooltip = Tooltip("Créer un nouveau pattern")
                    setOnAction { createNewPattern() }
                })

                children.add(Button("🎲 Générer").apply {
                    tooltip = Tooltip("Générer un pattern depuis un style musical")
                    setOnAction { generatePattern() }
                })

                children.add(Button("✏️ Éditer").apply {
                    tooltip = Tooltip("Éditer le pattern sélectionné")
                    disableProperty().bind(controller.selectedSummary.isNull)
                    setOnAction { editSelectedPattern() }
                })

                children.add(Separator(javafx.geometry.Orientation.VERTICAL))

                children.add(Button("🗑 Supprimer").apply {
                    tooltip = Tooltip("Supprimer le pattern sélectionné")
                    disableProperty().bind(controller.selectedSummary.isNull)
                    setOnAction { deleteSelectedPattern() }
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
    }
}
