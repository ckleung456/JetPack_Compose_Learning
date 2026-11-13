package com.example.feature.doordash.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.model.UIState
import com.example.presentation.model.toUiError
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDetailDataModel
import com.example.feature.doordash.model.domain.usecase.GetRestaurantDetailInput
import com.example.feature.doordash.usecase.GetRestaurantDetailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = RestaurantDetailViewModel.Factory::class)
class RestaurantDetailViewModel @AssistedInject constructor(
    val savedStateHandle: SavedStateHandle,
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    @Assisted val restaurantId: Long
) : ViewModel() {
    companion object {
        private const val TAG = "RestaurantDetailViewModel"
        private const val ARGUMENT_RESTAURANT = "$TAG.ARGUMENT_RESTAURANT"
    }

    @AssistedFactory
    interface Factory {
        fun create(restaurantId: Long): RestaurantDetailViewModel
    }

    val detail: StateFlow<Pair<RestaurantDetailDataModel, LikedStatus>?> = savedStateHandle.getMutableStateFlow(
        ARGUMENT_RESTAURANT,
        null
    )

    private val _uiState = MutableStateFlow<UIState<Pair<RestaurantDetailDataModel, LikedStatus>>>(UIState.Default)
    val uiState = _uiState
        .onStart {
            val cached = detail.value
            if (cached != null) {
                _uiState.value = UIState.Success(data = cached)
            } else {
                fetchRestaurantDetail()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UIState.Default
        )

    private var fetchJob: Job? = null

    fun fetchRestaurantDetail(restaurantId: Long = this.restaurantId) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            getRestaurantDetailUseCase(input = GetRestaurantDetailInput(restaurantId = restaurantId))
                .collect { state ->
                    when(state) {
                        is UseCaseOutputWithStatus.Progress -> _uiState.update { UIState.Loading }
                        is UseCaseOutputWithStatus.Success -> {
                            savedStateHandle[ARGUMENT_RESTAURANT] = state.result
                            _uiState.value = UIState.Success(data = state.result)
                        }
                        is UseCaseOutputWithStatus.Failed -> _uiState.update {
                            state.error.toUiError()
                        }
                    }
                }
        }
    }
}