package com.benimgunlerim.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies that [ToggleTaskCompletionUseCase] resolves to the same class as
 * [ToggleTaskUseCase] — i.e., the typealias is wired correctly.
 */
class ToggleTaskCompletionUseCaseTest {

    @Test
    fun toggleTaskCompletionUseCase_typealiasResolvesToToggleTaskUseCase() {
        // The @JvmName of the class is the canonical class name.
        // Both the alias and the original should refer to the same runtime class.
        assertEquals(
            ToggleTaskUseCase::class,
            ToggleTaskCompletionUseCase::class,
        )
    }
}
