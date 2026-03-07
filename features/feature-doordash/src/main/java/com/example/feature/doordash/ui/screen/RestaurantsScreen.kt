package com.example.feature.doordash.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ThumbsUpDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.ErrorView
import com.example.core.ui.SmartNetworkImage
import com.example.core.ui.UIStatefulContent
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDataModel
import com.example.feature.doordash.ui.viewmodel.RestaurantListsViewModel

@Composable
fun RestaurantScreen(
    modifier: Modifier = Modifier,
    viewModel: RestaurantListsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UIStatefulContent(
        state = uiState,
        successContent = {
            RestaurantsListView(
                restaurants = it
            )
        },
        errorContent = { message, _ ->
            ErrorView(
                message = message
            ) {
                viewModel.getRestaurants()
            }
        }
    )
}

@Composable
internal fun RestaurantsListView(
    modifier: Modifier = Modifier,
    viewModel: RestaurantListsViewModel = hiltViewModel(),
    restaurants: List<Pair<RestaurantDataModel, LikedStatus>>
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(restaurants) { restaurantInfo ->
            val data = restaurantInfo.first
            val likedStatus = restaurantInfo.second

            RestaurantListItem(
                imageUrl = data.coverImgUrl.orEmpty(),
                restaurantName = data.name.orEmpty(),
                restaurantDescription = data.description,
                restaurantStatus = data.status,
                restaurantDeliveryFee = data.deliveryFee,
                restaurantStatusType = data.statusType,
                likedStatus = likedStatus,
                onClick = {
                    viewModel.onSelectedRestaurant(data.id)
                },
                onLikeClick = {

                }
            )
        }
    }
}

@Composable
fun RestaurantListItem(
    imageUrl: String,
    restaurantName: String,
    restaurantDescription: String? = null,
    restaurantStatus: String? = null,
    restaurantDeliveryFee: String? = null,
    restaurantStatusType: String? = null,
    likedStatus: LikedStatus,
    onClick: () -> Unit = {},
    onLikeClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image on left
            SmartNetworkImage(
                url = imageUrl,
                contentDescription = restaurantName,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = restaurantName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (restaurantDescription.isNullOrBlank().not()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = restaurantDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (restaurantStatus.isNullOrBlank().not()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = restaurantStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (restaurantDeliveryFee.isNullOrBlank().not()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = restaurantDeliveryFee,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (restaurantStatusType.isNullOrBlank().not()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = restaurantStatusType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Optional action icon
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = when(likedStatus) {
                        LikedStatus.LIKED -> Icons.Outlined.ThumbUp
                        LikedStatus.UN_LIKED -> Icons.Outlined.ThumbDown
                        LikedStatus.NO_REF -> Icons.Outlined.ThumbsUpDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }
    }
}