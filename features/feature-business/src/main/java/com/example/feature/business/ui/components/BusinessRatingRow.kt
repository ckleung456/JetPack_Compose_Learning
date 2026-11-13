package com.example.feature.business.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.feature.business.R

@Composable
internal fun BusinessRatingRow(
    modifier: Modifier = Modifier,
    ratingText: String,
    reviewCount: Int,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(4.dp)
) {
    val ratingDesc = stringResource(R.string.cd_rating, ratingText)
    Row(
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier.semantics { contentDescription = ratingDesc }
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = ratingText,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = pluralStringResource(R.plurals.review_count, reviewCount, reviewCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}