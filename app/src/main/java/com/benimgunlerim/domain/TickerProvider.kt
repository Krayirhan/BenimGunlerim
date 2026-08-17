package com.benimgunlerim.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Abstracts ticking flows so components that poll on a schedule
 * (e.g. re-evaluating canCloseDay every minute, advancing date at midnight)
 * can be tested without real time delays.
 *
 * Production code injects [SystemTickerProvider]; tests can inject
 * [ImmediateTickerProvider] or any custom fake.
 */
interface TickerProvider {
    /** Emits [Unit] immediately and then once every 60 seconds. */
    fun minuteTicker(): Flow<Unit>

    /** Emits current date and advances when day rolls over. */
    fun dateTicker(dateTimeProvider: DateTimeProvider): Flow<LocalDate>
}

/** Production implementation: fires at real-time intervals. */
class SystemTickerProvider @Inject constructor() : TickerProvider {
    override fun minuteTicker(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(60_000L)
        }
    }

    override fun dateTicker(dateTimeProvider: DateTimeProvider): Flow<LocalDate> = flow {
        while (true) {
            val now = dateTimeProvider.today()
            emit(now)
            val midnight = now.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            val nowDateTime = Instant.ofEpochMilli(dateTimeProvider.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
            val delayMs = Duration.between(nowDateTime, midnight).toMillis()
            delay(delayMs.coerceAtLeast(0L) + 500L)
        }
    }
}

/**
 * Test-only implementation: emits state flows that stay active.
 * Use when you just need the ticker to fire without blocking state collection.
 */
class ImmediateTickerProvider : TickerProvider {
    override fun minuteTicker(): Flow<Unit> = MutableStateFlow(Unit)

    override fun dateTicker(dateTimeProvider: DateTimeProvider): Flow<LocalDate> =
        MutableStateFlow(dateTimeProvider.today())
}
