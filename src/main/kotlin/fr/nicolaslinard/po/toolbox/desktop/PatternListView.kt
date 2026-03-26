package fr.nicolaslinard.po.toolbox.desktop

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
                    val updatedPattern = NewPatternDialog(summary.pattern).show() ?: return@setOnMouseClicked
                    controller.updatePattern(summary.file, updatedPattern)
                }
            }

            setOnKeyPressed { event ->
                if (event.code == KeyCode.ENTER) {
                    val summary = selectionModel.selectedItem ?: return@setOnKeyPressed
                    val updatedPattern = NewPatternDialog(summary.pattern).show() ?: return@setOnKeyPressed
                    controller.updatePattern(summary.file, updatedPattern)
                }
            }

            vgrow = Priority.ALWAYS
        }
    }
}
