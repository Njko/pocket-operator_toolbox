package fr.nicolaslinard.po.toolbox.desktop

import fr.nicolaslinard.po.toolbox.io.MarkdownParser
import fr.nicolaslinard.po.toolbox.io.MarkdownWriter
import fr.nicolaslinard.po.toolbox.models.AnyPattern
import java.io.File

interface PatternRepository {
    fun loadAll(): List<PatternSummary>
    fun save(pattern: AnyPattern): File
    fun update(originalFile: File, updatedPattern: AnyPattern): File
    fun delete(file: File)
}

class FilePatternRepository(
    private val baseDir: File = File("patterns"),
    private val parser: MarkdownParser = MarkdownParser(),
    private val writer: MarkdownWriter = MarkdownWriter()
) : PatternRepository {

    override fun loadAll(): List<PatternSummary> {
        if (!baseDir.exists()) return emptyList()

        return baseDir.listFiles { f -> f.extension == "md" }
            ?.sortedBy { it.name }
            ?.mapNotNull { file ->
                try {
                    PatternSummary(file, parser.parse(file))
                } catch (_: Exception) {
                    null
                }
            }
            ?: emptyList()
    }

    override fun save(pattern: AnyPattern): File {
        baseDir.mkdirs()
        return writer.write(pattern, baseDir)
    }

    override fun update(originalFile: File, updatedPattern: AnyPattern): File {
        val backup = File(originalFile.parent, originalFile.name + ".bak")
        try {
            originalFile.copyTo(backup, overwrite = true)
            originalFile.delete()
            val newFile = writer.write(updatedPattern, baseDir)
            backup.delete()
            return newFile
        } catch (e: Exception) {
            if (backup.exists() && !originalFile.exists()) {
                backup.renameTo(originalFile)
            }
            backup.delete()
            throw e
        }
    }

    override fun delete(file: File) {
        file.delete()
    }
}
