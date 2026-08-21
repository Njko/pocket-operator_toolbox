package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TableColumn
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.label
import tornadofx.tableview
import tornadofx.vbox
import tornadofx.vgrow

class PatternListView : View() {

    private val controller: PatternController by inject()

    override val root = vbox {
        label("Patterns") {
            styleClass.add("h2")
            style = "-fx-padding: 8px;"
        }

        tableview(controller.patterns) {
            val colNom = TableColumn<PatternSummary, String>("Nom")
            colNom.prefWidth = 160.0
            colNom.setCellValueFactory { SimpleStringProperty(it.value.name) }

            val colNum = TableColumn<PatternSummary, String>("#")
            colNum.prefWidth = 35.0
            colNum.setCellValueFactory { SimpleStringProperty(it.value.patternNumber) }

            val colBpm = TableColumn<PatternSummary, String>("BPM")
            colBpm.prefWidth = 50.0
            colBpm.setCellValueFactory { SimpleStringProperty(it.value.bpm) }

            val colDiff = TableColumn<PatternSummary, String>("Difficulté")
            colDiff.prefWidth = 90.0
            colDiff.setCellValueFactory { SimpleStringProperty(it.value.difficulty) }

            val colGenre = TableColumn<PatternSummary, String>("Genre")
            colGenre.prefWidth = 110.0
            colGenre.setCellValueFactory { SimpleStringProperty(it.value.genre) }

            val colVoix = TableColumn<PatternSummary, String>("Voix")
            colVoix.prefWidth = 40.0
            colVoix.setCellValueFactory { SimpleStringProperty(it.value.voiceCount) }

            columns.addAll(colNom, colNum, colBpm, colDiff, colGenre, colVoix)

            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                controller.selectPattern(newValue)
            }

            setOnMouseClicked { event ->
                if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                    val summary = selectionModel.selectedItem ?: return@setOnMouseClicked
                    editPattern(summary)
                }
            }

            setOnKeyPressed { event ->
                if (event.code == KeyCode.ENTER) {
                    val summary = selectionModel.selectedItem ?: return@setOnKeyPressed
                    editPattern(summary)
                }
            }

            vgrow = Priority.ALWAYS
        }
    }

    private fun editPattern(summary: PatternSummary) {
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
}
