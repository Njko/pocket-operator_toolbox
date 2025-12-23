package fr.nicolaslinard.po.toolbox.analysis

import fr.nicolaslinard.po.toolbox.TestFixtures
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import kotlin.test.*

/**
 * TDD: RED phase - Tests for pattern similarity analysis
 *
 * Feature: Pattern similarity search and comparison
 * Helps users find similar patterns and discover related drum beats.
 */
class PatternSimilarityTest {

    // === Similarity Score Calculation Tests ===

    @Test
    fun `should calculate similarity between identical patterns`() {
        val pattern1 = TestFixtures.createSimplePattern()
        val pattern2 = TestFixtures.createSimplePattern()
        val analyzer = PatternSimilarityAnalyzer()

        val similarity = analyzer.calculateSimilarity(pattern1, pattern2)

        assertEquals(1.0, similarity, 0.001) // 100% similar
    }

    @Test
    fun `should calculate similarity between completely different patterns`() {
        val pattern1 = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)),
            metadata = TestFixtures.createTestMetadata()
        )
        val pattern2 = PO12Pattern(
            number = 2,
            voices = mapOf(PO12DrumVoice.SNARE to listOf(2, 6, 10, 14)),
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternSimilarityAnalyzer()

        val similarity = analyzer.calculateSimilarity(pattern1, pattern2)

        assertTrue(similarity < 0.5) // Less than 50% similar
    }

    @Test
    fun `should calculate similarity for patterns with overlapping voices`() {
        val pattern1 = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
                PO12DrumVoice.SNARE to listOf(5, 13)
            ),
            metadata = TestFixtures.createTestMetadata()
        )
        val pattern2 = PO12Pattern(
            number = 2,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 9), // 50% overlap
                PO12DrumVoice.SNARE to listOf(5, 13) // 100% overlap
            ),
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternSimilarityAnalyzer()

        val similarity = analyzer.calculateSimilarity(pattern1, pattern2)

        assertTrue(similarity > 0.5 && similarity < 1.0)
    }

    @Test
    fun `should handle empty patterns gracefully`() {
        val pattern1 = TestFixtures.createEmptyPattern()
        val pattern2 = TestFixtures.createSimplePattern()
        val analyzer = PatternSimilarityAnalyzer()

        val similarity = analyzer.calculateSimilarity(pattern1, pattern2)

        assertEquals(0.0, similarity, 0.001) // 0% similar
    }

    // === Voice Similarity Tests ===

    @Test
    fun `should calculate voice overlap similarity`() {
        val pattern1 = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1),
                PO12DrumVoice.SNARE to listOf(5),
                PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7)
            ),
            metadata = TestFixtures.createTestMetadata()
        )
        val pattern2 = PO12Pattern(
            number = 2,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1),
                PO12DrumVoice.SNARE to listOf(5)
                // Missing hi-hat
            ),
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternSimilarityAnalyzer()

        val voiceSimilarity = analyzer.calculateVoiceSimilarity(pattern1, pattern2)

        // 2 out of 3 voices match
        assertTrue(voiceSimilarity > 0.6 && voiceSimilarity < 0.7)
    }

    @Test
    fun `should calculate step placement similarity`() {
        val pattern1 = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)),
            metadata = TestFixtures.createTestMetadata()
        )
        val pattern2 = PO12Pattern(
            number = 2,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 11, 13)), // 3/4 match
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternSimilarityAnalyzer()

        val stepSimilarity = analyzer.calculateStepSimilarity(pattern1, pattern2)

        assertEquals(0.75, stepSimilarity, 0.01) // 75% similar steps
    }

    // === Rhythm Pattern Analysis Tests ===

    @Test
    fun `should detect identical rhythm patterns`() {
        val pattern1 = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)),
            metadata = TestFixtures.createTestMetadata()
        )
        val pattern2 = PO12Pattern(
            number = 2,
            voices = mapOf(PO12DrumVoice.SNARE to listOf(1, 5, 9, 13)), // Same rhythm, different voice
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternSimilarityAnalyzer()

        val rhythmSimilarity = analyzer.calculateRhythmSimilarity(pattern1, pattern2)

        assertEquals(1.0, rhythmSimilarity, 0.001) // Identical rhythm
    }

    @Test
    fun `should calculate density similarity`() {
        val pattern1 = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 5, 9, 13), // 4 notes
                PO12DrumVoice.SNARE to listOf(5, 13) // 2 notes
            ), // Total: 6 notes
            metadata = TestFixtures.createTestMetadata()
        )
        val pattern2 = PO12Pattern(
            number = 2,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 9), // 2 notes
                PO12DrumVoice.SNARE to listOf(5, 13), // 2 notes
                PO12DrumVoice.CLOSED_HH to listOf(1, 5) // 2 notes
            ), // Total: 6 notes
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternSimilarityAnalyzer()

        val densitySimilarity = analyzer.calculateDensitySimilarity(pattern1, pattern2)

        assertEquals(1.0, densitySimilarity, 0.001) // Same note count
    }

    // === Pattern Search Tests ===

    @Test
    fun `should find similar patterns in a collection`() {
        val targetPattern = TestFixtures.createSimplePattern()
        val library = listOf(
            TestFixtures.createSimplePattern(),
            TestFixtures.createComplexPattern(),
            TestFixtures.createEmptyPattern()
        )
        val analyzer = PatternSimilarityAnalyzer()

        val results = analyzer.findSimilar(targetPattern, library, threshold = 0.5)

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.similarity >= 0.5 })
    }

    @Test
    fun `should rank similar patterns by similarity score`() {
        val targetPattern = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)),
            metadata = TestFixtures.createTestMetadata()
        )
        val library = listOf(
            PO12Pattern( // Very similar
                number = 2,
                voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)),
                metadata = TestFixtures.createTestMetadata()
            ),
            PO12Pattern( // Somewhat similar
                number = 3,
                voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5)),
                metadata = TestFixtures.createTestMetadata()
            ),
            PO12Pattern( // Not similar
                number = 4,
                voices = mapOf(PO12DrumVoice.SNARE to listOf(2, 6, 10, 14)),
                metadata = TestFixtures.createTestMetadata()
            )
        )
        val analyzer = PatternSimilarityAnalyzer()

        val results = analyzer.findSimilar(targetPattern, library, threshold = 0.3)

        // Results should be sorted by similarity (descending)
        assertTrue(results.size >= 2)
        assertTrue(results[0].similarity >= results[1].similarity)
    }

    @Test
    fun `should filter patterns below similarity threshold`() {
        val targetPattern = TestFixtures.createSimplePattern()
        val library = listOf(
            TestFixtures.createSimplePattern(),
            TestFixtures.createComplexPattern(),
            TestFixtures.createEmptyPattern()
        )
        val analyzer = PatternSimilarityAnalyzer()

        val results = analyzer.findSimilar(targetPattern, library, threshold = 0.95)

        // Only very similar patterns should be returned
        assertTrue(results.all { it.similarity >= 0.95 })
    }

    // === Similarity Result Tests ===

    @Test
    fun `should create similarity result with pattern and score`() {
        val pattern = TestFixtures.createSimplePattern()
        val result = SimilarityResult(pattern, 0.85)

        assertEquals(pattern, result.pattern)
        assertEquals(0.85, result.similarity, 0.001)
    }

    @Test
    fun `should sort similarity results by score`() {
        val results = listOf(
            SimilarityResult(TestFixtures.createSimplePattern(), 0.6),
            SimilarityResult(TestFixtures.createComplexPattern(), 0.9),
            SimilarityResult(TestFixtures.createEmptyPattern(), 0.3)
        )

        val sorted = results.sortedByDescending { it.similarity }

        assertEquals(0.9, sorted[0].similarity, 0.001)
        assertEquals(0.6, sorted[1].similarity, 0.001)
        assertEquals(0.3, sorted[2].similarity, 0.001)
    }

    // === Weighted Similarity Tests ===

    @Test
    fun `should support weighted similarity calculation`() {
        val pattern1 = TestFixtures.createSimplePattern()
        val pattern2 = TestFixtures.createComplexPattern()
        val analyzer = PatternSimilarityAnalyzer()

        val weights = SimilarityWeights(
            voiceWeight = 0.5,
            stepWeight = 0.3,
            rhythmWeight = 0.2
        )

        val similarity = analyzer.calculateSimilarity(pattern1, pattern2, weights)

        assertTrue(similarity >= 0.0 && similarity <= 1.0)
    }

    @Test
    fun `should validate similarity weights sum to 1_0`() {
        val weights = SimilarityWeights(
            voiceWeight = 0.5,
            stepWeight = 0.3,
            rhythmWeight = 0.2
        )

        assertTrue(weights.isValid())
        assertEquals(1.0, weights.voiceWeight + weights.stepWeight + weights.rhythmWeight, 0.001)
    }

    // === Jaccard Similarity Tests ===

    @Test
    fun `should calculate Jaccard similarity for step sets`() {
        val set1 = setOf(1, 5, 9, 13)
        val set2 = setOf(1, 5, 11, 13) // 3/5 union, 3 intersection
        val analyzer = PatternSimilarityAnalyzer()

        val jaccard = analyzer.jaccardSimilarity(set1, set2)

        assertEquals(0.6, jaccard, 0.01) // 3/(3+2) = 0.6
    }

    @Test
    fun `should handle empty sets in Jaccard similarity`() {
        val set1 = emptySet<Int>()
        val set2 = setOf(1, 5, 9)
        val analyzer = PatternSimilarityAnalyzer()

        val jaccard = analyzer.jaccardSimilarity(set1, set2)

        assertEquals(0.0, jaccard, 0.001)
    }

    // === Performance Tests ===

    @Test
    fun `should efficiently search large pattern libraries`() {
        val targetPattern = TestFixtures.createSimplePattern()
        val largeLibrary = (1..100).map {
            TestFixtures.createSimplePattern()
        }
        val analyzer = PatternSimilarityAnalyzer()

        val startTime = System.currentTimeMillis()
        val results = analyzer.findSimilar(targetPattern, largeLibrary, threshold = 0.5)
        val duration = System.currentTimeMillis() - startTime

        assertTrue(duration < 1000) // Should complete in less than 1 second
        assertNotNull(results)
    }
}
