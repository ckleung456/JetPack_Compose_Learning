package com.example.feature.country.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.FullScreenCenteredContent
import com.example.feature.country.ui.viewmodel.CountryDetailViewModel
import kotlin.text.orEmpty

@Composable
fun CountryDetailScreen(
    viewModel: CountryDetailViewModel
) {
    val country by viewModel.detail.collectAsStateWithLifecycle()
    FullScreenCenteredContent(
        modifier = Modifier.fillMaxSize(),
        imageUrl = country?.flag.orEmpty(),
        title = country?.name.orEmpty(),
        descriptions = listOf(
            country?.code.orEmpty(),
            country?.capital.orEmpty(),
            country?.region.orEmpty(),
            country?.language?.name + " " + country?.language?.code,
            country?.currency?.name + " " + country?.currency?.code + " " + country?.currency?.symbol
        )
    )
}