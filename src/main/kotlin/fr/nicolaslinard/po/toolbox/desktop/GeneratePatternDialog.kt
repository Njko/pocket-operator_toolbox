package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.BuiltInTemplates
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import fr.nicolaslinard.po.toolbox.models.PatternTemplate
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class GeneratePatternDialog {

    private var selectedTemplate: PatternTemplate? = null
    private val previewBox = VBox(2.0)

    fun show(): PO12Pattern? {
        val dialog = Dialog<PO12Pattern?>()
        dialog.title = "Générer un pattern"
        dialog.headerText = null
        dialog.isResizable = true

        val content = buildContent()
        dialog.dialogPane.content = content
        dialog.dialogPane.prefWidth = 780.0
        dialog.dialogPane.prefHeight = 550.0
        content.prefHeightProperty().bind(dialog.dialogPane.heightProperty())

        val createType = ButtonType("Créer le pattern", ButtonBar.ButtonData.OK_DONE)
        dialog.dialogPane.buttonTypes.addAll(createType, ButtonType.CANCEL)

        val createButton = dialog.dialogPane.lookupButton(createType)
        createButton.isDisable = true

        dialog.setResultConverter { bt ->
            if (bt == createType) buildPatternFromTemplate() else null
        }

        // Will be enabled when a template is selected
        val enableCreate = { createButton.isDisable = selectedTemplate == null }

        buildCategoryList(enableCreate)

        return dialog.showAndWait().orElse(null)
    }

    private lateinit var categoryList: ListView<String>
    private lateinit var templateList: ListView<PatternTemplate>

    private fun buildContent(): VBox {
        // Left: category list
        categoryList = ListView<String>().apply {
            prefWidth = 160.0
            items.addAll(BuiltInTemplates.CATEGORIES.values)
        }

        // Center: template list
        templateList = ListView<PatternTemplate>().apply {
            prefWidth = 260.0
            cellFactory = javafx.util.Callback {
                object : ListCell<PatternTemplate>() {
                    override fun updateItem(item: PatternTemplate?, empty: Boolean) {
                        super.updateItem(item, empty)
                        if (empty || item == null) {
                            graphic = null
                        } else {
                            graphic = VBox(2.0).apply {
                                children.add(Label(item.name).apply {
                                    style = "-fx-font-weight: bold; -fx-font-size: 12px;"
                                })
                                children.add(Label(item.description).apply {
                                    style = "-fx-text-fill: gray; -fx-font-size: 11px;"
                                })
                                children.add(HBox(8.0).apply {
                                    children.add(Label("${item.suggestedBPM ?: "-"} BPM").apply {
                                        style = "-fx-font-size: 10px;"
                                    })
                                    children.add(Label(item.difficulty.displayName).apply {
                                        style = "-fx-font-size: 10px; -fx-text-fill: ${difficultyColor(item)};"
                                    })
                                })
                            }
                        }
                    }
                }
            }
        }

        // Right: preview
        val previewScroll = ScrollPane(previewBox).apply {
            isFitToWidth = true
            style = "-fx-background-color: transparent;"
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        val previewPanel = VBox(8.0,
            Label("Apercu").apply { style = "-fx-font-weight: bold;" },
            previewScroll
        ).apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        val browserBox = HBox(8.0, categoryList, templateList, previewPanel).apply {
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        return VBox(10.0,
            Label("Choisissez un style et un pattern :").apply {
                style = "-fx-font-size: 13px;"
            },
            browserBox
        ).apply {
            padding = Insets(16.0)
        }
    }

    private fun buildCategoryList(onTemplateSelected: () -> Unit) {
        // Category selection updates template list
        categoryList.selectionModel.selectedItemProperty().addListener { _, _, displayName ->
            if (displayName == null) return@addListener
            val categoryKey = BuiltInTemplates.CATEGORIES.entries
                .find { it.value == displayName }?.key ?: return@addListener
            val templates = BuiltInTemplates.byCategory(categoryKey)
            templateList.items.setAll(templates)
            templateList.selectionModel.clearSelection()
            selectedTemplate = null
            previewBox.children.clear()
            onTemplateSelected()
        }

        // Template selection updates preview
        templateList.selectionModel.selectedItemProperty().addListener { _, _, template ->
            selectedTemplate = template
            renderPreview(template)
            onTemplateSelected()
        }

        // Auto-select first category
        categoryList.selectionModel.selectFirst()
    }

    private fun renderPreview(template: PatternTemplate?) {
        previewBox.children.clear()
        if (template == null) return

        previewBox.children.add(Label(template.name).apply {
            style = "-fx-font-weight: bold; -fx-font-size: 14px;"
        })
        previewBox.children.add(Label(template.description).apply {
            style = "-fx-text-fill: gray; -fx-padding: 0 0 4 0;"
        })
        previewBox.children.add(Label("BPM : ${template.suggestedBPM ?: "-"}  |  Difficulté : ${template.difficulty.displayName}").apply {
            style = "-fx-font-size: 11px; -fx-padding: 0 0 8 0;"
        })

        // Step header
        previewBox.children.add(HBox(2.0).apply {
            children.add(Label("").apply { prefWidth = 120.0 })
            (1..16).forEach { step ->
                children.add(Label(step.toString()).apply {
                    prefWidth = 20.0
                    alignment = Pos.CENTER
                    style = "-fx-font-size: 9px; -fx-text-fill: gray; -fx-font-family: monospace;"
                })
            }
        })

        // Voice rows
        PO12DrumVoice.entries.forEach { voice ->
            val steps = template.voices[voice] ?: return@forEach
            previewBox.children.add(HBox(2.0).apply {
                alignment = Pos.CENTER_LEFT
                children.add(Label("${voice.displayName} (%02d)".format(voice.poNumber)).apply {
                    prefWidth = 120.0
                    alignment = Pos.CENTER_RIGHT
                    style = "-fx-font-family: monospace; -fx-font-size: 10px;"
                })
                (1..16).forEach { step ->
                    val active = step in steps
                    children.add(Label(if (active) "●" else "·").apply {
                        prefWidth = 20.0
                        alignment = Pos.CENTER
                        style = buildString {
                            append("-fx-font-family: monospace; -fx-font-size: 12px;")
                            if (active) append(" -fx-text-fill: #e07050; -fx-font-weight: bold;")
                            else append(" -fx-text-fill: #666666;")
                        }
                    })
                }
            })
        }
    }

    private fun buildPatternFromTemplate(): PO12Pattern? {
        val template = selectedTemplate ?: return null
        return PO12Pattern(
            voices = template.voices,
            metadata = PatternMetadata(
                name = template.name,
                bpm = template.suggestedBPM,
                genre = listOf(
                    BuiltInTemplates.CATEGORIES[template.category] ?: template.category
                ),
                difficulty = template.difficulty
            ),
            number = 1
        )
    }

    private fun difficultyColor(template: PatternTemplate): String {
        return when (template.difficulty.displayName) {
            "beginner" -> "#55aa55"
            "intermediate" -> "#cc9933"
            "advanced" -> "#cc5555"
            else -> "gray"
        }
    }
}
