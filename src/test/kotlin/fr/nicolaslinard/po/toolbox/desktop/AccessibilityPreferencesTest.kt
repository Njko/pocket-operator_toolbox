package fr.nicolaslinard.po.toolbox.desktop

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccessibilityPreferencesTest {

    private val testDir = File("build/test-output/prefs")
    private val testFile = File(testDir, "preferences.json")

    @BeforeEach
    fun setup() {
        testDir.mkdirs()
        testFile.delete()
    }

    @AfterEach
    fun cleanup() {
        testFile.delete()
    }

    // --- Default values ---

    @Test
    fun `should have dark theme by default`() {
        val prefs = AccessibilityPreferences()
        assertEquals("dark", prefs.theme)
    }

    @Test
    fun `should have no color blind mode by default`() {
        val prefs = AccessibilityPreferences()
        assertEquals("none", prefs.colorBlindMode)
    }

    @Test
    fun `should not reduce motion by default`() {
        val prefs = AccessibilityPreferences()
        assertFalse(prefs.reduceMotion)
    }

    @Test
    fun `should have font size multiplier 1 by default`() {
        val prefs = AccessibilityPreferences()
        assertEquals(1.0, prefs.fontSizeMultiplier)
    }

    // --- Save and load ---

    @Test
    fun `should save and load preferences`() {
        val prefs = AccessibilityPreferences()
        prefs.theme = "light"
        prefs.colorBlindMode = "deuteranopia"
        prefs.reduceMotion = true
        prefs.fontSizeMultiplier = 1.5
        prefs.save(testFile)

        val loaded = AccessibilityPreferences()
        loaded.load(testFile)
        assertEquals("light", loaded.theme)
        assertEquals("deuteranopia", loaded.colorBlindMode)
        assertTrue(loaded.reduceMotion)
        assertEquals(1.5, loaded.fontSizeMultiplier)
    }

    @Test
    fun `should keep defaults when file does not exist`() {
        val prefs = AccessibilityPreferences()
        prefs.load(testFile)
        assertEquals("dark", prefs.theme)
        assertFalse(prefs.reduceMotion)
    }

    // --- Reset ---

    @Test
    fun `should reset to defaults`() {
        val prefs = AccessibilityPreferences()
        prefs.theme = "high-contrast"
        prefs.reduceMotion = true
        prefs.fontSizeMultiplier = 2.0
        prefs.reset()
        assertEquals("dark", prefs.theme)
        assertEquals("none", prefs.colorBlindMode)
        assertFalse(prefs.reduceMotion)
        assertEquals(1.0, prefs.fontSizeMultiplier)
    }

    // --- Validation ---

    @Test
    fun `should clamp font size multiplier to valid range`() {
        val prefs = AccessibilityPreferences()
        prefs.fontSizeMultiplier = 3.0
        assertEquals(2.0, prefs.fontSizeMultiplier)
        prefs.fontSizeMultiplier = 0.1
        assertEquals(0.8, prefs.fontSizeMultiplier)
    }

    @Test
    fun `should accept valid themes`() {
        val prefs = AccessibilityPreferences()
        for (theme in listOf("dark", "light", "high-contrast")) {
            prefs.theme = theme
            assertEquals(theme, prefs.theme)
        }
    }

    @Test
    fun `should reject invalid theme and keep previous`() {
        val prefs = AccessibilityPreferences()
        prefs.theme = "invalid"
        assertEquals("dark", prefs.theme)
    }

    @Test
    fun `should accept valid color blind modes`() {
        val prefs = AccessibilityPreferences()
        for (mode in listOf("none", "deuteranopia", "protanopia", "tritanopia")) {
            prefs.colorBlindMode = mode
            assertEquals(mode, prefs.colorBlindMode)
        }
    }

    @Test
    fun `should reject invalid color blind mode`() {
        val prefs = AccessibilityPreferences()
        prefs.colorBlindMode = "invalid"
        assertEquals("none", prefs.colorBlindMode)
    }
}
