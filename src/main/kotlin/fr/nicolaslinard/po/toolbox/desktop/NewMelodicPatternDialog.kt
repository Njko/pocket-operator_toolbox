package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.BuiltInPo14Templates
import fr.nicolaslinard.po.toolbox.models.PO14Pattern
import fr.nicolaslinard.po.toolbox.models.PO14Step
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.POVoice
import fr.nicolaslinard.po.toolbox.models.Pitch
import fr.nicolaslinard.po.toolbox.models.Po14PatternTemplate
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.util.StringConverter

class NewMelodicPatternDialog(existingPattern: PO14Pattern? = null) {

    val model = NewMelodicPatternDialogModel(existingPattern)
    private val sz = ScaledSize()

    private val nameField = TextField().apply { promptText = "Nom du pattern (requis)"; accessibleText = "Nom du pattern" }
    private val patternNumberSpinner = Spinner<Int>(1, 16, 1).apply { prefWidth = 70.0; accessibleText = "Numéro de pattern" }
    private val bpmField = TextField().apply { promptText = "ex: 100"; prefWidth = 70.0; accessibleText = "BPM" }
    private val difficultyCombo = ComboBox<String>().apply { accessibleText = "Difficulté" }
    private val soundCombo = ComboBox<POVoice>().apply {
        items.addAll(PODevice.PO_14.voices)
        converter = object : StringConverter<POVoice>() {
            override fun toString(v: POVoice?) = v?.let { "${it.displayName} (${it.number})" } ?: ""
            override fun fromString(s: String) = null
        }
        accessibleText = "Son du PO-14"
    }
    private val templateCombo = ComboBox<Po14PatternTemplate>().apply {
        items.addAll(BuiltInPo14Templates.all())
        converter = object : StringConverter<Po14PatternTemplate>() {
            override fun toString(t: Po14PatternTemplate?) = t?.name ?: ""
            override fun fromString(s: String) = null
        }
        accessibleText = "Template de pattern"
    }

    private val noteCombos = Array(16) { ComboBox<String>() }
    private val octaveSpinners = Array(16) { Spinner<Int>(0, 8, 3) }
    private val sharpChecks = Array(16) { CheckBox("#") }

    fun show(): PO14Pattern? {
        val dialog = Dialog<PO14Pattern?>()
        dialog.title = if (model.isEditMode) "Éditer le pattern PO-14" else "Nouveau pattern PO-14"
        dialog.headerText = null
        dialog.isResizable = true
        val content = buildContent()
        dialog.dialogPane.content = content
        dialog.dialogPane.prefWidth = sz.dialogWidth
        dialog.dialogPane.prefHeight = sz.dialogHeight

        content.prefHeightProperty().bind(dialog.dialogPane.heightProperty())

        val prefs = SharedAccessibilityPreferences.instance
        dialog.dialogPane.sceneProperty().addListener { _, _, scene ->
            scene?.let { ThemeManager.apply(it, prefs) }
        }

        val saveType = ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE)
        dialog.dialogPane.buttonTypes.addAll(saveType, ButtonType.CANCEL)

        val saveButton = dialog.dialogPane.lookupButton(saveType)
        saveButton.isDisable = nameField.text.trim().isEmpty()
        nameField.textProperty().addListener { _, _, v ->
            saveButton.isDisable = v.trim().isEmpty()
        }

        dialog.setResultConverter { bt ->
            if (bt == saveType) { syncModelFromUI(); model.buildPattern() } else null
        }

        return dialog.showAndWait().orElse(null)
    }

    private fun buildContent(): VBox {
        difficultyCombo.items.addAll("-", "beginner", "intermediate", "advanced")
        difficultyCombo.value = "-"
        soundCombo.value = model.sound

        val metaRow1 = HBox(10.0, Label("Nom *").apply { labelFor = nameField }, nameField).apply {
            alignment = Pos.CENTER_LEFT
            HBox.setHgrow(nameField, Priority.ALWAYS)
        }
        val metaRow2 = HBox(10.0,
            Label("N° pattern").apply { labelFor = patternNumberSpinner }, patternNumberSpinner,
            Label("BPM").apply { labelFor = bpmField }, bpmField,
            Label("Difficulté").apply { labelFor = difficultyCombo }, difficultyCombo,
            Label("Son").apply { labelFor = soundCombo }, soundCombo
        ).apply { alignment = Pos.CENTER_LEFT }

        val applyTemplateBtn = Button("Charger").apply {
            accessibleText = "Charger le template sélectionné"
            setOnAction {
                templateCombo.value?.let { template ->
                    model.applyTemplate(template)
                    soundCombo.value = model.sound
                    if (model.bpm.isNotEmpty()) bpmField.text = model.bpm
                    refreshStepControlsFromModel()
                }
            }
        }
        val templateRow = HBox(8.0, Label("Template"), templateCombo, applyTemplateBtn).apply {
            alignment = Pos.CENTER_LEFT
        }

        val metaBox = VBox(8.0, metaRow1, metaRow2, templateRow).apply {
            padding = Insets(0.0, 0.0, 4.0, 0.0)
        }

        val stepsLabel = Label("Notes (touches blanches uniquement — # = effet 15 \"half note up\" déclenché en direct)").apply {
            styleClass.add("h2")
            isWrapText = true
        }

        val stepsGrid = GridPane().apply {
            hgap = sz.spacing
            vgap = sz.spacing
            add(Label("Step"), 0, 0)
            add(Label("Note"), 1, 0)
            add(Label("Octave"), 2, 0)
            add(Label("#"), 3, 0)
            for (i in 0 until 16) {
                val step = i + 1
                add(Label(step.toString()).apply { prefWidth = sz.toggleSize; alignment = Pos.CENTER }, 0, step)

                noteCombos[i].items.addAll("-", "C", "D", "E", "F", "G", "A", "B")
                noteCombos[i].value = "-"
                noteCombos[i].accessibleText = "Note du step $step"
                add(noteCombos[i], 1, step)

                octaveSpinners[i].isDisable = true
                add(octaveSpinners[i], 2, step)

                sharpChecks[i].isDisable = true
                add(sharpChecks[i], 3, step)

                noteCombos[i].valueProperty().addListener { _, _, v ->
                    val active = v != null && v != "-"
                    octaveSpinners[i].isDisable = !active
                    sharpChecks[i].isDisable = !active
                }
            }
        }

        if (model.isEditMode) {
            nameField.text = model.name
            patternNumberSpinner.valueFactory.value = model.patternNumber
            if (model.bpm.isNotEmpty()) bpmField.text = model.bpm
            difficultyCombo.value = model.difficulty
            refreshStepControlsFromModel()
        }

        val scrollPane = ScrollPane(stepsGrid).apply {
            isFitToWidth = true
            minHeight = sz.scrollMinHeight
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        return VBox(10.0, metaBox, Separator(), stepsLabel, scrollPane).apply {
            padding = Insets(sz.padding)
        }
    }

    private fun refreshStepControlsFromModel() {
        for (i in 0 until 16) {
            val step = i + 1
            val note = model.steps[step]
            if (note == null) {
                noteCombos[i].value = "-"
            } else {
                noteCombos[i].value = note.pitch.letter
                octaveSpinners[i].valueFactory.value = note.octave
                sharpChecks[i].isSelected = note.halfToneUp
            }
        }
    }

    private fun syncModelFromUI() {
        model.name = nameField.text
        model.patternNumber = patternNumberSpinner.value
        model.bpm = bpmField.text
        model.difficulty = difficultyCombo.value
        model.sound = soundCombo.value ?: model.sound

        for (i in 0 until 16) {
            val step = i + 1
            val letter = noteCombos[i].value
            if (letter == null || letter == "-") {
                model.clearStep(step)
            } else {
                val pitch = Pitch.fromLetter(letter) ?: continue
                model.setStep(
                    step,
                    PO14Step(
                        pitch = pitch,
                        octave = octaveSpinners[i].value,
                        halfToneUp = sharpChecks[i].isSelected
                    )
                )
            }
        }
    }
}
