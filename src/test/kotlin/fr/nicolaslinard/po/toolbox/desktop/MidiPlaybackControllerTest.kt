package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.TestFixtures
import fr.nicolaslinard.po.toolbox.io.MidiPlaybackService
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TDD: Tests for MidiPlaybackController.
 * Uses MockK to isolate from real MIDI and TornadoFX.
 */
class MidiPlaybackControllerTest {

    private lateinit var mockService: MidiPlaybackService
    private lateinit var controller: MidiPlaybackController

    @BeforeEach
    fun setup() {
        mockService = mockk(relaxed = true)
        controller = MidiPlaybackController(mockService)
    }

    @AfterEach
    fun cleanup() {
        controller.dispose()
    }

    @Test
    fun `should delegate play to service`() {
        val pattern = TestFixtures.createSimplePattern()
        controller.play(pattern)
        verify { mockService.play(pattern) }
    }

    @Test
    fun `should delegate stop to service`() {
        controller.stop()
        verify { mockService.stop() }
    }

    @Test
    fun `should delegate toggleLoop to service`() {
        controller.toggleLoop()
        verify { mockService.toggleLoop() }
    }

    @Test
    fun `should delegate dispose to service`() {
        controller.dispose()
        verify { mockService.dispose() }
    }

    @Test
    fun `should expose isPlaying from service`() {
        every { mockService.isPlaying } returns true
        assertTrue(controller.isPlaying)
    }

    @Test
    fun `should expose isLooping from service`() {
        every { mockService.isLooping } returns true
        assertTrue(controller.isLooping)
    }

    @Test
    fun `should expose currentStep from service`() {
        every { mockService.currentStep } returns 5
        kotlin.test.assertEquals(5, controller.currentStep)
    }
}
