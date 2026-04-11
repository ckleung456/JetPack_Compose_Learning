package com.example.feature.country.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.model.UIState
import com.example.core.usecase.UseCaseOutputWithStatus
import com.example.feature.country.model.domain.Country
import com.example.feature.country.model.domain.CountryItem
import com.example.feature.country.usecase.GetCountriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCountriesUseCase: GetCountriesUseCase
) : ViewModel() {
    companion object {
        private const val TAG = "GetCountriesViewModel"
        private const val ARGUMENT_COUNTRIES = "$TAG.COUNTRIES"
    }

    private val _countries = savedStateHandle.get<List<CountryItem>>(ARGUMENT_COUNTRIES)

    private val _uiState = Channel<UIState<List<CountryItem>>>(Channel.BUFFERED)
    val uiState = _uiState
        .receiveAsFlow()
        .onStart {
            if (_countries.isNullOrEmpty()) {
                getCountries()
            } else {
                viewModelScope.launch {
                    _uiState.send(UIState.Success(data = _countries))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UIState.Default
        )

    private val _selectedCountry = Channel<Country>(Channel.BUFFERED)
    val selectedCountry: Flow<Country?> = _selectedCountry.receiveAsFlow()

    fun getCountries() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                getCountriesUseCase.invoke(
                    input = Unit
                ).collect { state ->
                    launch {
                        when (state) {
                            is UseCaseOutputWithStatus.Progress -> _uiState.send(UIState.Loading)
                            is UseCaseOutputWithStatus.Success -> {
                                savedStateHandle[ARGUMENT_COUNTRIES] = state.result
                                _uiState.send(UIState.Success(data = state.result))
                            }
                            is UseCaseOutputWithStatus.Failed -> _uiState.send(
                                UIState.Error(
                                    message = state.error.message.orEmpty(),
                                    kind = state.error.kind
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun onSelectedCountry(country: Country) {
        viewModelScope.launch {
            _selectedCountry.send(country)
        }
    }
}