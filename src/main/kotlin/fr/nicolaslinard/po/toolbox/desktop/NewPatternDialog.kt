package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.util.StringConverter

class NewPatternDialog(existingPattern: PO12Pattern? = null) {

    val model = NewPatternDialogModel(existingPattern)

    private val nameField = TextField().apply { promptText = "Nom du pattern (requis)" }
    private val patternNumberSpinner = Spinner<Int>(1, 16, 1).apply { prefWidth = 70.0 }
    private val bpmField = TextField().apply { promptText = "ex: 120"; prefWidth = 70.0 }
    private val difficultyCombo = ComboBox<String>()
    private val voiceCombo = ComboBox<PO12DrumVoice>()
    private val voicesBox = VBox(4.0)
    private val voiceRows = LinkedHashMap<PO12DrumVoice, Array<ToggleButton>>()

    fun show(): PO12Pattern? {
        val dialog = Dialog<PO12Pattern?>()
        dialog.title = if (model.isEditMode) "Éditer le pattern" else "Nouveau pattern"
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
                        model.addVoice(v)
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
            styleClass.add("h2")
        }

        // Step header
        val headerBox = HBox(2.0).apply {
            children.add(Label("").apply { prefWidth = 148.0 })
            (1..16).forEach { step ->
                children.add(Label(step.toString()).apply {
                    prefWidth = 32.0
                    alignment = Pos.CENTER
                    styleClass.add("step-header")
                })
            }
        }

        val scrollPane = ScrollPane(voicesBox).apply {
            isFitToWidth = true
            minHeight = 120.0
            style = "-fx-background-color: transparent;"
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        // Pre-fill from model
        if (model.isEditMode) {
            nameField.text = model.name
            patternNumberSpinner.valueFactory.value = model.patternNumber
            if (model.bpm.isNotEmpty()) bpmField.text = model.bpm
            difficultyCombo.value = model.difficulty

            for ((voice, steps) in model.voices) {
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
                prefWidth = 32.0
                prefHeight = 32.0
                isSelected = isActive
                selectedProperty().addListener { _, _, on ->
                    text = if (on) "●" else "·"
                }
                tooltip = Tooltip("Step $stepNumber")
            }
        }
        voiceRows[voice] = toggles

        val removeBtn = Button("×").apply {
            styleClass.add("danger")
            setOnAction {
                model.removeVoice(voice)
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
                styleClass.add("voice-label")
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

    private fun refreshVoiceCombo() {
        val remaining = PO12DrumVoice.entries.filter { !voiceRows.containsKey(it) }
        voiceCombo.items.setAll(remaining)
        if (remaining.isNotEmpty()) voiceCombo.value = remaining.first()
    }

    private fun syncModelFromUI() {
        model.name = nameField.text
        model.patternNumber = patternNumberSpinner.value
        model.bpm = bpmField.text
        model.difficulty = difficultyCombo.value
        voiceRows.forEach { (voice, toggles) ->
            val steps = toggles.indices.filter { i -> toggles[i].isSelected }.map { it + 1 }
            model.setSteps(voice, steps)
        }
    }

    private fun buildPattern(): PO12Pattern {
        syncModelFromUI()
        return model.buildPattern()
    }
}
