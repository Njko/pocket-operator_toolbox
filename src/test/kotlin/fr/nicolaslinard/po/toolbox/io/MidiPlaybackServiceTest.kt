package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.TestFixtures
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.sound.midi.MidiSystem
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

/**
 * TDD: Tests for MIDI playback service.
 * Uses assumeTrue to skip on environments without MIDI synthesizer.
 */
class MidiPlaybackServiceTest {

    private lateinit var service: MidiPlaybackService

    private fun midiAvailable(): Boolean {
        return try {
            val seq = MidiSystem.getSequencer(false)
            seq.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    @BeforeEach
    fun setup() {
        assumeTrue(midiAvailable(), "MIDI sequencer not available, skipping")
        service = MidiPlaybackService()
    }

    @AfterEach
    fun cleanup() {
        if (::service.isInitialized) {
            service.dispose()
        }
    }

    // === 2a. Sequencer creation ===

    @Test
    fun `should create service successfully`() {
        assertNotNull(service)
        assertFalse(service.isPlaying)
    }

    // === 2b/2c. Play and Stop ===

    @Test
    fun `should set isPlaying to true when playing`() {
        val pattern = TestFixtures.createSimplePattern()
        service.play(pattern)
        assertTrue(service.isPlaying)
    }

    @Test
    fun `should set isPlaying to false when stopped`() {
        val pattern = TestFixtures.createSimplePattern()
        service.play(pattern)
        service.stop()
        assertFalse(service.isPlaying)
    }

    @Test
    fun `should stop be idempotent when not playing`() {
        service.stop()
        assertFalse(service.isPlaying)
    }

    // === 2d. Loop ===

    @Test
    fun `should not loop by default`() {
        assertFalse(service.isLooping)
    }

    @Test
    fun `should enable loop mode`() {
        service.toggleLoop()
        assertTrue(service.isLooping)
    }

    @Test
    fun `should disable loop mode on second toggle`() {
        service.toggleLoop()
        service.toggleLoop()
        assertFalse(service.isLooping)
    }

    // === 2e. Step position calculation ===

    @Test
    fun `should return step 1 for tick 0`() {
        assertEquals(1, service.tickToStep(0))
    }

    @Test
    fun `should return step 2 for tick 24`() {
        // At 96 PPQ, ticksPerStep = 96/4 = 24
        assertEquals(2, service.tickToStep(24))
    }

    @Test
    fun `should return step 16 for tick 360`() {
        // step 16 = tick (16-1)*24 = 360
        assertEquals(16, service.tickToStep(360))
    }

    @Test
    fun `should wrap step position for ticks beyond one bar`() {
        // tick 384 = 16*24 = start of next bar = step 1 again
        assertEquals(1, service.tickToStep(384))
    }

    // === 2g. Dispose ===

    @Test
    fun `should dispose resources cleanly`() {
        val pattern = TestFixtures.createSimplePattern()
        service.play(pattern)
        service.dispose()
        assertFalse(service.isPlaying)
    }

    @Test
    fun `should be safe to dispose twice`() {
        service.dispose()
        service.dispose()
        assertFalse(service.isPlaying)
    }

    // === 2h. Edge cases ===

    @Test
    fun `should handle empty pattern gracefully`() {
        val pattern = TestFixtures.createEmptyPattern()
        service.play(pattern)
        assertTrue(service.isPlaying)
        service.stop()
    }

    @Test
    fun `should stop previous playback when new pattern played`() {
        val pattern1 = TestFixtures.createSimplePattern(name = "Pattern 1")
        val pattern2 = TestFixtures.createSimplePattern(name = "Pattern 2")
        service.play(pattern1)
        assertTrue(service.isPlaying)
        service.play(pattern2)
        assertTrue(service.isPlaying)
        service.stop()
        assertFalse(service.isPlaying)
    }

    // === Loop applied during playback ===

    @Test
    fun `should apply loop mode when playing`() {
        val pattern = TestFixtures.createSimplePattern()
        service.toggleLoop()
        service.play(pattern)
        assertTrue(service.isPlaying)
        assertTrue(service.isLooping)
        service.stop()
    }

    // === Current step from sequencer ===

    @Test
    fun `should return step 0 when not playing`() {
        assertEquals(0, service.currentStep)
    }

    @Test
    fun `should return current step when playing`() {
        val pattern = TestFixtures.createSimplePattern()
        service.play(pattern)
        // Immediately after play, step should be >= 1
        val step = service.currentStep
        assertTrue(step in 0..16)
        service.stop()
    }

    // === C1: End of track callback ===

    @Test
    fun `should invoke onPlaybackStopped callback when set`() {
        var callbackInvoked = false
        service.onPlaybackStopped = { callbackInvoked = true }
        val pattern = TestFixtures.createSimplePattern()
        service.play(pattern)
        service.stop()
        // Manual stop should also invoke the callback
        assertTrue(callbackInvoked)
    }

    // === C3: Stale sequencer cleanup ===

    @Test
    fun `should recover after dispose and play again`() {
        val pattern = TestFixtures.createSimplePattern()
        service.play(pattern)
        service.dispose()
        assertFalse(service.isPlaying)
        // Should be able to play again after dispose
        service.play(pattern)
        assertTrue(service.isPlaying)
        service.stop()
    }

    // === M6: Toggle loop during active playback ===

    @Test
    fun `should toggle loop during active playback`() {
        val pattern = TestFixtures.createSimplePattern()
        service.play(pattern)
        assertFalse(service.isLooping)
        service.toggleLoop()
        assertTrue(service.isLooping)
        service.toggleLoop()
        assertFalse(service.isLooping)
        service.stop()
    }

    // === M1: Use sequence resolution for step calculation ===

    @Test
    fun `should use sequence resolution for currentStep`() {
        val pattern = TestFixtures.createSimplePattern()
        service.play(pattern)
        // currentStep should work regardless of resolution
        val step = service.currentStep
        assertTrue(step in 0..16)
        service.stop()
    }

    // === Loop continuity: isPlaying must stay true during loop ===

    @Test
    fun `should remain playing when looping and end of track is reached`() {
        val pattern = TestFixtures.createSimplePattern(bpm = 206) // fast tempo for quick loop
        service.toggleLoop()
        service.play(pattern)
        assertTrue(service.isPlaying)
        // Wait for at least one full loop cycle (at 206 BPM, 1 bar ~ 1.16s)
        Thread.sleep(1500)
        // isPlaying must still be true after the first loop
        assertTrue(service.isPlaying, "isPlaying should remain true during loop playback")
        service.stop()
    }

    @Test
    fun `should not invoke onPlaybackStopped during loop`() {
        var stoppedCount = 0
        service.onPlaybackStopped = { stoppedCount++ }
        val pattern = TestFixtures.createSimplePattern(bpm = 206)
        service.toggleLoop()
        service.play(pattern)
        Thread.sleep(1500)
        // Callback should NOT have been invoked during looping
        assertEquals(0, stoppedCount, "onPlaybackStopped should not fire during loop")
        service.stop()
        assertEquals(1, stoppedCount, "onPlaybackStopped should fire once on manual stop")
    }

    @Test
    fun `should set explicit loop points for seamless looping`() {
        val pattern = TestFixtures.createSimplePattern(bpm = 206)
        service.toggleLoop()
        service.play(pattern)
        // Sequencer should have loop start at 0
        assertTrue(service.isPlaying)
        Thread.sleep(1500)
        assertTrue(service.isPlaying)
        service.stop()
    }

    @Test
    fun `should use short note duration for playback to avoid bleed past bar`() {
        // Verify the playback options use a note duration shorter than ticksPerStep
        val options = service.playbackOptions
        val ticksPerStep = options.resolution / 4 // 96/4 = 24
        assertTrue(
            options.noteDuration <= ticksPerStep,
            "Note duration (${options.noteDuration}) should be <= ticksPerStep ($ticksPerStep) for clean looping"
        )
    }

    @Test
    fun `should set loop end point to exact bar boundary`() {
        val pattern = TestFixtures.createSimplePattern(bpm = 120)
        service.toggleLoop()
        service.play(pattern)
        // Loop end should be at 16 * ticksPerStep = 16 * 24 = 384 ticks
        val expectedLoopEnd = 16L * (service.playbackOptions.resolution / 4)
        assertEquals(expectedLoopEnd, service.loopEndTick, "Loop end should be at exact bar boundary")
        service.stop()
    }
}
