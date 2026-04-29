package com.benimgunlerim.helpers

import com.benimgunlerim.data.DatabaseTransactionRunner

/**
 * Shared test-only [DatabaseTransactionRunner] that executes the block
 * directly (no actual transaction) so unit tests don't need an in-memory
 * database.
 *
 * Usage:
 * ```
 * private val transactionRunner = FakeTransactionRunner()
 * ```
 *
 * Note: [DatabaseTransactionRunner] is a regular interface (not `fun interface`),
 * requiring `object :` syntax — this class encapsulates that detail.
 */
class FakeTransactionRunner : DatabaseTransactionRunner {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
}
