package com.example.myapplication.features.doordash.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.model.UIState
import com.example.core.usecase.UseCaseOutputWithStatus
import com.example.myapplication.features.doordash.model.data.LikedStatus
import com.example.myapplication.features.doordash.model.data.RestaurantDetailDataModel
import com.example.myapplication.features.doordash.model.usecases.GetRestaurantDetailInput
import com.example.myapplication.features.doordash.usecases.GetRestaurantDetailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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

    val detail: StateFlow<RestaurantDetailDataModel?> = savedStateHandle.getMutableStateFlow(
        ARGUMENT_RESTAURANT,
        null
    )

    private val _uiState = Channel<UIState<Pair<RestaurantDetailDataModel, LikedStatus>>>()
    val uiState = _uiState.receiveAsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = UIState.Default
    )

    init {
        fetchRestaurantDetail(
            restaurantId = restaurantId
        )
    }

    fun fetchRestaurantDetail(restaurantId: Long) {
        viewModelScope.launch {
            getRestaurantDetailUseCase.invoke(
                input = GetRestaurantDetailInput(
                    restaurantId = restaurantId
                )
            ) { state ->
                launch {
                    when(state) {
                        is UseCaseOutputWithStatus.Progress -> _uiState.send(UIState.Loading)
                        is UseCaseOutputWithStatus.Success -> _uiState.send(
                            UIState.Success(data = state.result)
                        )
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