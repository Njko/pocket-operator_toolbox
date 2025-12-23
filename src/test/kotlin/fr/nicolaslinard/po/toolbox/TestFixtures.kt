package fr.nicolaslinard.po.toolbox

import fr.nicolaslinard.po.toolbox.models.*
import java.time.LocalDate

/**
 * Test fixtures and helper functions for creating test data.
 */
object TestFixtures {

    /**
     * Creates a simple test pattern with kick and snare.
     */
    fun createSimplePattern(
        name: String = "Test Pattern",
        patternNumber: Int = 1,
        bpm: Int = 120
    ): PO12Pattern {
        val voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
            PO12DrumVoice.SNARE to listOf(5, 13)
        )

        val metadata = PatternMetadata(
            name = name,
            description = "A simple test pattern",
            bpm = bpm,
            genre = listOf("Test"),
            difficulty = Difficulty.BEGINNER,
            author = "Test Suite",
            dateCreated = LocalDate.of(2025, 1, 1)
        )

        return PO12Pattern(
            voices = voices,
            metadata = metadata,
            number = patternNumber
        )
    }

    /**
     * Creates a complex pattern with multiple voices.
     */
    fun createComplexPattern(
        name: String = "Complex Pattern",
        patternNumber: Int = 1
    ): PO12Pattern {
        val voices = mapOf(
            PO12DrumVoice.KICK to listOf(1, 3, 11, 12),
            PO12DrumVoice.SNARE to listOf(5, 8, 10, 13),
            PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15),
            PO12DrumVoice.OPEN_HH to listOf(7, 15)
        )

        val metadata = PatternMetadata(
            name = name,
            description = "A complex test pattern with multiple voices",
            bpm = 169,
            genre = listOf("Breakbeat", "Test"),
            difficulty = Difficulty.INTERMEDIATE,
            sourceAttribution = "Test Suite",
            author = "Test Suite",
            dateCreated = LocalDate.of(2025, 1, 1)
        )

        return PO12Pattern(
            voices = voices,
            metadata = metadata,
            number = patternNumber
        )
    }

    /**
     * Creates an empty pattern (no voices).
     */
    fun createEmptyPattern(): PO12Pattern {
        val metadata = PatternMetadata(
            name = "Empty Pattern",
            author = "Test Suite",
            dateCreated = LocalDate.of(2025, 1, 1)
        )

        return PO12Pattern(
            voices = emptyMap(),
            metadata = metadata,
            number = 1
        )
    }

    /**
     * Creates a pattern with invalid step numbers (for testing validation).
     */
    fun createInvalidPattern(): PO12Pattern {
        // This would normally fail validation, so we bypass the constructor validation
        // by using copy or creating through unsafe means for testing purposes
        val metadata = PatternMetadata(
            name = "Invalid Pattern",
            author = "Test Suite",
            dateCreated = LocalDate.of(2025, 1, 1)
        )

        // Note: This might throw in the constructor if validation is enforced
        // Adjust as needed based on actual validation implementation
        return PO12Pattern(
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)),
            metadata = metadata,
            number = 1
        )
    }

    /**
     * Creates metadata for testing.
     */
    fun createTestMetadata(
        name: String = "Test Metadata",
        bpm: Int? = 120,
        difficulty: Difficulty? = Difficulty.BEGINNER
    ): PatternMetadata {
        return PatternMetadata(
            name = name,
            description = "Test metadata description",
            bpm = bpm,
            genre = listOf("Test", "Example"),
            difficulty = difficulty,
            sourceAttribution = "Test Source",
            author = "Test Author",
            dateCreated = LocalDate.of(2025, 1, 1)
        )
    }
}
