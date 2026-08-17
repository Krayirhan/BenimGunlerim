package com.benimgunlerim.domain

import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TickerProviderTest {

    @Test
    fun immediateTickerProvider_minuteTicker_emitsUnit() = runTest {
        val provider = ImmediateTickerProvider()
        val first = provider.minuteTicker().first()
        assertEquals(Unit, first)
    }

    @Test
    fun immediateTickerProvider_dateTicker_emitsToday() = runTest {
        val provider = ImmediateTickerProvider()
        val mockDateTimeProvider = object : DateTimeProvider {
            override fun today(): LocalDate = LocalDate.of(2025, 1, 15)
            override fun currentTimeMillis(): Long = 1000L
            override fun currentTime(): java.time.LocalTime = java.time.LocalTime.NOON
        }
        val date = provider.dateTicker(mockDateTimeProvider).first()
        assertEquals(LocalDate.of(2025, 1, 15), date)
    }

    @Test
    fun systemTickerProvider_instantiation_doesNotThrow() {
        val provider = SystemTickerProvider()
        assertNotNull(provider)
    }
}
