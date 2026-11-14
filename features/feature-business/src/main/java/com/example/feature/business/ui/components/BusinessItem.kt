package com.example.feature.business.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.feature.business.model.domain.BusinessLikeStatus
import com.example.presentation.SmartNetworkImage
import com.example.feature.business.model.domain.BusinessUi

@Composable
fun BusinessItem(
    modifier: Modifier = Modifier,
    business: BusinessUi,
    onClick: (BusinessUi) -> Unit,
    onLikeClick: (BusinessUi) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(BusinessTestTags.businessItem(business.id))
            .clickable { onClick(business) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmartNetworkImage(
            url = business.imageUrl,
            contentDescription = business.name,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = business.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            BusinessRatingRow(ratingText = business.ratingText, reviewCount = business.reviewCount)
            val meta = listOfNotNull(
                business.priceText,
                business.distanceText
            ).joinToString(" , ")
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (business.address.isNotEmpty()) {
                Text(
                    text = business.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        LikeToggleButton(
            modifier = Modifier.testTag(BusinessTestTags.likeButton(business.id)),
            isLiked = business.likeStatus == BusinessLikeStatus.LIKED,
            onToggle = { onLikeClick(business) }
        )
    }
}