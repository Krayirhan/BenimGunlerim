package com.benimgunlerim.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

/**
 * Hilt qualifier for [kotlinx.coroutines.Dispatchers.Main].
 *
 * Inject this when a coroutine context must run on the Android main thread
 * (e.g. UI state updates that cannot use `withContext(Dispatchers.Main)` via
 * a library-provided scope).
 */
@Qualifier
@Retention(BINARY)
annotation class MainDispatcher
