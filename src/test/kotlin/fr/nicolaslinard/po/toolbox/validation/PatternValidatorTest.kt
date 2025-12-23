package fr.nicolaslinard.po.toolbox.validation

import fr.nicolaslinard.po.toolbox.TestFixtures
import fr.nicolaslinard.po.toolbox.models.Difficulty
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import fr.nicolaslinard.po.toolbox.models.PatternMetadata
import java.time.LocalDate
import kotlin.test.*

class PatternValidatorTest {

    private lateinit var validator: PatternValidator

    @BeforeTest
    fun setup() {
        validator = PatternValidator()
    }

    // === Valid Pattern Tests ===

    @Test
    fun `should validate simple pattern successfully`() {
        val pattern = TestFixtures.createSimplePattern()

        val result = validator.validate(pattern)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `should validate complex pattern successfully`() {
        val pattern = TestFixtures.createComplexPattern()

        val result = validator.validate(pattern)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    // === Pattern Number Validation ===

    @Test
    fun `should reject pattern number less than 1`() {
        // This should fail in the PO12Pattern constructor itself
        assertFails {
            PO12Pattern(
                voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5)),
                metadata = TestFixtures.createTestMetadata(),
                number = 0
            )
        }
    }

    @Test
    fun `should reject pattern number greater than 16`() {
        assertFails {
            PO12Pattern(
                voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5)),
                metadata = TestFixtures.createTestMetadata(),
                number = 17
            )
        }
    }

    // === Voice and Step Validation ===

    @Test
    fun `should warn about empty voices`() {
        val pattern = TestFixtures.createEmptyPattern()

        val result = validator.validate(pattern)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("no drum voices") })
    }

    @Test
    fun `should warn about single voice pattern`() {
        val pattern = PO12Pattern(
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)),
            metadata = TestFixtures.createTestMetadata(),
            number = 1
        )

        val result = validator.validate(pattern)

        assertTrue(result.isValid) // It's valid, but...
        assertTrue(result.warnings.any { it.contains("only uses 1 voice") })
    }

    @Test
    fun `should warn about pattern without kick or snare`() {
        val pattern = PO12Pattern(
            voices = mapOf(
                PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15),
                PO12DrumVoice.COWBELL to listOf(5, 13)
            ),
            metadata = TestFixtures.createTestMetadata(),
            number = 1
        )

        val result = validator.validate(pattern)

        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("no kick or snare") })
    }

    // === Metadata Validation ===

    @Test
    fun `should reject pattern with blank name`() {
        val pattern = PO12Pattern(
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5)),
            metadata = PatternMetadata(
                name = "",
                dateCreated = LocalDate.now()
            ),
            number = 1
        )

        val result = validator.validate(pattern)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("name cannot be blank") })
    }

    @Test
    fun `should warn about BPM outside typical range`() {
        val pattern = PO12Pattern(
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5)),
            metadata = TestFixtures.createTestMetadata(bpm = 50),
            number = 1
        )

        val result = validator.validate(pattern)

        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("outside typical range") })
    }

    @Test
    fun `should warn about BPM exceeding PO-12 maximum`() {
        val pattern = PO12Pattern(
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5)),
            metadata = TestFixtures.createTestMetadata(bpm = 250),
            number = 1
        )

        val result = validator.validate(pattern)

        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("exceeds PO-12 maximum") })
    }

    // === validateOrThrow Tests ===

    @Test
    fun `validateOrThrow should not throw for valid pattern`() {
        val pattern = TestFixtures.createSimplePattern()

        // Should not throw
        validator.validateOrThrow(pattern)
    }

    @Test
    fun `validateOrThrow should throw for invalid pattern`() {
        val pattern = TestFixtures.createEmptyPattern()

        assertFails {
            validator.validateOrThrow(pattern)
        }
    }

    // === ValidationResult Tests ===

    @Test
    fun `ValidationResult should correctly report errors and warnings`() {
        val result = ValidationResult(
            isValid = false,
            errors = listOf("Error 1", "Error 2"),
            warnings = listOf("Warning 1")
        )

        assertFalse(result.isValid)
        assertTrue(result.hasErrors)
        assertTrue(result.hasWarnings)
        assertEquals(2, result.errors.size)
        assertEquals(1, result.warnings.size)
    }

    @Test
    fun `ValidationResult should indicate valid with no errors`() {
        val result = ValidationResult(
            isValid = true,
            errors = emptyList(),
            warnings = listOf("Warning 1")
        )

        assertTrue(result.isValid)
        assertFalse(result.hasErrors)
        assertTrue(result.hasWarnings)
    }
}
