package com.example.feature.doordash.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.model.UIState
import com.example.core.usecase.UseCaseOutputWithStatus
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDetailDataModel
import com.example.feature.doordash.model.domain.usecase.GetRestaurantDetailInput
import com.example.feature.doordash.usecase.GetRestaurantDetailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
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

    val detail: StateFlow<Pair<RestaurantDetailDataModel, LikedStatus>?> = savedStateHandle.getMutableStateFlow(
        ARGUMENT_RESTAURANT,
        null
    )

    private val _uiState = Channel<UIState<Pair<RestaurantDetailDataModel, LikedStatus>>>()
    val uiState = _uiState.receiveAsFlow()
        .onStart {
            detail.value?.let {
                _uiState.send(UIState.Success(data = it))
            } ?: run {
                fetchRestaurantDetail()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UIState.Default
        )

    fun fetchRestaurantDetail(restaurantId: Long = this.restaurantId) {
        viewModelScope.launch {
            getRestaurantDetailUseCase.invoke(
                input = GetRestaurantDetailInput(
                    restaurantId = restaurantId
                )
            ) { state ->
                launch {
                    when(state) {
                        is UseCaseOutputWithStatus.Progress -> _uiState.send(UIState.Loading)
                        is UseCaseOutputWithStatus.Success ->{
                            savedStateHandle[ARGUMENT_RESTAURANT] = state.result
                            _uiState.send(
                                UIState.Success(data = state.result)
                            )
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