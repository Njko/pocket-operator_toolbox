package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.TestFixtures
import fr.nicolaslinard.po.toolbox.models.PO12DrumVoice
import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatternRepositoryTest {

    private lateinit var tempDir: File
    private lateinit var repository: FilePatternRepository

    @BeforeTest
    fun setup() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "po-test-${System.nanoTime()}")
        tempDir.mkdirs()
        repository = FilePatternRepository(tempDir)
    }

    @AfterTest
    fun cleanup() {
        tempDir.deleteRecursively()
    }

    // --- loadAll ---

    @Test
    fun `should return empty list when directory does not exist`() {
        val repo = FilePatternRepository(File(tempDir, "nonexistent"))
        val patterns = repo.loadAll()
        assertTrue(patterns.isEmpty())
    }

    @Test
    fun `should return empty list when directory is empty`() {
        val patterns = repository.loadAll()
        assertTrue(patterns.isEmpty())
    }

    @Test
    fun `should load saved pattern`() {
        val pattern = TestFixtures.createSimplePattern(name = "My Pattern")
        repository.save(pattern)

        val loaded = repository.loadAll()
        assertEquals(1, loaded.size)
        assertEquals("My Pattern", loaded[0].name)
    }

    @Test
    fun `should load multiple patterns sorted by filename`() {
        repository.save(TestFixtures.createSimplePattern(name = "Beta Pattern"))
        repository.save(TestFixtures.createSimplePattern(name = "Alpha Pattern"))

        val loaded = repository.loadAll()
        assertEquals(2, loaded.size)
        assertEquals("Alpha Pattern", loaded[0].name)
        assertEquals("Beta Pattern", loaded[1].name)
    }

    @Test
    fun `should skip non-markdown files`() {
        repository.save(TestFixtures.createSimplePattern())
        File(tempDir, "readme.txt").writeText("not a pattern")

        val loaded = repository.loadAll()
        assertEquals(1, loaded.size)
    }

    @Test
    fun `should skip malformed markdown files`() {
        repository.save(TestFixtures.createSimplePattern())
        File(tempDir, "broken.md").writeText("this is not a valid pattern file")

        val loaded = repository.loadAll()
        assertEquals(1, loaded.size)
    }

    // --- save ---

    @Test
    fun `should create file when saving pattern`() {
        val pattern = TestFixtures.createSimplePattern(name = "Test Save")
        val file = repository.save(pattern)

        assertTrue(file.exists())
        assertTrue(file.name.endsWith(".md"))
    }

    @Test
    fun `should create directory if it does not exist`() {
        val newDir = File(tempDir, "subdir")
        val repo = FilePatternRepository(newDir)
        repo.save(TestFixtures.createSimplePattern())

        assertTrue(newDir.exists())
        assertTrue(newDir.isDirectory)
    }

    @Test
    fun `should preserve pattern content after save and load`() {
        val pattern = TestFixtures.createComplexPattern()
        repository.save(pattern)

        val loaded = repository.loadAll()
        assertEquals(1, loaded.size)
        assertEquals(pattern.metadata.name, loaded[0].pattern.metadata.name)
        assertEquals(pattern.metadata.bpm, loaded[0].pattern.metadata.bpm)
        assertEquals(4, (loaded[0].pattern as PO12Pattern).voices.size)
    }

    // --- update ---

    @Test
    fun `should update pattern in place`() {
        val original = TestFixtures.createSimplePattern(name = "Original")
        val originalFile = repository.save(original)

        val updated = TestFixtures.createSimplePattern(name = "Updated", bpm = 200)
        repository.update(originalFile, updated)

        val loaded = repository.loadAll()
        assertEquals(1, loaded.size)
        assertEquals("Updated", loaded[0].name)
    }

    @Test
    fun `should delete original file after update`() {
        val original = TestFixtures.createSimplePattern(name = "Original")
        val originalFile = repository.save(original)

        val updated = TestFixtures.createSimplePattern(name = "Updated")
        repository.update(originalFile, updated)

        assertFalse(originalFile.exists())
    }

    @Test
    fun `should not leave backup file after successful update`() {
        val original = TestFixtures.createSimplePattern(name = "Original")
        val originalFile = repository.save(original)

        val updated = TestFixtures.createSimplePattern(name = "Updated")
        repository.update(originalFile, updated)

        val backupFile = File(originalFile.parent, originalFile.name + ".bak")
        assertFalse(backupFile.exists())
    }

    // --- delete ---

    @Test
    fun `should delete pattern file`() {
        val pattern = TestFixtures.createSimplePattern()
        val file = repository.save(pattern)
        assertTrue(file.exists())

        repository.delete(file)
        assertFalse(file.exists())
    }

    @Test
    fun `should have empty list after deleting all patterns`() {
        val file = repository.save(TestFixtures.createSimplePattern())
        repository.delete(file)

        val loaded = repository.loadAll()
        assertTrue(loaded.isEmpty())
    }

    // --- PatternSummary ---

    @Test
    fun `should expose pattern summary properties`() {
        val pattern = TestFixtures.createSimplePattern(name = "Summary Test", bpm = 150, patternNumber = 3)
        repository.save(pattern)

        val summary = repository.loadAll().first()
        assertEquals("Summary Test", summary.name)
        assertEquals("150", summary.bpm)
        assertEquals("#3", summary.patternNumber)
        assertEquals("2", summary.voiceCount)
        assertEquals("beginner", summary.difficulty)
        assertEquals("Test", summary.genre)
    }

    @Test
    fun `should show dash for missing BPM`() {
        val pattern = TestFixtures.createEmptyPattern()
        repository.save(pattern)

        val summary = repository.loadAll().first()
        assertEquals("-", summary.bpm)
    }

    // --- PO-14 patterns ---

    @Test
    fun `should save and load a PO14 pattern alongside PO12 patterns`() {
        val po12 = TestFixtures.createSimplePattern(name = "Drum Pattern")
        val po14 = fr.nicolaslinard.po.toolbox.models.PO14Pattern(
            steps = mapOf(1 to fr.nicolaslinard.po.toolbox.models.PO14Step(fr.nicolaslinard.po.toolbox.models.Pitch.C, 2)),
            sound = fr.nicolaslinard.po.toolbox.models.PODevice.PO_14.voices.first(),
            metadata = fr.nicolaslinard.po.toolbox.models.PatternMetadata(name = "Bass Pattern", deviceModel = "PO-14")
        )

        repository.save(po12)
        repository.save(po14)

        val loaded = repository.loadAll()
        assertEquals(2, loaded.size)
        assertTrue(loaded.any { it.pattern is PO12Pattern })
        assertTrue(loaded.any { it.pattern is fr.nicolaslinard.po.toolbox.models.PO14Pattern })
    }

    @Test
    fun `PO14 pattern summary reports its programmed note count as voiceCount`() {
        val po14 = fr.nicolaslinard.po.toolbox.models.PO14Pattern(
            steps = mapOf(
                1 to fr.nicolaslinard.po.toolbox.models.PO14Step(fr.nicolaslinard.po.toolbox.models.Pitch.C, 2),
                9 to fr.nicolaslinard.po.toolbox.models.PO14Step(fr.nicolaslinard.po.toolbox.models.Pitch.G, 2)
            ),
            sound = fr.nicolaslinard.po.toolbox.models.PODevice.PO_14.voices.first(),
            metadata = fr.nicolaslinard.po.toolbox.models.PatternMetadata(name = "Bass Count Test", deviceModel = "PO-14")
        )
        repository.save(po14)

        val summary = repository.loadAll().first()
        assertEquals("2", summary.voiceCount)
    }
}
