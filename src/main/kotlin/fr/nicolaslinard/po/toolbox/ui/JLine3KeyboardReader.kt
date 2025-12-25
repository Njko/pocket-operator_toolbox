package fr.nicolaslinard.po.toolbox.ui

import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.AttributedString
import org.jline.utils.InfoCmp
import java.io.Closeable

/**
 * JLine3-based keyboard input reader for cross-platform raw keyboard input.
 *
 * Features:
 * - Raw mode (non-canonical input - no Enter key needed)
 * - Cross-platform escape sequence parsing (Windows/Linux/macOS)
 * - TTY detection for graceful degradation
 * - Resource cleanup with shutdown hook
 * - Support for arrow keys, special keys, and Ctrl combinations
 *
 * Phase 7.1 - Milestone 7.1.1: Basic Integration
 */
class JLine3KeyboardReader : KeyboardInputReader, Closeable {
    private val terminal: Terminal
    private var originalAttributes: org.jline.terminal.Attributes? = null
    private var isTerminalOpen = true

    init {
        // Create JLine3 terminal
        terminal = TerminalBuilder.builder()
            .system(true)
            .build()

        // Save original attributes for restoration
        originalAttributes = terminal.attributes

        // Enter raw mode (non-canonical input, no echo)
        terminal.enterRawMode()

        // Add shutdown hook for crash recovery
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                close()
            } catch (e: Exception) {
                // Ignore errors in shutdown hook
            }
        })
    }

    /**
     * Read a single key from the terminal without blocking indefinitely.
     *
     * Parses escape sequences for arrow keys and special keys.
     * Returns null if no key is available (non-blocking).
     */
    override fun readKey(): Key? {
        if (!isTerminalOpen) {
            return null
        }

        return try {
            val reader = terminal.reader()

            // Check if input is available (non-blocking)
            if (!reader.ready()) {
                return null
            }

            // Read first character
            val firstChar = reader.read()
            if (firstChar == -1) {
                return null // End of stream
            }

            val char = firstChar.toChar()

            // Handle escape sequences (arrow keys, etc.)
            if (char == '\u001b') { // ESC character
                // Arrow keys send ESC + [ + letter in quick succession
                // We need to read the next characters to determine if it's an arrow key
                // or just the ESC key pressed alone

                // Small delay to let the rest of the sequence arrive
                Thread.sleep(20)

                // Check if more input is available
                if (reader.ready()) {
                    val secondChar = reader.read()
                    if (secondChar == -1) {
                        return Key.Escape // End of stream
                    }

                    if (secondChar == '['.code) {
                        // This is an ANSI escape sequence \x1b[X
                        // Read the final character (blocking)
                        val thirdChar = reader.read()
                        if (thirdChar == -1) {
                            return Key.Escape // Incomplete sequence
                        }

                        return parseEscapeSequence("[${thirdChar.toChar()}")
                    } else {
                        // Not a recognized escape sequence, treat as ESC
                        return Key.Escape
                    }
                } else {
                    // No more input available, just the ESC key by itself
                    return Key.Escape
                }
            }

            // Handle Ctrl combinations
            when (firstChar) {
                0x1A -> return Key.CtrlZ // Ctrl+Z
                0x19 -> return Key.CtrlY // Ctrl+Y
                0x0D, 0x0A -> return Key.Enter // Enter (CR or LF)
                0x20 -> return Key.Space // Space
            }

            // Handle regular characters
            if (firstChar in 32..126) {
                return Key.Character(char)
            }

            // Unknown key
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if interactive mode is supported (TTY detection).
     *
     * Returns true if the terminal is a TTY (real terminal),
     * false if input is piped/redirected.
     */
    override fun isInteractiveModeSupported(): Boolean {
        // Check if terminal is a dumb terminal or not a TTY
        if (terminal.type.equals("dumb", ignoreCase = true)) {
            return false
        }

        // Check if we have a real TTY
        // In test environments or piped input, this will be false
        return try {
            // If we can get terminal size, we likely have a TTY
            val size = terminal.size
            size.rows > 0 && size.columns > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Close the terminal and restore original attributes.
     *
     * Safe to call multiple times.
     */
    override fun close() {
        if (!isTerminalOpen) {
            return // Already closed
        }

        try {
            // Restore original terminal attributes
            originalAttributes?.let {
                terminal.attributes = it
            }

            // Close terminal
            terminal.close()
            isTerminalOpen = false
        } catch (e: Exception) {
            // Ignore errors during cleanup
        }
    }

    /**
     * Parse ANSI escape sequences into Key objects.
     *
     * ANSI Escape Sequences:
     * - [D = Arrow Left
     * - [C = Arrow Right
     * - [A = Arrow Up (not used, returns null)
     * - [B = Arrow Down (not used, returns null)
     */
    private fun parseEscapeSequence(sequence: CharSequence): Key? {
        return when (sequence.toString()) {
            "[D" -> Key.ArrowLeft
            "[C" -> Key.ArrowRight
            // Arrow Up/Down not in our Key enum, ignore
            "[A", "[B" -> null
            else -> null
        }
    }
}
