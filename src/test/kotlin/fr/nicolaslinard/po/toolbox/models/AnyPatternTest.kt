package fr.nicolaslinard.po.toolbox.models

import fr.nicolaslinard.po.toolbox.TestFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AnyPatternTest {

    @Test
    fun `PO12Pattern is an AnyPattern exposing metadata and number`() {
        val pattern = TestFixtures.createSimplePattern(name = "Any Test", patternNumber = 4)
        val any: AnyPattern = assertIs<AnyPattern>(pattern)
        assertEquals("Any Test", any.metadata.name)
        assertEquals(4, any.number)
    }
}
