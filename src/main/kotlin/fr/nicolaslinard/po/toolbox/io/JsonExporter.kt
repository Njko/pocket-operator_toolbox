package fr.nicolaslinard.po.toolbox.io

import fr.nicolaslinard.po.toolbox.models.PO12Pattern
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Exports PO-12 patterns to JSON format for programmatic integration.
 * JSON output enables data exchange with other tools and platforms.
 */
class JsonExporter(
    private val prettyPrint: Boolean = true
) {

    /**
     * Export a single pattern to JSON file.
     */
    fun export(pattern: PO12Pattern, outputFile: File) {
        val json = patternToJson(pattern)
        writeJson(json, outputFile)
    }

    /**
     * Export multiple patterns to JSON array file.
     */
    fun exportMultiple(patterns: List<PO12Pattern>, outputFile: File) {
        val jsonArray = JSONArray()
        patterns.forEach { pattern ->
            jsonArray.put(patternToJson(pattern))
        }
        writeJson(jsonArray, outputFile)
    }

    /**
     * Convert a pattern to JSON object.
     */
    private fun patternToJson(pattern: PO12Pattern): JSONObject {
        val json = JSONObject()

        // Pattern number
        json.put("patternNumber", pattern.number)

        // Metadata
        val metadata = JSONObject()
        metadata.put("name", pattern.metadata.name)
        pattern.metadata.description?.let { metadata.put("description", it) }
        pattern.metadata.bpm?.let { metadata.put("bpm", it) }
        if (pattern.metadata.genre.isNotEmpty()) {
            metadata.put("genre", JSONArray(pattern.metadata.genre))
        }
        pattern.metadata.difficulty?.let { metadata.put("difficulty", it.displayName) }
        pattern.metadata.sourceAttribution?.let { metadata.put("source", it) }
        pattern.metadata.author?.let { metadata.put("author", it) }
        metadata.put("dateCreated", pattern.metadata.dateCreated.toString())

        json.put("metadata", metadata)

        // Voices
        val voicesArray = JSONArray()
        pattern.voices.forEach { (voice, steps) ->
            val voiceObj = JSONObject()
            voiceObj.put("shortName", voice.shortName)
            voiceObj.put("displayName", voice.displayName)
            voiceObj.put("poNumber", voice.poNumber)
            voiceObj.put("steps", JSONArray(steps))
            voicesArray.put(voiceObj)
        }

        json.put("voices", voicesArray)

        return json
    }

    /**
     * Write JSON to file with optional pretty printing.
     */
    private fun writeJson(json: Any, outputFile: File) {
        outputFile.parentFile?.mkdirs()

        val jsonString = when (json) {
            is JSONObject -> if (prettyPrint) json.toString(2) else json.toString()
            is JSONArray -> if (prettyPrint) json.toString(2) else json.toString()
            else -> json.toString()
        }

        outputFile.writeText(jsonString)
    }
}

/**
 * Imports PO-12 patterns from JSON format.
 */
class JsonImporter {

    /**
     * Import a pattern from JSON file.
     */
    fun import(file: File): PO12Pattern {
        val json = JSONObject(file.readText())
        return jsonToPattern(json)
    }

    /**
     * Convert JSON object to pattern.
     */
    private fun jsonToPattern(json: JSONObject): PO12Pattern {
        val number = json.getInt("patternNumber")

        // Parse metadata
        val metaJson = json.getJSONObject("metadata")
        val metadata = fr.nicolaslinard.po.toolbox.models.PatternMetadata(
            name = metaJson.getString("name"),
            description = metaJson.optString("description").takeIf { it.isNotEmpty() },
            bpm = if (metaJson.has("bpm")) metaJson.getInt("bpm") else null,
            genre = if (metaJson.has("genre")) {
                val genreArray = metaJson.getJSONArray("genre")
                List(genreArray.length()) { genreArray.getString(it) }
            } else emptyList(),
            difficulty = metaJson.optString("difficulty").takeIf { it.isNotEmpty() }?.let {
                fr.nicolaslinard.po.toolbox.models.Difficulty.fromString(it)
            },
            sourceAttribution = metaJson.optString("source").takeIf { it.isNotEmpty() },
            author = metaJson.optString("author").takeIf { it.isNotEmpty() },
            dateCreated = java.time.LocalDate.parse(metaJson.getString("dateCreated"))
        )

        // Parse voices
        val voicesJson = json.getJSONArray("voices")
        val voices = mutableMapOf<fr.nicolaslinard.po.toolbox.models.PO12DrumVoice, List<Int>>()

        for (i in 0 until voicesJson.length()) {
            val voiceJson = voicesJson.getJSONObject(i)
            val shortName = voiceJson.getString("shortName")
            val voice = fr.nicolaslinard.po.toolbox.models.PO12DrumVoice.fromShortName(shortName)

            if (voice != null) {
                val stepsArray = voiceJson.getJSONArray("steps")
                val steps = List(stepsArray.length()) { stepsArray.getInt(it) }
                voices[voice] = steps
            }
        }

        return PO12Pattern(
            number = number,
            voices = voices,
            metadata = metadata
        )
    }
}
