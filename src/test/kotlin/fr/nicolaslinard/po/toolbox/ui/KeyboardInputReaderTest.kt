package fr.nicolaslinard.po.toolbox.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * TDD RED Phase: Tests for KeyboardInputReader
 *
 * Tests keyboard input detection for interactive grid editing.
 * Verifies arrow keys, special keys, and interactive mode support.
 */
class KeyboardInputReaderTest {

    @Test
    fun `should detect arrow left key`() {
        // Create a mock keyboard reader that simulates left arrow press
        val reader = MockKeyboardReader(Key.ArrowLeft)

        val key = reader.readKey()

        assertNotNull(key)
        assertTrue(key is Key.ArrowLeft)
    }

    @Test
    fun `should detect arrow right key`() {
        val reader = MockKeyboardReader(Key.ArrowRight)

        val key = reader.readKey()

        assertNotNull(key)
        assertTrue(key is Key.ArrowRight)
    }

    @Test
    fun `should detect space key`() {
        val reader = MockKeyboardReader(Key.Space)

        val key = reader.readKey()

        assertNotNull(key)
        assertTrue(key is Key.Space)
    }

    @Test
    fun `should detect enter key`() {
        val reader = MockKeyboardReader(Key.Enter)

        val key = reader.readKey()

        assertNotNull(key)
        assertTrue(key is Key.Enter)
    }

    @Test
    fun `should detect escape key`() {
        val reader = MockKeyboardReader(Key.Escape)

        val key = reader.readKey()

        assertNotNull(key)
        assertTrue(key is Key.Escape)
    }

    @Test
    fun `should detect Ctrl+Z key`() {
        val reader = MockKeyboardReader(Key.CtrlZ)

        val key = reader.readKey()

        assertNotNull(key)
        assertTrue(key is Key.CtrlZ)
    }

    @Test
    fun `should detect Ctrl+Y key`() {
        val reader = MockKeyboardReader(Key.CtrlY)

        val key = reader.readKey()

        assertNotNull(key)
        assertTrue(key is Key.CtrlY)
    }

    @Test
    fun `should detect character keys`() {
        val reader = MockKeyboardReader(Key.Character('a'))

        val key = reader.readKey()

        assertNotNull(key)
        assertTrue(key is Key.Character)
        assertEquals('a', (key as Key.Character).char)
    }

    @Test
    fun `should return null when no key pressed`() {
        val reader = MockKeyboardReader(null)

        val key = reader.readKey()

        assertNull(key)
    }

    @Test
    fun `should report interactive mode support correctly`() {
        // Mock reader that supports interactive mode
        val supportedReader = MockKeyboardReader(Key.Space, supportsInteractive = true)
        assertTrue(supportedReader.isInteractiveModeSupported())

        // Mock reader that doesn't support interactive mode
        val unsupportedReader = MockKeyboardReader(Key.Space, supportsInteractive = false)
        assertFalse(unsupportedReader.isInteractiveModeSupported())
    }
}

/**
 * Mock implementation of KeyboardInputReader for testing
 */
private class MockKeyboardReader(
    private val keyToReturn: Key?,
    private val supportsInteractive: Boolean = true
) : KeyboardInputReader {
    override fun readKey(): Key? = keyToReturn
    override fun isInteractiveModeSupported(): Boolean = supportsInteractive
}
