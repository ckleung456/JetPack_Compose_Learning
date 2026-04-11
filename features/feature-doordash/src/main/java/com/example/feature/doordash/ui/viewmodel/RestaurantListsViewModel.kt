package com.example.feature.doordash.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.model.UIState
import com.example.core.usecase.UseCaseOutputWithStatus
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDataModel
import com.example.feature.doordash.model.domain.usecase.GetNearByRestaurantInput
import com.example.feature.doordash.model.domain.usecase.LikeRestaurantUseCaseInput
import com.example.feature.doordash.usecase.GetNearByRestaurantsUseCase
import com.example.feature.doordash.usecase.LikeRestaurantUseCase
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
class RestaurantListsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getRestaurantListUseCase: GetNearByRestaurantsUseCase,
    private val likeRestaurantUseCase: LikeRestaurantUseCase
) : ViewModel() {
    companion object {
        private const val DOOR_DASH_LAT = 37.422740F
        private const val DOOR_DASH_LNG = -122.139956F
    }

    private val _uiState = Channel<UIState<List<Pair<RestaurantDataModel, LikedStatus>>>>(Channel.BUFFERED)
    val uiState = _uiState
        .receiveAsFlow()
        .onStart {
            getRestaurants()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UIState.Default
        )

    private val _selectedRestaurant = Channel<Long>(Channel.BUFFERED)
    val selectedRestaurant: Flow<Long> = _selectedRestaurant.receiveAsFlow()


    // TODO: should call this using map geo point
    fun getRestaurants(
        lat: Float = DOOR_DASH_LAT,
        lng: Float = DOOR_DASH_LNG
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                getRestaurantListUseCase.invoke(
                    input = GetNearByRestaurantInput(
                        lat = lat,
                        lng = lng
                    )
                ).collect { state ->
                    launch(Dispatchers.Main) {
                        when (state) {
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

    fun onSelectedRestaurant(restaurantId: Long) {
        viewModelScope.launch {
            _selectedRestaurant.send(restaurantId)
        }
    }

    fun likedRestaurant(
        restaurantId: Long,
        likedStatus: LikedStatus
    ) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            likeRestaurantUseCase.invoke(
                input = LikeRestaurantUseCaseInput(
                    restaurantId = restaurantId,
                    status = likedStatus
                )
            ).collect { state ->
                if (state is UseCaseOutputWithStatus.Success && uiState.value is UIState.Success) {
                    val currentData = (uiState.value as UIState.Success<List<Pair<RestaurantDataModel, LikedStatus>>>)
                    viewModelScope.launch {
                        _uiState.send(
                            UIState.Success(
                                data = currentData.data.map {
                                    if (it.first.id == restaurantId) {
                                        it.copy(second = state.result)
                                    } else {
                                        it
                                    }
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}