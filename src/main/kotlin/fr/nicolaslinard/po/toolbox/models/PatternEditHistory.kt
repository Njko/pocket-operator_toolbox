package fr.nicolaslinard.po.toolbox.models

/**
 * GREEN Phase - Minimal implementation to pass tests
 *
 * Command Pattern implementation for reversible pattern editing operations.
 * Manages undo/redo history with configurable size limit.
 */
class PatternEditHistory(private val maxHistorySize: Int = 50) {

    private val undoStack = mutableListOf<EditCommand>()
    private val redoStack = mutableListOf<EditCommand>()

    fun execute(command: EditCommand) {
        // Add to undo stack
        undoStack.add(command)

        // Limit history size
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }

        // Clear redo stack when new command is executed
        redoStack.clear()
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun undo(): EditCommand? {
        if (undoStack.isEmpty()) return null

        val command = undoStack.removeLast()
        redoStack.add(command)
        return command
    }

    fun redo(): EditCommand? {
        if (redoStack.isEmpty()) return null

        val command = redoStack.removeLast()
        undoStack.add(command)
        return command
    }

    fun getUndoDescription(): String? = undoStack.lastOrNull()?.describe()

    fun getRedoDescription(): String? = redoStack.lastOrNull()?.describe()

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

/**
 * Command interface for reversible pattern editing operations
 */
interface EditCommand {
    fun execute(pattern: MutableMap<PO12DrumVoice, List<Int>>)
    fun undo(pattern: MutableMap<PO12DrumVoice, List<Int>>)
    fun describe(): String
}

/**
 * Command for adding a new voice to the pattern
 */
data class AddVoiceCommand(
    val voice: PO12DrumVoice,
    val steps: List<Int>
) : EditCommand {
    override fun execute(pattern: MutableMap<PO12DrumVoice, List<Int>>) {
        pattern[voice] = steps
    }

    override fun undo(pattern: MutableMap<PO12DrumVoice, List<Int>>) {
        pattern.remove(voice)
    }

    override fun describe(): String {
        val stepsStr = steps.joinToString(", ")
        return "Added ${voice.displayName}: $stepsStr"
    }
}

/**
 * Command for removing a voice from the pattern
 */
data class RemoveVoiceCommand(
    val voice: PO12DrumVoice,
    val previousSteps: List<Int>
) : EditCommand {
    override fun execute(pattern: MutableMap<PO12DrumVoice, List<Int>>) {
        pattern.remove(voice)
    }

    override fun undo(pattern: MutableMap<PO12DrumVoice, List<Int>>) {
        pattern[voice] = previousSteps
    }

    override fun describe(): String {
        return "Removed ${voice.displayName}"
    }
}

/**
 * Command for modifying an existing voice in the pattern
 */
data class ModifyVoiceCommand(
    val voice: PO12DrumVoice,
    val oldSteps: List<Int>,
    val newSteps: List<Int>
) : EditCommand {
    override fun execute(pattern: MutableMap<PO12DrumVoice, List<Int>>) {
        pattern[voice] = newSteps
    }

    override fun undo(pattern: MutableMap<PO12DrumVoice, List<Int>>) {
        pattern[voice] = oldSteps
    }

    override fun describe(): String {
        val newStepsStr = newSteps.joinToString(", ")
        return "Modified ${voice.displayName}: $newStepsStr"
    }
}
