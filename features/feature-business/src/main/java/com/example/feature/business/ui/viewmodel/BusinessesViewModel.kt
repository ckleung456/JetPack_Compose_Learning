package com.example.feature.business.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.Error
import com.example.domain.location.LocationError
import com.example.presentation.model.UIState
import com.example.presentation.model.toUiError
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.R
import com.example.feature.business.model.domain.AddressDialogUi
import com.example.feature.business.model.domain.BuildBusinessSectionsInput
import com.example.feature.business.model.domain.BusinessListAction
import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.BusinessListEvent
import com.example.feature.business.model.domain.BusinessListEvent.*
import com.example.feature.business.model.domain.BusinessListState
import com.example.feature.business.model.domain.BusinessSectionUi
import com.example.feature.business.model.domain.NearyByBusinessesUi
import com.example.feature.business.usecase.BuildBusinessSectionsUseCase
import com.example.feature.business.usecase.GetCurrentAddressUseCase
import com.example.feature.business.usecase.SearchPizzaAndBeerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class BusinessesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchPizzaAndBeerUseCase: SearchPizzaAndBeerUseCase,
    private val buildBusinessSectionsUseCase: BuildBusinessSectionsUseCase,
    private val getCurrentAddressUseCase: GetCurrentAddressUseCase
) : ViewModel() {
    companion object {
        private const val TAG = "BusinessesViewModel"
        private const val SEARCH_DEBOUNCE_MILLIS = 300L
        private const val ARGUMENT_ADDRESS = "$TAG.address"
        private const val ARGUMENT_QUERY = "$TAG.query"
    }

    private val _uiState = MutableStateFlow<BusinessListState>(
        BusinessListState(
            address = savedStateHandle[ARGUMENT_ADDRESS] ?: BusinessListState.DEFAULT_ADDRESS,
            searchQuery = savedStateHandle[ARGUMENT_QUERY] ?: ""
        )
    )
    val uiState = _uiState
        .onStart {
            observeSections()
            loadBusinesses(isRefreshing = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = BusinessListState(
                address = savedStateHandle[ARGUMENT_ADDRESS] ?: BusinessListState.DEFAULT_ADDRESS
            )
        )

    private val _events = MutableSharedFlow<BusinessListEvent>()
    val events = _events.asSharedFlow()

    private val nearByBusinesses = MutableStateFlow<NearyByBusinessesUi?>(null)
    private var loadJob: Job? = null
    private var locateJob: Job? = null

    fun onAction(action: BusinessListAction) {
        Log.w("BusinessesViewModel", "onAction: $action")
        when(action) {
            is BusinessListAction.OnBusinessSelected -> sendEvent(
                NavigateToDetail(id = action.id)
            )
            is BusinessListAction.ShowSnackbar -> sendEvent(
                ShowSnackbar(
                    message = action.message,
                    messageId = action.messageId
                )
            )
            is BusinessListAction.OnFilterSelect -> _uiState.update {
                it.copy(filter = action.filter)
            }
            is BusinessListAction.OnRefresh -> loadBusinesses(isRefreshing = true)
            is BusinessListAction.OnRetry -> loadBusinesses(isRefreshing = false)
            is BusinessListAction.OnSearchQueryUpdate -> updateSearchQuery(query = action.query)
            is BusinessListAction.OnLocationPermissionResult -> onLocationPermissionResult(granted = action.granted)
            BusinessListAction.OnAddressClick -> showDialog(dialogUi = AddressDialogUi.ChooseSource)
            BusinessListAction.OnAddressConfirm -> onAddressConfirm()
            is BusinessListAction.OnAddressTextChange -> onAddressChange(address = action.address)
            BusinessListAction.OnChooseEnterAddress -> showDialog(dialogUi = AddressDialogUi.EnterAddress(address = _uiState.value.address))
            BusinessListAction.OnChooseUseLocation -> {
                showDialog(dialogUi = AddressDialogUi.Hidden)
                sendEvent(event = BusinessListEvent.RequestLocationPermission)
            }
            BusinessListAction.OnDialogDismiss -> showDialog(dialogUi = AddressDialogUi.Hidden)
        }
    }

    fun loadBusinesses(isRefreshing: Boolean) {
        val address = _uiState.value.address
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            searchPizzaAndBeerUseCase(input = address).collect { output ->
                when(output) {
                    is UseCaseOutputWithStatus.Progress -> onLoadStarted(isRefreshing = isRefreshing)
                    is UseCaseOutputWithStatus.Success -> onLoadSucceeded(result = output.result)
                    is UseCaseOutputWithStatus.Failed -> onLoadFailed(error = output.error, isRefreshing)
                }
            }
        }
    }

    private fun onLocationPermissionResult(granted: Boolean) {
        if (!granted) {
            sendEvent(BusinessListEvent.ShowLocationPermissionDenied)
            return
        }
        locateJob?.cancel()
        locateJob = viewModelScope.launch {
            getCurrentAddressUseCase(input = Unit).collect { output ->
                when (output) {
                    is UseCaseOutputWithStatus.Progress -> _uiState.update {
                        it.copy(isLocatingAddress = true)
                    }
                    is UseCaseOutputWithStatus.Success -> updateAddress(address = output.result)
                    is UseCaseOutputWithStatus.Failed -> onLocateFailed(error = output.error)
                }
            }
        }
    }

    private fun showDialog(dialogUi: AddressDialogUi) {
        _uiState.update { it.copy(addressDialogUi = dialogUi) }
    }

    private fun onAddressChange(address: String) {
        if (_uiState.value.addressDialogUi is AddressDialogUi.EnterAddress) {
            showDialog(dialogUi = AddressDialogUi.EnterAddress(address = address))
        }
    }

    private fun onAddressConfirm() {
        val address = (_uiState.value.addressDialogUi as? AddressDialogUi.EnterAddress)?.address?.trim()
        if (address.isNullOrBlank()) return
        showDialog(AddressDialogUi.Hidden)
        updateAddress(address = address)
    }

    private fun updateSearchQuery(query: String) {
        savedStateHandle[ARGUMENT_QUERY] = query
        _uiState.update {
            it.copy(searchQuery = query)
        }
    }

    private fun onLoadStarted(isRefreshing: Boolean) {
        if (isRefreshing && _uiState.value.content is UIState.Success) {
            _uiState.update { it.copy(isRefreshing = true) }
        } else {
            nearByBusinesses.update { null }
            _uiState.update {
                it.copy(content = UIState.Loading, isRefreshing = false)
            }
        }
    }

    private fun onLoadSucceeded(result: NearyByBusinessesUi) {
        _uiState.update { it.copy(isRefreshing = false) }
        nearByBusinesses.value = result
        result.failedCategories.forEach { category ->
            val message = when(category) {
                BusinessCategory.PIZZA -> R.string.partial_failure_pizza
                BusinessCategory.BEER -> R.string.partial_failure_beer
            }
            sendEvent(event = BusinessListEvent.ShowSnackbar(messageId = message))
        }
    }

    private fun onLoadFailed(error: Error, isRefreshing: Boolean) {
        val uiError = error.toUiError()
        if (isRefreshing && _uiState.value.content is UIState.Success) {
            _uiState.update { it.copy(isRefreshing = false) }
            sendEvent(BusinessListEvent.ShowSnackbar(message = uiError.message.ifBlank { null }))
        } else {
            nearByBusinesses.update { null }
            _uiState.update { it.copy(isRefreshing = false, content = uiError) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSections() {
        combine(
            nearByBusinesses.filterNotNull(),
            _uiState.map { it.filter }.distinctUntilChanged(),
            debounceQuery()
        ) { businesses, filter, searchQuery ->
            BuildBusinessSectionsInput(
                businesses = businesses,
                filter = filter,
                searchQuery = searchQuery
            )
        }
            .flatMapLatest { input -> buildBusinessSectionsUseCase(input = input) }
            .onEach(::onSectionsOutput)
            .launchIn(viewModelScope)
    }

    private fun updateAddress(address: String) {
        savedStateHandle[ARGUMENT_ADDRESS] = address
        _uiState.update {
            it.copy(
                isLocatingAddress = false,
                address = address,
            )
        }
        loadBusinesses(isRefreshing = false)
    }

    private fun onLocateFailed(error: Error) {
        val locationError = error as? LocationError ?: LocationError.UNAVAILABLE
        when (locationError) {
            LocationError.PERMISSION_DENIED -> sendEvent(BusinessListEvent.ShowLocationPermissionDenied)
            else -> sendEvent(BusinessListEvent.ShowSnackbar(message = locationError.toString()))
        }
    }

    private fun debounceQuery(): Flow<String> {
        val query = _uiState.map { it.searchQuery }.distinctUntilChanged()
        return merge(
            query.take(1),
            query.drop(1).debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS}
        ).distinctUntilChanged()
    }

    private fun onSectionsOutput(output: UseCaseOutputWithStatus<ImmutableList<BusinessSectionUi>>) {
        when(output) {
            is UseCaseOutputWithStatus.Progress -> Unit
            is UseCaseOutputWithStatus.Success -> _uiState.update {
                it.copy(content = UIState.Success(data = output.result))
            }
            is UseCaseOutputWithStatus.Failed -> _uiState.update {
                it.copy(content = output.error.toUiError())
            }
        }
    }

    private fun sendEvent(event: BusinessListEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}