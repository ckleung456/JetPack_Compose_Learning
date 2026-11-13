package com.example.feature.doordash.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.AppTopBar
import com.example.presentation.ErrorView
import com.example.presentation.FullScreenCenteredContent
import com.example.presentation.UIStatefulContent
import com.example.feature.doordash.R
import com.example.feature.doordash.ui.viewmodel.RestaurantDetailViewModel
import kotlin.text.orEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    viewModel: RestaurantDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val title = stringResource(R.string.title_restaurant_detail)

    Scaffold(
        topBar = {
            AppTopBar(
                title = title,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Box(modifier = modifier.fillMaxSize().padding(innerPadding)) {
            UIStatefulContent(
                state = uiState,
                successContent = { restaurantInfo ->
                    val restaurantDetail = restaurantInfo.first
                    val likedStatus = restaurantInfo.second
                    FullScreenCenteredContent(
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
    }
}
