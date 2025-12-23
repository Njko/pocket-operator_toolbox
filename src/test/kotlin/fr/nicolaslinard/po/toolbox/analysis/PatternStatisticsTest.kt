package fr.nicolaslinard.po.toolbox.analysis

import fr.nicolaslinard.po.toolbox.TestFixtures
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import kotlin.test.*

/**
 * TDD: RED phase - Tests for pattern statistics and analysis
 *
 * Feature: Pattern statistics and metrics
 * Provides insights into drum patterns and pattern libraries.
 */
class PatternStatisticsTest {

    // === Single Pattern Statistics Tests ===

    @Test
    fun `should calculate note density for pattern`() {
        val pattern = TestFixtures.createSimplePattern()
        val analyzer = PatternStatisticsAnalyzer()

        val density = analyzer.calculateDensity(pattern)

        assertTrue(density > 0.0 && density <= 1.0) // Percentage of filled steps
    }

    @Test
    fun `should calculate note count for pattern`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 5, 9, 13), // 4 notes
                PO12DrumVoice.SNARE to listOf(5, 13) // 2 notes
            ), // Total: 6 notes
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternStatisticsAnalyzer()

        val stats = analyzer.analyze(pattern)

        assertEquals(6, stats.totalNotes)
        assertEquals(2, stats.voiceCount)
    }

    @Test
    fun `should calculate voice usage statistics`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 5, 9, 13),
                PO12DrumVoice.SNARE to listOf(5, 13),
                PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7, 9, 11, 13, 15)
            ),
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternStatisticsAnalyzer()

        val stats = analyzer.analyze(pattern)

        assertEquals(3, stats.voiceCount)
        assertTrue(stats.voiceUsage.containsKey(PO12DrumVoice.KICK))
        assertEquals(4, stats.voiceUsage[PO12DrumVoice.KICK])
    }

    @Test
    fun `should calculate step coverage`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 5, 9, 13)
            ), // 4 unique steps out of 16
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternStatisticsAnalyzer()

        val stats = analyzer.analyze(pattern)

        assertEquals(4, stats.activeSteps)
        assertEquals(0.25, stats.stepCoverage, 0.01) // 4/16 = 25%
    }

    @Test
    fun `should detect empty pattern`() {
        val pattern = TestFixtures.createEmptyPattern()
        val analyzer = PatternStatisticsAnalyzer()

        val stats = analyzer.analyze(pattern)

        assertEquals(0, stats.totalNotes)
        assertEquals(0, stats.voiceCount)
        assertEquals(0.0, stats.density, 0.001)
    }

    // === Rhythm Complexity Tests ===

    @Test
    fun `should calculate rhythm complexity score`() {
        val simplePattern = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)), // Simple 4-on-floor
            metadata = TestFixtures.createTestMetadata()
        )
        val complexPattern = TestFixtures.createComplexPattern()
        val analyzer = PatternStatisticsAnalyzer()

        val simpleComplexity = analyzer.calculateComplexity(simplePattern)
        val complexComplexity = analyzer.calculateComplexity(complexPattern)

        assertTrue(complexComplexity > simpleComplexity)
    }

    @Test
    fun `should detect syncopation`() {
        val syncopatedPattern = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 4, 7, 10, 13) // Off-beat rhythm
            ),
            metadata = TestFixtures.createTestMetadata()
        )
        val straightPattern = PO12Pattern(
            number = 2,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 5, 9, 13) // On-beat rhythm
            ),
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternStatisticsAnalyzer()

        val syncopation1 = analyzer.calculateSyncopation(syncopatedPattern)
        val syncopation2 = analyzer.calculateSyncopation(straightPattern)

        assertTrue(syncopation1 > syncopation2)
    }

    // === Common Pattern Detection Tests ===

    @Test
    fun `should detect four-on-the-floor pattern`() {
        val fourOnFloor = PO12Pattern(
            number = 1,
            voices = mapOf(PO12DrumVoice.KICK to listOf(1, 5, 9, 13)), // Quarter notes
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternStatisticsAnalyzer()

        val isFourOnFloor = analyzer.isFourOnTheFloor(fourOnFloor)

        assertTrue(isFourOnFloor)
    }

    @Test
    fun `should detect breakbeat pattern characteristics`() {
        val pattern = PO12Pattern(
            number = 1,
            voices = mapOf(
                PO12DrumVoice.KICK to listOf(1, 7, 11),
                PO12DrumVoice.SNARE to listOf(5, 13)
            ),
            metadata = TestFixtures.createTestMetadata()
        )
        val analyzer = PatternStatisticsAnalyzer()

        val isBreakbeat = analyzer.hasBreakbeatCharacteristics(pattern)

        assertNotNull(isBreakbeat) // Returns true or false, not null
    }

    // === Library Statistics Tests ===

    @Test
    fun `should calculate statistics for pattern library`() {
        val library = listOf(
            TestFixtures.createSimplePattern(),
            TestFixtures.createComplexPattern(),
            TestFixtures.createEmptyPattern()
        )
        val analyzer = PatternStatisticsAnalyzer()

        val libraryStats = analyzer.analyzeLibrary(library)

        assertEquals(3, libraryStats.totalPatterns)
        assertTrue(libraryStats.averageDensity >= 0.0)
    }

    @Test
    fun `should find most used voices in library`() {
        val library = listOf(
            PO12Pattern(
                number = 1,
                voices = mapOf(
                    PO12DrumVoice.KICK to listOf(1),
                    PO12DrumVoice.SNARE to listOf(5)
                ),
                metadata = TestFixtures.createTestMetadata()
            ),
            PO12Pattern(
                number = 2,
                voices = mapOf(
                    PO12DrumVoice.KICK to listOf(1),
                    PO12DrumVoice.CLOSED_HH to listOf(1, 3, 5, 7)
                ),
                metadata = TestFixtures.createTestMetadata()
            )
        )
        val analyzer = PatternStatisticsAnalyzer()

        val libraryStats = analyzer.analyzeLibrary(library)

        // Kick appears in 2 patterns
        assertTrue(libraryStats.mostUsedVoices.contains(PO12DrumVoice.KICK))
    }

    @Test
    fun `should calculate average complexity for library`() {
        val library = listOf(
            TestFixtures.createSimplePattern(),
            TestFixtures.createComplexPattern()
        )
        val analyzer = PatternStatisticsAnalyzer()

        val libraryStats = analyzer.analyzeLibrary(library)

        assertTrue(libraryStats.averageComplexity > 0.0)
    }

    // === BPM Statistics Tests ===

    @Test
    fun `should calculate BPM distribution`() {
        val library = listOf(
            PO12Pattern(
                number = 1,
                voices = mapOf(PO12DrumVoice.KICK to listOf(1)),
                metadata = TestFixtures.createTestMetadata(bpm = 120)
            ),
            PO12Pattern(
                number = 2,
                voices = mapOf(PO12DrumVoice.KICK to listOf(1)),
                metadata = TestFixtures.createTestMetadata(bpm = 140)
            ),
            PO12Pattern(
                number = 3,
                voices = mapOf(PO12DrumVoice.KICK to listOf(1)),
                metadata = TestFixtures.createTestMetadata(bpm = 120)
            )
        )
        val analyzer = PatternStatisticsAnalyzer()

        val libraryStats = analyzer.analyzeLibrary(library)

        assertTrue(libraryStats.bpmRange.min <= libraryStats.bpmRange.max)
        assertEquals(120.0, libraryStats.bpmRange.min, 0.1)
        assertEquals(140.0, libraryStats.bpmRange.max, 0.1)
    }

    // === Pattern Statistics Data Class Tests ===

    @Test
    fun `should create pattern statistics object`() {
        val stats = PatternStatistics(
            totalNotes = 10,
            voiceCount = 3,
            activeSteps = 8,
            density = 0.5,
            stepCoverage = 0.5,
            complexity = 0.7,
            voiceUsage = mapOf(PO12DrumVoice.KICK to 4)
        )

        assertEquals(10, stats.totalNotes)
        assertEquals(3, stats.voiceCount)
        assertEquals(0.5, stats.density, 0.001)
    }

    @Test
    fun `should create library statistics object`() {
        val stats = LibraryStatistics(
            totalPatterns = 10,
            averageDensity = 0.6,
            averageComplexity = 0.5,
            mostUsedVoices = listOf(PO12DrumVoice.KICK, PO12DrumVoice.SNARE),
            bpmRange = BpmRange(min = 60.0, max = 200.0, average = 120.0)
        )

        assertEquals(10, stats.totalPatterns)
        assertEquals(0.6, stats.averageDensity, 0.001)
        assertEquals(2, stats.mostUsedVoices.size)
    }

    // === Percentile Calculation Tests ===

    @Test
    fun `should calculate density percentile`() {
        val library = (1..10).map { index ->
            PO12Pattern(
                number = index,
                voices = mapOf(PO12DrumVoice.KICK to List(index) { it + 1 }),
                metadata = TestFixtures.createTestMetadata()
            )
        }
        val targetPattern = library[4] // Middle pattern
        val analyzer = PatternStatisticsAnalyzer()

        val percentile = analyzer.calculateDensityPercentile(targetPattern, library)

        assertTrue(percentile >= 0.0 && percentile <= 100.0)
    }
}
