package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PODevice
import fr.nicolaslinard.po.toolbox.models.POVoice
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.util.StringConverter

class NewPatternDialog(existingPattern: PO12Pattern? = null) {

    val model = NewPatternDialogModel(existingPattern)
    private val sz = ScaledSize()

    private val nameField = TextField().apply { promptText = "Nom du pattern (requis)"; accessibleText = "Nom du pattern" }
    private val patternNumberSpinner = Spinner<Int>(1, 16, 1).apply { prefWidth = 70.0; accessibleText = "Numéro de pattern" }
    private val bpmField = TextField().apply { promptText = "ex: 120"; prefWidth = 70.0; accessibleText = "BPM" }
    private val difficultyCombo = ComboBox<String>().apply { accessibleText = "Difficulté" }
    private val deviceCombo = ComboBox<PODevice>().apply {
        items.addAll(PODevice.entries)
        value = PODevice.PO_12
        accessibleText = "Modèle Pocket Operator"
        converter = object : StringConverter<PODevice>() {
            override fun toString(d: PODevice?) = d?.let { "${it.modelId} ${it.deviceName}" } ?: ""
            override fun fromString(s: String) = null
        }
    }
    private val voiceCombo = ComboBox<PO12DrumVoice>()
    private val barCountSpinner = Spinner<Int>(1, 16, 1).apply { prefWidth = 70.0; accessibleText = "Nombre de mesures" }
    private val barLabel = Label("Mesure 1 / 1")
    private val voicesBox = VBox(4.0)
    private val voiceRows = LinkedHashMap<PO12DrumVoice, Array<ToggleButton>>()

    fun show(): PO12Pattern? = showMultiBar()?.firstPattern

    fun showMultiBar(): PatternDialogResult? {
        val dialog = Dialog<PatternDialogResult?>()
        dialog.title = if (model.isEditMode) "Éditer le pattern" else "Nouveau pattern"
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
            if (bt == saveType) { syncModelFromUI(); model.buildResult() } else null
        }

        return dialog.showAndWait().orElse(null)
    }

    private fun buildContent(): VBox {
        // Metadata
        difficultyCombo.items.addAll("-", "beginner", "intermediate", "advanced")
        difficultyCombo.value = "-"

        val metaRow1 = HBox(10.0, Label("Nom *").apply { labelFor = nameField }, nameField).apply {
            alignment = Pos.CENTER_LEFT
            HBox.setHgrow(nameField, Priority.ALWAYS)
        }
        val metaRow2 = HBox(10.0,
            Label("Modèle").apply { labelFor = deviceCombo }, deviceCombo,
            Label("N° pattern").apply { labelFor = patternNumberSpinner }, patternNumberSpinner,
            Label("BPM").apply { labelFor = bpmField }, bpmField,
            Label("Difficulté").apply { labelFor = difficultyCombo }, difficultyCombo
        ).apply { alignment = Pos.CENTER_LEFT }

        // When device changes, rebuild voice combo with device-specific voices
        deviceCombo.valueProperty().addListener { _, _, newDevice ->
            if (newDevice != null) {
                model.device = newDevice
                rebuildVoiceGrid()
            }
        }

        // Multi-bar navigation
        val prevBarBtn = Button("◀").apply {
            accessibleText = "Mesure précédente"
            setOnAction { navigateBar(-1) }
        }
        val nextBarBtn = Button("▶").apply {
            accessibleText = "Mesure suivante"
            setOnAction { navigateBar(1) }
        }
        barCountSpinner.valueFactory.valueProperty().addListener { _, _, newCount ->
            model.barCount = newCount
            updateBarLabel()
        }
        val barNavRow = HBox(8.0,
            Label("Mesures").apply { labelFor = barCountSpinner }, barCountSpinner,
            Separator(javafx.geometry.Orientation.VERTICAL),
            prevBarBtn, barLabel, nextBarBtn
        ).apply { alignment = Pos.CENTER_LEFT }

        val metaBox = VBox(8.0, metaRow1, metaRow2, barNavRow).apply {
            padding = Insets(0.0, 0.0, 4.0, 0.0)
        }

        // Voice selector
        voiceCombo.prefWidth = 200.0
        voiceCombo.converter = object : StringConverter<PO12DrumVoice>() {
            override fun toString(v: PO12DrumVoice?) = v?.let {
                val device = deviceCombo.value ?: PODevice.PO_12
                val deviceVoice = device.getVoiceByNumber(it.poNumber)
                deviceVoice?.let { dv -> "${dv.displayName} (${dv.number})" } ?: "${it.displayName} (${it.poNumber})"
            } ?: ""
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
        val headerBox = HBox(sz.spacing).apply {
            children.add(Label("").apply { prefWidth = sz.editHeaderSpacerWidth })
            (1..16).forEach { step ->
                children.add(Label(step.toString()).apply {
                    prefWidth = sz.toggleSize
                    alignment = Pos.CENTER
                    styleClass.add("step-header")
                })
            }
        }

        val scrollPane = ScrollPane(voicesBox).apply {
            isFitToWidth = true
            minHeight = sz.scrollMinHeight
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
            padding = Insets(sz.padding)
        }
    }

    private fun addVoiceRow(voice: PO12DrumVoice, activeSteps: List<Int> = emptyList()) {
        val toggles = Array(16) { i ->
            val stepNumber = i + 1
            val isActive = stepNumber in activeSteps
            ToggleButton(if (isActive) "●" else "·").apply {
                prefWidth = sz.toggleSize
                prefHeight = sz.toggleSize
                isSelected = isActive
                accessibleText = "${voice.displayName} step $stepNumber ${if (isActive) "actif" else "inactif"}"
                selectedProperty().addListener { _, _, on ->
                    text = if (on) "●" else "·"
                    accessibleText = "${voice.displayName} step $stepNumber ${if (on) "actif" else "inactif"}"
                }
                tooltip = Tooltip("Step $stepNumber")
            }
        }
        voiceRows[voice] = toggles

        val removeBtn = Button("×").apply {
            styleClass.add("danger")
            accessibleText = "Supprimer ${voice.displayName}"
            setOnAction {
                model.removeVoice(voice)
                voiceRows.remove(voice)
                voicesBox.children.removeIf { it.userData == voice }
                refreshVoiceCombo()
            }
        }

        val row = HBox(sz.spacing).apply {
            userData = voice
            alignment = Pos.CENTER_LEFT
            val deviceVoice = deviceCombo.value?.getVoiceByNumber(voice.poNumber)
            val voiceName = deviceVoice?.displayName ?: voice.displayName
            children.add(Label("$voiceName (%02d)".format(voice.poNumber)).apply {
                prefWidth = sz.editVoiceLabelWidth
                alignment = Pos.CENTER_RIGHT
                styleClass.add("voice-label")
                accessibleText = "$voiceName, son numéro ${voice.poNumber}"
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

    private fun navigateBar(delta: Int) {
        syncModelFromUI()
        val newIndex = (model.currentBarIndex + delta).coerceIn(0, model.barCount - 1)
        model.switchBar(newIndex)
        rebuildVoiceGrid()
        updateBarLabel()
    }

    private fun updateBarLabel() {
        barLabel.text = "Mesure ${model.currentBarIndex + 1} / ${model.barCount}"
    }

    private fun rebuildVoiceGrid() {
        voicesBox.children.clear()
        voiceRows.clear()
        for ((voice, steps) in model.voices) {
            addVoiceRow(voice, steps)
        }
        refreshVoiceCombo()
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
