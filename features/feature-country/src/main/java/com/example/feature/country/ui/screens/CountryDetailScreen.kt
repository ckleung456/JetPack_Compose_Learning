package com.example.feature.country.ui.screens

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
import com.example.presentation.FullScreenCenteredContent
import com.example.feature.country.R
import com.example.feature.country.ui.viewmodel.CountryDetailViewModel
import kotlin.text.orEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    viewModel: CountryDetailViewModel
) {
    val title = stringResource(R.string.title_country_detail)
    val country by viewModel.detail.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = title,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        FullScreenCenteredContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
}
