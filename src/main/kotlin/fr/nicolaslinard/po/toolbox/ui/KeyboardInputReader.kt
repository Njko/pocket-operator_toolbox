package fr.nicolaslinard.po.toolbox.ui

/**
 * RED Phase - Stub implementation for compilation
 *
 * Platform-agnostic keyboard input reader for interactive editing.
 * Detects special keys (arrows, space, enter, escape, ctrl combos) and character input.
 */
interface KeyboardInputReader {
    /**
     * Read a single key press. Returns null if no key is available.
     */
    fun readKey(): Key?

    /**
     * Check if the current terminal supports interactive mode.
     * Returns false if running in basic terminal, SSH without TTY, or other limited environments.
     */
    fun isInteractiveModeSupported(): Boolean
}

/**
 * Represents a key press detected by the keyboard reader
 */
sealed class Key {
    object ArrowLeft : Key()
    object ArrowRight : Key()
    object Space : Key()
    object Enter : Key()
    object Escape : Key()
    object CtrlZ : Key()
    object CtrlY : Key()
    data class Character(val char: Char) : Key()
}

/**
 * Mordant-based keyboard reader implementation
 *
 * Uses Mordant's terminal capabilities to detect keyboard input.
 * Fallback to text mode if raw keyboard input is not supported.
 *
 * Note: Mordant does not currently provide raw keyboard input APIs.
 * This implementation always returns false for interactive mode support
 * as a safe default. Platform-specific implementations (like JLine3) could
 * be added in the future for full arrow key support.
 */
class MordantKeyboardReader : KeyboardInputReader {
    override fun readKey(): Key? {
        // Mordant doesn't expose raw keyboard input APIs
        // Return null indicating no key available
        return null
    }

    override fun isInteractiveModeSupported(): Boolean {
        // Mordant doesn't support raw keyboard input
        // Return false to trigger fallback to text mode
        return false
    }
}
