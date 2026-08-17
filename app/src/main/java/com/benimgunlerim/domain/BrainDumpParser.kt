package com.benimgunlerim.domain

/**
 * Turns Brain Dump free text into a clean list of candidate task titles.
 *
 * Splits on newlines, strips leading `-`/`•` bullet markers and whitespace,
 * drops blank lines, and de-duplicates while preserving first-seen order.
 */
object BrainDumpParser {
    fun parse(raw: String): List<String> =
        raw.lines()
            .map { it.trim().removePrefix("-").removePrefix("•").trim() }
            .filter { it.isNotBlank() }
            .distinct()
}
