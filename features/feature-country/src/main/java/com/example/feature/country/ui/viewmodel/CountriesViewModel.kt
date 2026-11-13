package com.example.feature.country.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.model.UIState
import com.example.presentation.model.toUiError
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.country.model.domain.CountriesUIState
import com.example.feature.country.model.domain.CountryItem
import com.example.feature.country.model.domain.CountryUi
import com.example.feature.country.model.domain.SearchInput
import com.example.feature.country.model.domain.SearchState
import com.example.feature.country.usecase.GetCountriesUseCase
import com.example.feature.country.usecase.SearchCountriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCountriesUseCase: GetCountriesUseCase,
    private val searchCountriesUseCase: SearchCountriesUseCase
) : ViewModel() {
    companion object {
        private const val TAG = "GetCountriesViewModel"
        private const val ARGUMENT_COUNTRIES = "$TAG.COUNTRIES"

        private const val SEARCH_DEBOUNCE_MILLIS = 300L
    }

    private val _countries: List<CountryItem>
        get() = savedStateHandle.get<List<CountryItem>>(ARGUMENT_COUNTRIES).orEmpty()

    private val _uiState = MutableStateFlow<UIState<CountriesUIState>>(UIState.Loading)
    val uiState = _uiState
        .onStart {
            if (_countries.isEmpty()) {
                getCountries()
            } else {
                viewModelScope.launch {
                    _uiState.update {
                        UIState.Success(
                            data = CountriesUIState(
                                countries = _countries.toPersistentList(),
                                searchState = SearchState()
                            )
                        )
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UIState.Default
        )

    private val _selectedCountry = Channel<CountryUi>(Channel.BUFFERED)
    val selectedCountry: Flow<CountryUi?> = _selectedCountry.receiveAsFlow()

    private val _searchQuery = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            _searchQuery
                .filterNotNull()
                .distinctUntilChanged()
                .debounce(SEARCH_DEBOUNCE_MILLIS.milliseconds)
                .collectLatest { query -> runSearch(query = query) }
        }
    }

    fun getCountries() {
        viewModelScope.launch {
            getCountriesUseCase.invoke(
                input = Unit
            ).collect { state ->
                launch {
                    when (state) {
                        is UseCaseOutputWithStatus.Progress -> _uiState.update {
                            UIState.Loading
                        }
                        is UseCaseOutputWithStatus.Success -> {
                            savedStateHandle[ARGUMENT_COUNTRIES] = state.result
                            _uiState.update {
                                UIState.Success(
                                    data = CountriesUIState(
                                        countries = state.result.toPersistentList(),
                                        searchState = SearchState()
                                    )
                                )
                            }
                        }
                        is UseCaseOutputWithStatus.Failed -> _uiState.update {
                            state.error.toUiError()
                        }
                    }
                }
            }
        }
    }

    fun onSelectedCountry(country: CountryUi) {
        viewModelScope.launch {
            _selectedCountry.send(country)
        }
    }

    fun triggerSearch() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                if (currentState is UIState.Success) {
                    currentState.copy(
                        data = currentState.data.copy(
                            searchState = currentState.data.searchState.copy(
                                isActive = !currentState.data.searchState.isActive
                            )
                        )
                    )
                } else currentState
            }
        }
    }

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                if (currentState is UIState.Success) {
                    currentState.copy(
                        data = currentState.data.copy(
                            searchState = currentState.data.searchState.copy(query = query)
                        )
                    )
                } else currentState
            }
            _searchQuery.update { query }
        }
    }

    fun clearSearchQuery() {
        viewModelScope.launch {
            _searchQuery.value = ""
            _uiState.update { currentState ->
                if (currentState is UIState.Success) {
                    currentState.copy(
                        data = currentState.data.copy(
                            countries = _countries.toPersistentList(),
                            searchState = SearchState()
                        )
                    )
                } else currentState
            }
        }
    }

    private suspend fun runSearch(query: String) {
        searchCountriesUseCase.invoke(
            input = SearchInput(
                query = query,
                countries = _countries,
                currentState = _uiState.value
            )
        ).collect { state ->
            if (state is UseCaseOutputWithStatus.Success) {
                _uiState.update {
                    state.result
                }
            }
        }
    }
}