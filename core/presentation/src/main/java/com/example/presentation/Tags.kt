package com.example.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TagListView(
    modifier: Modifier = Modifier,
    tags: List<String>,
    onItemClick: (String) -> Unit = {},
    selectedTags: Set<String> = emptySet(),
    selectable: Boolean = false,
    horizontalSpace: Dp = 8.dp,
    verticalSpace: Dp = 4.dp,
    labelStyle: TextStyle = MaterialTheme.typography.labelMedium,
    isClickable: Boolean = true
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(horizontalSpace),
        verticalArrangement = Arrangement.spacedBy(verticalSpace)
    ) {
        tags.forEach { tag ->
            if (selectable) {
                FilterChip(
                    selected = tag in selectedTags,
                    enabled = isClickable,
                    onClick = { onItemClick(tag) },
                    label = {
                        Text(text = tag, style = labelStyle)
                    }
                )
            } else {
                AssistChip(
                    onClick = { onItemClick(tag) },
                    enabled = isClickable,
                    label = {
                        Text(text = tag, style = labelStyle)
                    }
                )
            }
        }
    }
}