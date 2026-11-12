package com.example.feature.country.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.AppTopBar
import com.example.core.ui.FullScreenCenteredContent
import com.example.core.ui.SimpleListRowItem
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
