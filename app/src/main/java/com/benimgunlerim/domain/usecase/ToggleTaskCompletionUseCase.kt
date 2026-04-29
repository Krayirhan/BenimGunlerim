package com.benimgunlerim.domain.usecase

/**
 * Canonical alias that matches the name used in the architecture plan.
 *
 * Both [ToggleTaskUseCase] and [ToggleTaskCompletionUseCase] refer to the
 * same class.  New code should prefer [ToggleTaskCompletionUseCase];
 * existing call sites using [ToggleTaskUseCase] continue to compile without
 * any changes.
 */
typealias ToggleTaskCompletionUseCase = ToggleTaskUseCase
