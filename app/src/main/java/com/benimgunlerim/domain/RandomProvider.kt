package com.benimgunlerim.domain

/**
 * Abstracts random element selection so that functions relying on randomness
 * (e.g. [GameEngine.companionMessage]) can be tested deterministically.
 *
 * Production code receives [SystemRandomProvider]; tests use [FixedIndexRandomProvider].
 */
interface RandomProvider {
    /** Pick one element from [list]. Throws [NoSuchElementException] if [list] is empty. */
    fun <T> pickFrom(list: List<T>): T
}

/** Production implementation that delegates to [List.random]. */
class SystemRandomProvider : RandomProvider {
    override fun <T> pickFrom(list: List<T>): T = list.random()
}

/**
 * Test-only implementation that always returns the element at [index] (mod list size).
 *
 * Example:
 * ```
 * val random = FixedIndexRandomProvider(0) // always returns list[0]
 * ```
 */
class FixedIndexRandomProvider(private val index: Int = 0) : RandomProvider {
    override fun <T> pickFrom(list: List<T>): T = list[index % list.size]
}
