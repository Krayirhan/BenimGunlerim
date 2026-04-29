package com.benimgunlerim.helpers

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeTransactionRunnerTest {

    @Test
    fun runInTransaction_executesBlock() = runTest {
        val runner = FakeTransactionRunner()
        var executed = false

        runner.runInTransaction {
            executed = true
        }

        assertTrue(executed)
    }

    @Test
    fun runInTransaction_returnsBlockValue() = runTest {
        val runner = FakeTransactionRunner()

        val result = runner.runInTransaction { 42 }

        assertEquals(42, result)
    }
}
