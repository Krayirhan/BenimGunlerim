package com.benimgunlerim.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BrainDumpParserTest {

    @Test
    fun singleLine_noPrefix() {
        assertEquals(listOf("Su iç"), BrainDumpParser.parse("Su iç"))
    }

    @Test
    fun singleLine_dashPrefix() {
        assertEquals(listOf("Su iç"), BrainDumpParser.parse("- Su iç"))
    }

    @Test
    fun singleLine_bulletPrefix() {
        assertEquals(listOf("Su iç"), BrainDumpParser.parse("• Su iç"))
    }

    @Test
    fun twoLines_withBlankLineBetween() {
        assertEquals(listOf("Su iç", "Kitap oku"), BrainDumpParser.parse("Su iç\n\nKitap oku"))
    }

    @Test
    fun duplicateLines_areDeduplicated() {
        assertEquals(listOf("Su iç"), BrainDumpParser.parse("Su iç\nSu iç"))
    }

    @Test
    fun whitespaceOnly_returnsEmptyList() {
        assertEquals(emptyList<String>(), BrainDumpParser.parse("   "))
    }

    @Test
    fun emptyString_returnsEmptyList() {
        assertEquals(emptyList<String>(), BrainDumpParser.parse(""))
    }

    @Test
    fun mixedBulletsAndPlainLines_allParsed() {
        val raw = "- Su iç\n• Kitap oku\nYürüyüş yap"
        assertEquals(listOf("Su iç", "Kitap oku", "Yürüyüş yap"), BrainDumpParser.parse(raw))
    }

    @Test
    fun leadingTrailingWhitespace_isTrimmed() {
        assertEquals(listOf("Su iç"), BrainDumpParser.parse("   Su iç   "))
    }

    @Test
    fun duplicates_preserveFirstSeenOrder() {
        val raw = "Kitap oku\nSu iç\nKitap oku\nSu iç"
        assertEquals(listOf("Kitap oku", "Su iç"), BrainDumpParser.parse(raw))
    }

    @Test
    fun blankLinesBetweenManyItems_areSkipped() {
        val raw = "Su iç\n\n\nKitap oku\n\nYürüyüş yap\n"
        assertEquals(listOf("Su iç", "Kitap oku", "Yürüyüş yap"), BrainDumpParser.parse(raw))
    }
}
