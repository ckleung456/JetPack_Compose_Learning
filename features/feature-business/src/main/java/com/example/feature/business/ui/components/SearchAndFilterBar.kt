package com.example.feature.business.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.feature.business.R
import com.example.feature.business.model.domain.BusinessFilter

@Composable
fun SearchAndFilterBar(
    modifier: Modifier = Modifier,
    query: String,
    selectedFilter: BusinessFilter,
    onQueryChanged: (String) -> Unit,
    onFilterSelected: (BusinessFilter) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(""),
            placeholder = { Text(stringResource(R.string.search_by_name)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChanged("") }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.search_clear)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BusinessFilter.entries.forEach { filter ->
                FilterChip(
                    selected = filter == selectedFilter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(stringResource(filter.labelRes())) },
                    modifier = Modifier.testTag("")
                )
            }
        }
    }
}

private fun BusinessFilter.labelRes(): Int = when(this) {
    BusinessFilter.ALL -> R.string.filter_all
    BusinessFilter.PIZZA -> R.string.filter_pizza
    BusinessFilter.BEER -> R.string.filter_beer
}