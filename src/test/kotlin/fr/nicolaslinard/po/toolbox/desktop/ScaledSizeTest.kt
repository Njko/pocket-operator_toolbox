package fr.nicolaslinard.po.toolbox.desktop

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScaledSizeTest {

    @Test
    fun `should return base size at 1x multiplier`() {
        val prefs = AccessibilityPreferences()
        val scaled = ScaledSize(prefs)
        assertEquals(22.0, scaled.stepWidth)
    }

    @Test
    fun `should scale step width at 1_5x`() {
        val prefs = AccessibilityPreferences().apply { fontSizeMultiplier = 1.5 }
        val scaled = ScaledSize(prefs)
        assertEquals(33.0, scaled.stepWidth)
    }

    @Test
    fun `should scale voice label width at 2x`() {
        val prefs = AccessibilityPreferences().apply { fontSizeMultiplier = 2.0 }
        val scaled = ScaledSize(prefs)
        assertEquals(260.0, scaled.voiceLabelWidth)
    }

    @Test
    fun `should scale dialog dimensions`() {
        val prefs = AccessibilityPreferences().apply { fontSizeMultiplier = 1.5 }
        val scaled = ScaledSize(prefs)
        assertEquals(1110.0, scaled.dialogWidth)
    }

    @Test
    fun `should derive editHeaderSpacerWidth from editVoiceLabelWidth`() {
        val prefs = AccessibilityPreferences().apply { fontSizeMultiplier = 2.0 }
        val scaled = ScaledSize(prefs)
        assertEquals(scaled.editVoiceLabelWidth + scaled.scale(8.0), scaled.editHeaderSpacerWidth)
    }

    @Test
    fun `should provide scale method for arbitrary values`() {
        val prefs = AccessibilityPreferences().apply { fontSizeMultiplier = 2.0 }
        val scaled = ScaledSize(prefs)
        assertEquals(200.0, scaled.scale(100.0))
    }
}
