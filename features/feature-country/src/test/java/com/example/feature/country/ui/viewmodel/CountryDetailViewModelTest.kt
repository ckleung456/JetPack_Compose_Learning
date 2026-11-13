package com.example.feature.country.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.feature.country.country
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CountryDetailViewModelTest {

    @Test
    fun `exposes the given country and stores it in saved state`() {
        val france = country(name = "France", code = "FR").toUi()
        val savedStateHandle = SavedStateHandle()

        val viewModel = CountryDetailViewModel(savedStateHandle, france)

        assertEquals(france, viewModel.detail.value)
        assertEquals(france, savedStateHandle["CountryDetailViewModel.ARGUMENT_COUNTRY"])
    }

    @Test
    fun `null country exposes no detail`() {
        assertNull(CountryDetailViewModel(SavedStateHandle(), null).detail.value)
    }
}
