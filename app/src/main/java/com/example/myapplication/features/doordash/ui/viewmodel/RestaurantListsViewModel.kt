package com.example.myapplication.features.doordash.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.model.UIState
import com.example.core.usecase.UseCaseOutputWithStatus
import com.example.myapplication.features.doordash.model.data.LikedStatus
import com.example.myapplication.features.doordash.model.data.RestaurantDataModel
import com.example.myapplication.features.doordash.model.usecases.GetNearByRestaurantInput
import com.example.myapplication.features.doordash.model.usecases.LikeRestaurantUseCaseInput
import com.example.myapplication.features.doordash.usecases.GetNearByRestaurantsUseCase
import com.example.myapplication.features.doordash.usecases.LikeRestaurantUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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

    private val _uiState = Channel<UIState<List<Pair<RestaurantDataModel, LikedStatus>>>>(Channel.BUFFERED)
    val uiState = _uiState.receiveAsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = UIState.Default
    )

    private val _selectedRestaurant = Channel<Long>(Channel.BUFFERED)
    val selectedRestaurant: StateFlow<Long> = _selectedRestaurant.receiveAsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Long.MIN_VALUE
    )


    init {
        getRestaurants()
    }

    // TODO: should call this using map geo point
    fun getRestaurants(
        lat: Float = DOOR_DASH_LAT,
        lng: Float = DOOR_DASH_LNG
    ) {
        viewModelScope.launch {
           getRestaurantListUseCase.invoke(
               input = GetNearByRestaurantInput(
                   lat = lat,
                   lng = lng
               )
           ) { state ->
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
        ) { state ->
            // no-op
        }
    }
}