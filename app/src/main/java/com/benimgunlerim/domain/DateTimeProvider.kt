package com.benimgunlerim.domain

import java.time.LocalDate
import java.time.LocalTime

/**
 * Abstracts current date / time so that any class depending on "now" can be
 * tested deterministically without sleeping or manipulating system clocks.
 *
 * Production code injects [SystemDateTimeProvider]; tests use [FixedDateTimeProvider].
 */
interface DateTimeProvider {
    fun today(): LocalDate
    fun currentTime(): LocalTime
    fun currentTimeMillis(): Long
}

/** Production implementation that delegates to the system clock. */
class SystemDateTimeProvider : DateTimeProvider {
    override fun today(): LocalDate = LocalDate.now()
    override fun currentTime(): LocalTime = LocalTime.now()
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}

/**
 * Test-only implementation with a fixed date / time.
 *
 * Example:
 * ```
 * val clock = FixedDateTimeProvider(
 *     fixedDate  = LocalDate.of(2024, 6, 1),
 *     fixedTime  = LocalTime.of(20, 30),
 *     fixedMillis = 1_717_272_600_000L,
 * )
 * ```
 */
class FixedDateTimeProvider(
    private val fixedDate: LocalDate = LocalDate.of(2024, 1, 1),
    private val fixedTime: LocalTime = LocalTime.of(12, 0),
    private val fixedMillis: Long = 1_704_067_200_000L,
) : DateTimeProvider {
    override fun today(): LocalDate = fixedDate
    override fun currentTime(): LocalTime = fixedTime
    override fun currentTimeMillis(): Long = fixedMillis
}
