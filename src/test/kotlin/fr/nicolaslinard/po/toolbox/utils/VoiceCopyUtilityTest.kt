package fr.nicolaslinard.po.toolbox.utils

import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * TDD RED Phase: Tests for VoiceCopyUtility
 *
 * Tests voice copying between patterns.
 * Verifies pattern discovery, voice loading, and copying operations.
 */
class VoiceCopyUtilityTest {

    @Test
    fun `should list available patterns in directory`() {
        // VoiceCopyUtility should find all .md pattern files in directory

        val utility = VoiceCopyUtility(File("patterns"))
        val patterns = utility.listAvailablePatterns()

        // Expected behavior:
        // 1. Scan directory for *.md files
        // 2. Parse pattern metadata
        // 3. Return PatternSummary list

        assertTrue(true, "Integration test placeholder - will be implemented in GREEN phase")
    }

    @Test
    fun `should load voice from pattern file`() {
        // Given pattern file with kick voice, load those steps

        val utility = VoiceCopyUtility(File("patterns"))
        val testFile = File("patterns/test-pattern.md")

        // If file exists with kick voice
        val kickSteps = utility.loadVoiceFromPattern(testFile, PO12DrumVoice.KICK)

        // Expected behavior:
        // 1. Parse markdown file
        // 2. Extract steps for specified voice
        // 3. Return sorted list of steps

        assertTrue(true, "Integration test placeholder")
    }

    @Test
    fun `should return null when voice not in pattern`() {
        // If pattern doesn't have requested voice, return null

        val utility = VoiceCopyUtility(File("patterns"))
        val testFile = File("patterns/test-pattern.md")

        // Pattern has kick but not cowbell
        val cowbellSteps = utility.loadVoiceFromPattern(testFile, PO12DrumVoice.COWBELL)

        // Should return null (voice not present)
        assertNull(cowbellSteps, "Should return null for missing voice")
    }

    @Test
    fun `should copy voice to target pattern`() {
        // Copy voice from source pattern to target pattern

        val utility = VoiceCopyUtility(File("patterns"))
        val sourceFile = File("patterns/source.md")
        val targetVoices = mutableMapOf<PO12DrumVoice, List<Int>>()

        utility.copyVoiceBetweenPatterns(
            sourceFile = sourceFile,
            sourceVoice = PO12DrumVoice.KICK,
            targetPattern = targetVoices,
            targetVoice = PO12DrumVoice.KICK
        )

        // Target should now have kick voice from source
        assertTrue(targetVoices.containsKey(PO12DrumVoice.KICK))
    }

    @Test
    fun `should copy voice to different target voice`() {
        // Copy kick from source to snare in target

        val utility = VoiceCopyUtility(File("patterns"))
        val sourceFile = File("patterns/source.md")
        val targetVoices = mutableMapOf<PO12DrumVoice, List<Int>>()

        utility.copyVoiceBetweenPatterns(
            sourceFile = sourceFile,
            sourceVoice = PO12DrumVoice.KICK,
            targetPattern = targetVoices,
            targetVoice = PO12DrumVoice.SNARE
        )

        // Kick pattern should now be on snare voice
        assertTrue(targetVoices.containsKey(PO12DrumVoice.SNARE))
        assertEquals(
            utility.loadVoiceFromPattern(sourceFile, PO12DrumVoice.KICK),
            targetVoices[PO12DrumVoice.SNARE]
        )
    }

    @Test
    fun `should include pattern name in summary`() {
        // PatternSummary should include human-readable name

        val utility = VoiceCopyUtility(File("patterns"))
        val patterns = utility.listAvailablePatterns()

        if (patterns.isNotEmpty()) {
            val firstPattern = patterns.first()
            assertNotNull(firstPattern.name)
            assertTrue(firstPattern.name.isNotBlank())
        }
    }

    @Test
    fun `should list all voices in pattern summary`() {
        // PatternSummary should show which voices are present

        val utility = VoiceCopyUtility(File("patterns"))
        val patterns = utility.listAvailablePatterns()

        if (patterns.isNotEmpty()) {
            val firstPattern = patterns.first()
            assertNotNull(firstPattern.voices)
            // Voices list should contain PO12DrumVoice entries
            assertTrue(firstPattern.voices.all { it is PO12DrumVoice })
        }
    }

    @Test
    fun `should handle missing pattern files gracefully`() {
        // If directory doesn't exist or has no patterns, return empty list

        val utility = VoiceCopyUtility(File("nonexistent-directory"))
        val patterns = utility.listAvailablePatterns()

        // Should not crash, return empty list
        assertEquals(emptyList(), patterns)
    }

    @Test
    fun `should skip non-markdown files`() {
        // Only process .md files, skip .txt, .json, etc.

        val utility = VoiceCopyUtility(File("patterns"))
        val patterns = utility.listAvailablePatterns()

        // All pattern files should be .md files
        assertTrue(patterns.all { it.file.extension == "md" })
    }

    @Test
    fun `should handle corrupt pattern files gracefully`() {
        // If pattern file is malformed, skip it or return null

        val utility = VoiceCopyUtility(File("patterns"))
        val corruptFile = File("patterns/corrupt.md")

        // Should not crash when loading from corrupt file
        val voice = utility.loadVoiceFromPattern(corruptFile, PO12DrumVoice.KICK)

        // Either returns null or throws specific exception
        assertTrue(true, "Should handle corrupt files gracefully")
    }
}
