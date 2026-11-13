package com.example.feature.business.usecase

import com.example.domain.Result
import com.example.domain.location.LocationError
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.FakeLocationAddressProvider
import com.example.feature.business.MainDispatcherRule
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GetCurrentAddressUseCaseTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val provider = FakeLocationAddressProvider()
    private val useCase = GetCurrentAddressUseCase(provider)

    @Test
    fun `emits the trimmed address`() = runTest {
        provider.result = Result.Success("  500 Market St  ")

        assertEquals(
            listOf(UseCaseOutputWithStatus.Progress(), UseCaseOutputWithStatus.Success("500 Market St")),
            useCase(Unit).toList()
        )
    }

    // Regression: the provider error used to be rethrown as a plain Exception, so callers only got
    // UnexpectedError("LOCATION_DISABLED") and could never react to a specific LocationError.
    @Test
    fun `every location error surfaces as itself`() = runTest {
        LocationError.entries.forEach { locationError ->
            provider.result = Result.Error(locationError)

            val failed = useCase(Unit).toList().last() as UseCaseOutputWithStatus.Failed

            assertEquals(locationError, failed.error)
        }
    }
}
