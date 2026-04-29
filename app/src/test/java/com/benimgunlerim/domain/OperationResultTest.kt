package com.benimgunlerim.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OperationResultTest {

    // ── Success ───────────────────────────────────────────────────────────────

    @Test
    fun success_isSuccess_isTrue() {
        val result: OperationResult<Int> = OperationResult.Success(42)
        assertTrue(result.isSuccess)
    }

    @Test
    fun success_isFailure_isFalse() {
        val result: OperationResult<Int> = OperationResult.Success(42)
        assertFalse(result.isFailure)
    }

    @Test
    fun success_isValidationError_isFalse() {
        val result: OperationResult<Int> = OperationResult.Success(42)
        assertFalse(result.isValidationError)
    }

    @Test
    fun success_data_isAccessible() {
        val result = OperationResult.Success("hello")
        assertEquals("hello", result.data)
    }

    // ── ValidationError ───────────────────────────────────────────────────────

    @Test
    fun validationError_isValidationError_isTrue() {
        val result: OperationResult<Nothing> = OperationResult.ValidationError("blank title")
        assertTrue(result.isValidationError)
    }

    @Test
    fun validationError_isSuccess_isFalse() {
        val result: OperationResult<Nothing> = OperationResult.ValidationError("blank title")
        assertFalse(result.isSuccess)
    }

    @Test
    fun validationError_isFailure_isFalse() {
        val result: OperationResult<Nothing> = OperationResult.ValidationError("blank title")
        assertFalse(result.isFailure)
    }

    @Test
    fun validationError_reason_isAccessible() {
        val result = OperationResult.ValidationError("blank title")
        assertEquals("blank title", result.reason)
    }

    // ── Failure ───────────────────────────────────────────────────────────────

    @Test
    fun failure_isFailure_isTrue() {
        val ex = RuntimeException("db error")
        val result: OperationResult<Nothing> = OperationResult.Failure(ex)
        assertTrue(result.isFailure)
    }

    @Test
    fun failure_isSuccess_isFalse() {
        val result: OperationResult<Nothing> = OperationResult.Failure(RuntimeException())
        assertFalse(result.isSuccess)
    }

    @Test
    fun failure_isValidationError_isFalse() {
        val result: OperationResult<Nothing> = OperationResult.Failure(RuntimeException())
        assertFalse(result.isValidationError)
    }

    @Test
    fun failure_error_isAccessible() {
        val ex = IllegalStateException("oops")
        val result = OperationResult.Failure(ex)
        assertEquals(ex, result.error)
    }

    // ── Null data edge case ───────────────────────────────────────────────────

    @Test
    fun success_nullableData_isAllowed() {
        val result = OperationResult.Success<String?>(null)
        assertTrue(result.isSuccess)
        assertNull(result.data)
    }
}
