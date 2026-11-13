package com.example.feature.business.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.feature.business.R
import com.example.feature.business.model.domain.BusinessCategory

@Composable
fun BusinessSectionHeader(
    category: BusinessCategory,
    count: Int
) {
    val titleRes = when(category) {
        BusinessCategory.PIZZA -> R.string.section_pizza
        BusinessCategory.BEER -> R.string.section_beer
    }
    Text(
        text = stringResource(titleRes, count),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(BusinessTestTags.sectionHeader(category))
    )
}