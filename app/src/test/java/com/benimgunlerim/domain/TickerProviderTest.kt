package com.benimgunlerim.domain

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TickerProviderTest {

    @Test
    fun immediateTickerProvider_emitsOneUnit() = runTest {
        val provider = ImmediateTickerProvider()

        val values = provider.minuteTicker().toList()

        assertEquals(listOf(Unit), values)
    }

    @Test
    fun immediateTickerProvider_firstEmission_isUnit() = runTest {
        val provider = ImmediateTickerProvider()

        val first = provider.minuteTicker().first()

        assertEquals(Unit, first)
    }

    @Test
    fun systemTickerProvider_instantiation_doesNotThrow() {
        // Just verify it can be constructed; actual timing is not tested in unit tests
        val provider = SystemTickerProvider()
        assert(provider is TickerProvider)
    }
}
