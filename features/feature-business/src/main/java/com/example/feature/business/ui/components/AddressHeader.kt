package com.example.feature.business.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.feature.business.R

@Composable
fun AddressHeader(
    modifier: Modifier = Modifier,
    isLocating: Boolean = false,
    address: String,
    onClick: () -> Unit
) {
    val changeAddressLabel = stringResource(R.string.change_address)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isLocating,
                onClickLabel = changeAddressLabel,
                role = Role.Button,
                onClick = onClick
            )
            .testTag(BusinessTestTags.ADDRESS_HEADER)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Image locator",
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.searching_near),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isLocating) stringResource(R.string.finding_location) else address,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isLocating) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = changeAddressLabel,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}