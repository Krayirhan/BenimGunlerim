package com.benimgunlerim.domain

/**
 * Generic result wrapper for ViewModel-level operations that can succeed,
 * fail due to user input, or fail unexpectedly.
 *
 * Usage:
 * ```
 * fun doSomething(): OperationResult<MyData> = try {
 *     OperationResult.Success(performAction())
 * } catch (e: ValidationException) {
 *     OperationResult.ValidationError(e.message ?: "Invalid input")
 * } catch (e: Exception) {
 *     OperationResult.Failure(e)
 * }
 * ```
 */
sealed class OperationResult<out T> {

    /** The operation completed successfully and produced [data]. */
    data class Success<T>(val data: T) : OperationResult<T>()

    /** The operation was rejected due to invalid or missing user input. */
    data class ValidationError(val reason: String) : OperationResult<Nothing>()

    /** The operation failed unexpectedly. */
    data class Failure(val error: Throwable) : OperationResult<Nothing>()

    val isSuccess: Boolean get() = this is Success<*>
    val isFailure: Boolean get() = this is Failure
    val isValidationError: Boolean get() = this is ValidationError
}
