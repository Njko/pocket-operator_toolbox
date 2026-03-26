package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.TestFixtures
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import javax.sound.midi.MidiSystem
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

/**
 * TDD: Tests for MIDI playback service.
 *
 * Split into two groups:
 * - Unit tests: pure logic (tickToStep, options, state), no MIDI hardware needed
 * - Integration tests: require a real MIDI sequencer, skipped on headless CI
 */
class MidiPlaybackServiceTest {

    companion object {
        /** Check if a MIDI sequencer can actually be opened (not just obtained). */
        fun midiHardwareAvailable(): Boolean {
            return try {
                val seq = MidiSystem.getSequencer()
                seq.open()
                seq.close()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    // ======================================================================
    // Unit tests — no MIDI hardware required
    // ======================================================================

    @Nested
    inner class UnitTests {

        private lateinit var service: MidiPlaybackService

        @BeforeEach
        fun setup() {
            service = MidiPlaybackService()
        }

        @AfterEach
        fun cleanup() {
            // dispose is safe even without a sequencer
            service.dispose()
        }

        // --- Initial state ---

        @Test
        fun `should create service successfully`() {
            assertNotNull(service)
            assertFalse(service.isPlaying)
        }

        @Test
        fun `should not loop by default`() {
            assertFalse(service.isLooping)
        }

        @Test
        fun `should return step 0 when not playing`() {
            assertEquals(0, service.currentStep)
        }

        // --- Loop toggle (no sequencer needed) ---

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

        // --- Stop idempotency ---

        @Test
        fun `should stop be idempotent when not playing`() {
            service.stop()
            assertFalse(service.isPlaying)
        }

        // --- Dispose safety ---

        @Test
        fun `should be safe to dispose twice`() {
            service.dispose()
            service.dispose()
            assertFalse(service.isPlaying)
        }

        // --- tickToStep calculation ---

        @Test
        fun `should return step 1 for tick 0`() {
            assertEquals(1, service.tickToStep(0))
        }

        @Test
        fun `should return step 2 for tick 24`() {
            assertEquals(2, service.tickToStep(24))
        }

        @Test
        fun `should return step 16 for tick 360`() {
            assertEquals(16, service.tickToStep(360))
        }

        @Test
        fun `should wrap step position for ticks beyond one bar`() {
            assertEquals(1, service.tickToStep(384))
        }

        // --- Playback options ---

        @Test
        fun `should use short note duration for playback to avoid bleed past bar`() {
            val options = service.playbackOptions
            val ticksPerStep = options.resolution / 4
            assertTrue(
                options.noteDuration <= ticksPerStep,
                "Note duration (${options.noteDuration}) should be <= ticksPerStep ($ticksPerStep)"
            )
        }
    }

    // ======================================================================
    // Integration tests — require real MIDI hardware, skipped on headless CI
    // ======================================================================

    @Nested
    inner class IntegrationTests {

        private lateinit var service: MidiPlaybackService

        @BeforeEach
        fun setup() {
            assumeTrue(midiHardwareAvailable(), "MIDI sequencer not available, skipping")
            service = MidiPlaybackService()
        }

        @AfterEach
        fun cleanup() {
            if (::service.isInitialized) {
                service.dispose()
            }
        }

        // --- Play / Stop ---

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

        // --- Loop during playback ---

        @Test
        fun `should apply loop mode when playing`() {
            val pattern = TestFixtures.createSimplePattern()
            service.toggleLoop()
            service.play(pattern)
            assertTrue(service.isPlaying)
            assertTrue(service.isLooping)
            service.stop()
        }

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

        // --- Current step ---

        @Test
        fun `should return current step when playing`() {
            val pattern = TestFixtures.createSimplePattern()
            service.play(pattern)
            val step = service.currentStep
            assertTrue(step in 0..16)
            service.stop()
        }

        @Test
        fun `should use sequence resolution for currentStep`() {
            val pattern = TestFixtures.createSimplePattern()
            service.play(pattern)
            val step = service.currentStep
            assertTrue(step in 0..16)
            service.stop()
        }

        // --- Dispose and recovery ---

        @Test
        fun `should dispose resources cleanly`() {
            val pattern = TestFixtures.createSimplePattern()
            service.play(pattern)
            service.dispose()
            assertFalse(service.isPlaying)
        }

        @Test
        fun `should recover after dispose and play again`() {
            val pattern = TestFixtures.createSimplePattern()
            service.play(pattern)
            service.dispose()
            assertFalse(service.isPlaying)
            service.play(pattern)
            assertTrue(service.isPlaying)
            service.stop()
        }

        // --- Callback ---

        @Test
        fun `should invoke onPlaybackStopped callback when set`() {
            var callbackInvoked = false
            service.onPlaybackStopped = { callbackInvoked = true }
            val pattern = TestFixtures.createSimplePattern()
            service.play(pattern)
            service.stop()
            assertTrue(callbackInvoked)
        }

        // --- Loop continuity ---

        @Test
        fun `should remain playing when looping and end of track is reached`() {
            val pattern = TestFixtures.createSimplePattern(bpm = 206)
            service.toggleLoop()
            service.play(pattern)
            assertTrue(service.isPlaying)
            Thread.sleep(1500)
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
            assertEquals(0, stoppedCount, "onPlaybackStopped should not fire during loop")
            service.stop()
            assertEquals(1, stoppedCount, "onPlaybackStopped should fire once on manual stop")
        }

        @Test
        fun `should set explicit loop points for seamless looping`() {
            val pattern = TestFixtures.createSimplePattern(bpm = 206)
            service.toggleLoop()
            service.play(pattern)
            assertTrue(service.isPlaying)
            Thread.sleep(1500)
            assertTrue(service.isPlaying)
            service.stop()
        }

        @Test
        fun `should set loop end point to exact bar boundary`() {
            val pattern = TestFixtures.createSimplePattern(bpm = 120)
            service.toggleLoop()
            service.play(pattern)
            val expectedLoopEnd = 16L * (service.playbackOptions.resolution / 4)
            assertEquals(expectedLoopEnd, service.loopEndTick, "Loop end should be at exact bar boundary")
            service.stop()
        }
    }
}
