package fr.nicolaslinard.po.toolbox.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PO14PatternTest {

    private fun metadata(name: String = "Test Bass") = PatternMetadata(name = name, deviceModel = "PO-14")

    // --- Pitch ---

    @Test
    fun `Pitch fromLetter resolves natural notes case-insensitively`() {
        assertEquals(Pitch.D, Pitch.fromLetter("d"))
        assertEquals(Pitch.D, Pitch.fromLetter("D"))
    }

    @Test
    fun `Pitch fromLetter returns null for unknown letters`() {
        assertNull(Pitch.fromLetter("H"))
    }

    // --- PO14Step ---

    @Test
    fun `PO14Step computes MIDI note for a natural note at octave 3`() {
        // C4 = MIDI 60 (middle C), so C3 = 48
        val step = PO14Step(Pitch.C, octave = 3)
        assertEquals(48, step.midiNote)
    }

    @Test
    fun `PO14Step half tone up raises the MIDI note by one semitone`() {
        val natural = PO14Step(Pitch.C, octave = 3, halfToneUp = false)
        val sharp = PO14Step(Pitch.C, octave = 3, halfToneUp = true)
        assertEquals(natural.midiNote + 1, sharp.midiNote)
    }

    @Test
    fun `PO14Step rejects octave outside 0 to 8`() {
        assertFailsWith<IllegalArgumentException> { PO14Step(Pitch.C, octave = 9) }
        assertFailsWith<IllegalArgumentException> { PO14Step(Pitch.C, octave = -1) }
    }

    @Test
    fun `PO14Step noteText renders letter, sharp and octave`() {
        assertEquals("C3", PO14Step(Pitch.C, octave = 3).noteText)
        assertEquals("D#2", PO14Step(Pitch.D, octave = 2, halfToneUp = true).noteText)
    }

    // --- PO14Pattern ---

    @Test
    fun `PO14Pattern is an AnyPattern`() {
        val pattern = PO14Pattern(
            steps = mapOf(1 to PO14Step(Pitch.C, 3)),
            sound = PODevice.PO_14.voices.first(),
            metadata = metadata(),
            number = 2
        )
        val any: AnyPattern = pattern
        assertEquals(metadata().name, any.metadata.name)
        assertEquals(2, any.number)
    }

    @Test
    fun `getNote returns the programmed step or null for a rest`() {
        val pattern = PO14Pattern(
            steps = mapOf(1 to PO14Step(Pitch.C, 3)),
            sound = PODevice.PO_14.voices.first(),
            metadata = metadata()
        )
        assertEquals(Pitch.C, pattern.getNote(1)?.pitch)
        assertNull(pattern.getNote(2))
    }

    @Test
    fun `rejects pattern number outside 1 to 16`() {
        assertFailsWith<IllegalArgumentException> {
            PO14Pattern(steps = emptyMap(), sound = PODevice.PO_14.voices.first(), metadata = metadata(), number = 17)
        }
    }

    @Test
    fun `rejects step keys outside 1 to 16`() {
        assertFailsWith<IllegalArgumentException> {
            PO14Pattern(
                steps = mapOf(17 to PO14Step(Pitch.C, 3)),
                sound = PODevice.PO_14.voices.first(),
                metadata = metadata()
            )
        }
    }

    @Test
    fun `defaults to an empty pattern with no notes`() {
        val pattern = PO14Pattern(steps = emptyMap(), sound = PODevice.PO_14.voices.first(), metadata = metadata())
        assertTrue(pattern.steps.isEmpty())
    }
}
