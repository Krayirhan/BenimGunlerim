package com.benimgunlerim.di

import javax.inject.Qualifier

/**
 * Hilt qualifier for [kotlinx.coroutines.Dispatchers.Default].
 *
 * Inject as `@DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher`
 * to keep CPU-bound work testable without directly referencing the global dispatcher.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
