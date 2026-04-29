package com.benimgunlerim.domain

import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Abstracts ticking flows so components that poll on a schedule
 * (e.g. re-evaluating canCloseDay every minute) can be tested without
 * real time delays.
 *
 * Production code injects [SystemTickerProvider]; tests can inject
 * [ImmediateTickerProvider] or any custom fake.
 */
interface TickerProvider {
    /** Emits [Unit] immediately and then once every 60 seconds. */
    fun minuteTicker(): Flow<Unit>
}

/** Production implementation: fires at real-time 60-second intervals. */
class SystemTickerProvider @Inject constructor() : TickerProvider {
    override fun minuteTicker(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(60_000L)
        }
    }
}

/**
 * Test-only implementation: emits [Unit] once and completes.
 * Use when you just need the ticker to fire once to unblock state collection.
 */
class ImmediateTickerProvider : TickerProvider {
    override fun minuteTicker(): Flow<Unit> = flowOf(Unit)
}
