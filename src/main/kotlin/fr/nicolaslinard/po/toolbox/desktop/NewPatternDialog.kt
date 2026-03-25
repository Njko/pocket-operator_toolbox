package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.util.StringConverter

class NewPatternDialog(private val existingPattern: PO12Pattern? = null) {

    private val isEditMode = existingPattern != null

    private val nameField = TextField().apply { promptText = "Nom du pattern (requis)" }
    private val patternNumberSpinner = Spinner<Int>(1, 16, 1).apply { prefWidth = 70.0 }
    private val bpmField = TextField().apply { promptText = "ex: 120"; prefWidth = 70.0 }
    private val difficultyCombo = ComboBox<String>()
    private val voiceCombo = ComboBox<PO12DrumVoice>()
    private val voicesBox = VBox(4.0)
    private val voiceRows = LinkedHashMap<PO12DrumVoice, Array<ToggleButton>>()

    fun show(): PO12Pattern? {
        val dialog = Dialog<PO12Pattern?>()
        dialog.title = if (isEditMode) "Éditer le pattern" else "Nouveau pattern"
        dialog.headerText = null
        dialog.isResizable = true
        val content = buildContent()
        dialog.dialogPane.content = content
        dialog.dialogPane.prefWidth = 740.0
        dialog.dialogPane.prefHeight = 550.0

        // Bind content height to dialog so it grows when resized
        content.prefHeightProperty().bind(dialog.dialogPane.heightProperty())

        val saveType = ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE)
        dialog.dialogPane.buttonTypes.addAll(saveType, ButtonType.CANCEL)

        val saveButton = dialog.dialogPane.lookupButton(saveType)
        saveButton.isDisable = nameField.text.trim().isEmpty()
        nameField.textProperty().addListener { _, _, v ->
            saveButton.isDisable = v.trim().isEmpty()
        }

        dialog.setResultConverter { bt -> if (bt == saveType) buildPattern() else null }

        return dialog.showAndWait().orElse(null)
    }

    private fun buildContent(): VBox {
        // Metadata
        difficultyCombo.items.addAll("-", "beginner", "intermediate", "advanced")
        difficultyCombo.value = "-"

        val metaRow1 = HBox(10.0, Label("Nom *"), nameField).apply {
            alignment = Pos.CENTER_LEFT
            HBox.setHgrow(nameField, Priority.ALWAYS)
        }
        val metaRow2 = HBox(10.0,
            Label("N° pattern"), patternNumberSpinner,
            Label("BPM"), bpmField,
            Label("Difficulté"), difficultyCombo
        ).apply { alignment = Pos.CENTER_LEFT }

        val metaBox = VBox(8.0, metaRow1, metaRow2).apply {
            padding = Insets(0.0, 0.0, 4.0, 0.0)
        }

        // Voice selector
        voiceCombo.prefWidth = 170.0
        voiceCombo.converter = object : StringConverter<PO12DrumVoice>() {
            override fun toString(v: PO12DrumVoice?) = v?.let { "${it.displayName} (${it.poNumber})" } ?: ""
            override fun fromString(s: String) = null
        }
        refreshVoiceCombo()

        val addBtn = Button("+ Ajouter").apply {
            setOnAction {
                voiceCombo.value?.let { v ->
                    if (!voiceRows.containsKey(v)) {
                        addVoiceRow(v)
                        refreshVoiceCombo()
                    }
                }
            }
        }

        val adderRow = HBox(8.0, Label("Voix :"), voiceCombo, addBtn).apply {
            alignment = Pos.CENTER_LEFT
        }

        val voicesLabel = Label("Grille des steps").apply {
            style = "-fx-font-weight: bold; -fx-padding: 4 0 0 0;"
        }

        // Step header
        val headerBox = HBox(2.0).apply {
            children.add(Label("").apply { prefWidth = 148.0 })
            (1..16).forEach { step ->
                children.add(Label(step.toString()).apply {
                    prefWidth = 30.0
                    alignment = Pos.CENTER
                    style = "-fx-font-size: 10px; -fx-text-fill: gray;"
                })
            }
        }

        val scrollPane = ScrollPane(voicesBox).apply {
            isFitToWidth = true
            minHeight = 120.0
            style = "-fx-background-color: transparent;"
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        // Pre-fill in edit mode
        existingPattern?.let { pattern ->
            nameField.text = pattern.metadata.name
            patternNumberSpinner.valueFactory.value = pattern.number
            pattern.metadata.bpm?.let { bpmField.text = it.toString() }
            pattern.metadata.difficulty?.let { difficultyCombo.value = it.displayName }
                ?: run { difficultyCombo.value = "-" }

            // Add existing voices with their active steps
            for ((voice, steps) in pattern.voices) {
                addVoiceRow(voice, steps)
            }
            refreshVoiceCombo()
        }

        return VBox(10.0, metaBox, Separator(), voicesLabel, adderRow, headerBox, scrollPane).apply {
            padding = Insets(16.0)
        }
    }

    private fun addVoiceRow(voice: PO12DrumVoice, activeSteps: List<Int> = emptyList()) {
        val toggles = Array(16) { i ->
            val stepNumber = i + 1
            val isActive = stepNumber in activeSteps
            ToggleButton(if (isActive) "●" else "·").apply {
                prefWidth = 30.0
                prefHeight = 28.0
                isSelected = isActive
                style = stepStyle(isActive)
                selectedProperty().addListener { _, _, on ->
                    text = if (on) "●" else "·"
                    style = stepStyle(on)
                }
                tooltip = Tooltip("Step $stepNumber")
            }
        }
        voiceRows[voice] = toggles

        val removeBtn = Button("×").apply {
            style = "-fx-text-fill: #cc4444; -fx-background-color: transparent; -fx-font-size: 13px; -fx-cursor: hand;"
            setOnAction {
                voiceRows.remove(voice)
                voicesBox.children.removeIf { it.userData == voice }
                refreshVoiceCombo()
            }
        }

        val row = HBox(2.0).apply {
            userData = voice
            alignment = Pos.CENTER_LEFT
            children.add(Label("${voice.displayName} (%02d)".format(voice.poNumber)).apply {
                prefWidth = 140.0
                alignment = Pos.CENTER_RIGHT
                style = "-fx-font-size: 11px; -fx-font-family: monospace;"
            })
            toggles.forEach { children.add(it) }
            children.add(removeBtn)
        }

        // Insert sorted by voice PO number
        val insertIndex = voicesBox.children.indexOfFirst {
            val existing = it.userData as? PO12DrumVoice ?: return@indexOfFirst false
            existing.poNumber > voice.poNumber
        }
        if (insertIndex >= 0) voicesBox.children.add(insertIndex, row)
        else voicesBox.children.add(row)
    }

    private fun stepStyle(active: Boolean) =
        if (active) "-fx-background-color: #e07050; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 2;"
        else "-fx-background-color: #555555; -fx-text-fill: #999999; -fx-font-size: 11px; -fx-padding: 2;"

    private fun refreshVoiceCombo() {
        val remaining = PO12DrumVoice.entries.filter { !voiceRows.containsKey(it) }
        voiceCombo.items.setAll(remaining)
        if (remaining.isNotEmpty()) voiceCombo.value = remaining.first()
    }

    private fun buildPattern(): PO12Pattern {
        val voices = voiceRows.mapValues { (_, toggles) ->
            toggles.indices.filter { i -> toggles[i].isSelected }.map { it + 1 }
        }.filter { (_, steps) -> steps.isNotEmpty() }

        return PO12Pattern(
            voices = voices,
            metadata = PatternMetadata(
                name = nameField.text.trim(),
                bpm = bpmField.text.trim().toIntOrNull(),
                difficulty = Difficulty.fromString(difficultyCombo.value)
            ),
            number = patternNumberSpinner.value
        )
    }
}
