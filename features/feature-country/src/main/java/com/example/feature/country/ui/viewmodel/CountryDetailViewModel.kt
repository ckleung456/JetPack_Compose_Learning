package com.example.feature.country.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.feature.country.model.domain.Country
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel(assistedFactory = CountryDetailViewModel.Factory::class)
class CountryDetailViewModel @AssistedInject constructor(
    savedStateHandle: SavedStateHandle,
    @Assisted val country: Country?
) : ViewModel()  {
    companion object {
        private const val TAG = "CountryDetailViewModel"
        private const val ARGUMENT_COUNTRY = "$TAG.ARGUMENT_COUNTRY"
    }

    @AssistedFactory
    interface Factory {
        fun create(country: Country?) : CountryDetailViewModel
    }

    val detail: StateFlow<Country?> = savedStateHandle.getMutableStateFlow(
        ARGUMENT_COUNTRY,
        null
    )

    init {
        savedStateHandle[ARGUMENT_COUNTRY] = country
    }
}