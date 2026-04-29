package com.benimgunlerim.di

import javax.inject.Qualifier

/**
 * Hilt qualifier for [kotlinx.coroutines.Dispatchers.IO].
 *
 * Inject as `@IoDispatcher private val ioDispatcher: CoroutineDispatcher`
 * to avoid hard-coding `Dispatchers.IO` inside classes, making them
 * testable with a [kotlinx.coroutines.test.TestCoroutineDispatcher].
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
