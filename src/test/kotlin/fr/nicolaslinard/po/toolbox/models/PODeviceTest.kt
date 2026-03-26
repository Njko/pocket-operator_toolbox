package fr.nicolaslinard.po.toolbox.models

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class PODeviceTest {

    @Test
    fun `should list all supported devices`() {
        val devices = PODevice.entries
        assertTrue(devices.size >= 3) // At least PO-12, PO-14, PO-16
    }

    @Test
    fun `should have PO-12 Rhythm`() {
        val po12 = PODevice.PO_12
        assertEquals("PO-12", po12.modelId)
        assertEquals("Rhythm", po12.deviceName)
        assertEquals(DeviceType.DRUM_MACHINE, po12.type)
        assertEquals(16, po12.voiceCount)
        assertEquals(16, po12.stepCount)
        assertEquals(60..206, po12.bpmRange)
    }

    @Test
    fun `should have PO-14 Sub`() {
        val po14 = PODevice.PO_14
        assertEquals("PO-14", po14.modelId)
        assertEquals("Sub", po14.deviceName)
        assertEquals(DeviceType.BASS_SYNTH, po14.type)
        assertEquals(16, po14.voiceCount)
    }

    @Test
    fun `should have PO-16 Factory`() {
        val po16 = PODevice.PO_16
        assertEquals("PO-16", po16.modelId)
        assertEquals("Factory", po16.deviceName)
        assertEquals(DeviceType.LEAD_SYNTH, po16.type)
    }

    @Test
    fun `should provide voices for each device`() {
        PODevice.entries.forEach { device ->
            val voices = device.voices
            assertEquals(device.voiceCount, voices.size, "Voice count mismatch for ${device.modelId}")
            voices.forEach { voice ->
                assertTrue(voice.number in 1..16)
                assertTrue(voice.displayName.isNotBlank())
                assertTrue(voice.shortName.isNotBlank())
            }
        }
    }

    @Test
    fun `should provide MIDI note mapping for each device`() {
        PODevice.entries.forEach { device ->
            device.voices.forEach { voice ->
                val note = device.getMidiNote(voice)
                assertTrue(note in 0..127, "Invalid MIDI note $note for ${voice.displayName} on ${device.modelId}")
            }
        }
    }

    @Test
    fun `should provide documentation URL for each device`() {
        PODevice.entries.forEach { device ->
            assertNotNull(device.guideUrl)
            assertTrue(device.guideUrl.contains("teenage.engineering"))
        }
    }

    @Test
    fun `PO-12 voices should match existing PO12DrumVoice enum`() {
        val po12Voices = PODevice.PO_12.voices
        assertEquals(PO12DrumVoice.entries.size, po12Voices.size)
        PO12DrumVoice.entries.forEach { drumVoice ->
            val found = po12Voices.find { it.number == drumVoice.poNumber }
            assertNotNull(found, "Missing voice for ${drumVoice.displayName}")
            assertEquals(drumVoice.displayName, found.displayName)
        }
    }
}
