package com.example.feature.doordash.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.ErrorView
import com.example.core.ui.FullScreenCenteredContent
import com.example.core.ui.UIStatefulContent
import com.example.feature.doordash.ui.viewmodel.RestaurantDetailViewModel
import kotlin.text.orEmpty

@Composable
fun RestaurantDetailScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    viewModel: RestaurantDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UIStatefulContent(
        state = uiState,
        successContent = { restaurantInfo ->
            val restaurantDetail = restaurantInfo.first
            val likedStatus = restaurantInfo.second
            FullScreenCenteredContent(
                modifier = modifier,
                imageUrl = restaurantDetail.coverImgUrl.orEmpty(),
                title = restaurantDetail.name.orEmpty(),
                descriptions = listOf(
                    "Phone number : ${restaurantDetail.phoneNumber}",
                    "Delivery fee : ${restaurantDetail.deliveryFee}",
                    "Average rating : ${restaurantDetail.averageRating}",
                    "Status type : ${restaurantDetail.statusType}",
                    "Status : ${restaurantDetail.status}",
                    "Yelp rating : ${restaurantDetail.yelpRating}",
                    "Liked status : $likedStatus"
                )
            )
        },
        errorContent = { message, _ ->
            ErrorView(
                message = message
            ) {
                viewModel.fetchRestaurantDetail()
            }
        }
    )
}