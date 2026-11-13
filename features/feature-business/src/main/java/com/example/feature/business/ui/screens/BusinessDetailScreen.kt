package com.example.feature.business.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.business.R
import com.example.feature.business.model.domain.BusinessDetailAction
import com.example.feature.business.model.domain.BusinessDetailUi
import com.example.feature.business.ui.components.BusinessRatingRow
import com.example.feature.business.ui.viewmodel.BusinessDetailViewModel
import com.example.presentation.AppTopBar
import com.example.presentation.ErrorView
import com.example.presentation.ImageItemView
import com.example.presentation.LinkText
import com.example.presentation.SimpleLoadingView
import com.example.presentation.TagListView
import com.example.presentation.UIStatefulContent
import com.example.presentation.model.ImageItemType
import com.example.presentation.model.ImageListItem
import com.example.presentation.model.TextLinkItem

@Composable
fun BusinessDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: BusinessDetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val title = stringResource(R.string.title_business_detail)
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = title,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            UIStatefulContent(
                state = state,
                loadingContent = { SimpleLoadingView() },
                successContent = { content ->
                    BusinessDetail(
                        modifier = Modifier.fillMaxSize(),
                        businessUi = content
                    )
                },
                errorContent = { message , _ ->
                    ErrorView(
                        message = message,
                        onRetry = {
                            viewModel.onAction(BusinessDetailAction.OnRetry)
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun BusinessDetail(
    modifier: Modifier = Modifier,
    businessUi: BusinessDetailUi
) {
    val yesText = stringResource(R.string.yes)
    val noText = stringResource(R.string.no)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Icon
            val imageUrl = businessUi.imageUrl
            if (imageUrl.isNullOrBlank().not()) {
                ImageItemView(
                    item = ImageListItem(
                        imageUrl = imageUrl,
                        type = ImageItemType.LARGE
                    )
                )
            }

            // Title
            val title = businessUi.name
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }

            BusinessRatingRow(
                modifier = modifier,
                ratingText = businessUi.ratingText,
                reviewCount = businessUi.reviewCount,
            )

            // address
            val address = businessUi.address
            if (address.isNotBlank()) {
                Row(
                    modifier = modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    val distance = businessUi.distanceText
                    if (distance.isNullOrBlank().not()) {
                        Text(
                            text = distance,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Menu
            val menu = businessUi.menu
            if (menu.isNullOrBlank().not()) {
                LinkText(
                    modifier = modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(0.8f),
                    textResId = R.string.detail_menu,
                    links = listOfNotNull(
                        TextLinkItem(
                            textResId = R.string.click_here,
                            url = menu
                        )
                    ),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            listOfNotNull(
                businessUi.phone?.let { stringResource(R.string.business_phone, it) },
                stringResource(R.string.business_claimed, if (businessUi.isClaimed) yesText else noText),
                stringResource(R.string.business_closed, if (businessUi.isClosed) yesText else noText),
                businessUi.dateOpened?.let { stringResource(R.string.business_date_opened, it) },
                businessUi.dateClosed?.let { stringResource(R.string.business_date_closed, it) },
            ).forEach {
                Text(
                    modifier = modifier.padding(top = 16.dp),
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TagListView(
                modifier = modifier,
                tags = businessUi.categories.filter { it.isNotBlank() },
                isClickable = false
            )

            businessUi.hours?.let { hours ->
                Spacer(modifier = Modifier.height(16.dp))
                (listOf(
                    stringResource(R.string.business_is_open_now, if (hours.isOpenNow) yesText else noText),
                    stringResource(R.string.business_hours_type, hours.hoursType)
                ) + hours.open.map {
                    stringResource(R.string.business_day_hours, stringResource(it.day), it.start, it.end, if (it.isOvernight) yesText else noText)
                }).forEach {
                    Text(
                        modifier = modifier.padding(top = 16.dp),
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            val specialHours = businessUi.specialHours

            if (specialHours.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.business_special_hours),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                businessUi.specialHours.map {
                    stringResource(R.string.business_day_hours, it.date, it.start, it.end, if (it.isOvernight) yesText else noText) + ", " + stringResource(R.string.business_close, if (it.isClosed) yesText else noText)
                }.forEach {
                    Text(
                        modifier = modifier.padding(top = 16.dp),
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
