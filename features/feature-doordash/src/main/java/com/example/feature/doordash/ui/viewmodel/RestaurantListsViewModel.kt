package com.example.feature.doordash.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.model.UIState
import com.example.presentation.model.toUiError
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDataModel
import com.example.feature.doordash.model.domain.usecase.GetNearByRestaurantInput
import com.example.feature.doordash.model.domain.usecase.LikeRestaurantUseCaseInput
import com.example.feature.doordash.usecase.GetNearByRestaurantsUseCase
import com.example.feature.doordash.usecase.LikeRestaurantUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    private var hasLoaded = false

    private val _uiState = MutableStateFlow<UIState<List<Pair<RestaurantDataModel, LikedStatus>>>>(UIState.Loading)
    val uiState = _uiState
        .onStart {
            if (!hasLoaded) {
                hasLoaded = true
                getRestaurants()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UIState.Loading
        )

    private val _selectedRestaurant = Channel<Long>(Channel.BUFFERED)
    val selectedRestaurant: Flow<Long> = _selectedRestaurant.receiveAsFlow()


    // TODO: should call this using map geo point
    fun getRestaurants(
        lat: Float = DOOR_DASH_LAT,
        lng: Float = DOOR_DASH_LNG
    ) {
        viewModelScope.launch {
            getRestaurantListUseCase(
                input = GetNearByRestaurantInput(
                    lat = lat,
                    lng = lng
                )
            ).collect { state ->
                when (state) {
                    is UseCaseOutputWithStatus.Progress -> _uiState.update { UIState.Loading }
                    is UseCaseOutputWithStatus.Success -> _uiState.update {
                        UIState.Success(data = state.result)
                    }
                    is UseCaseOutputWithStatus.Failed -> _uiState.update {
                        state.error.toUiError()
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
        likeRestaurantUseCase.invoke(
            input = LikeRestaurantUseCaseInput(
                restaurantId = restaurantId,
                status = likedStatus
            )
        ).collect { state ->
            if (state is UseCaseOutputWithStatus.Success && uiState.value is UIState.Success) {
                val currentData = (uiState.value as UIState.Success<List<Pair<RestaurantDataModel, LikedStatus>>>)
                _uiState.update {
                    UIState.Success(
                        data = currentData.data.map {
                            if (it.first.id == restaurantId) {
                                it.copy(second = state.result)
                            } else {
                                it
                            }
                        }
                    )
                }
            }
        }
    }
}