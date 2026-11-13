package com.example.feature.business.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shared.lib.openAppSettings
import com.example.presentation.location.rememberLocationPermissionRequester
import com.example.presentation.AppTopBar
import com.example.presentation.ErrorView
import com.example.presentation.SimpleLoadingView
import com.example.presentation.UIStatefulContent
import com.example.shared.lib.ObserveAsEvents
import com.example.feature.business.R
import com.example.feature.business.model.domain.BusinessListAction
import com.example.feature.business.model.domain.BusinessListEvent
import com.example.feature.business.model.domain.BusinessListState
import com.example.feature.business.model.domain.BusinessSectionUi
import com.example.feature.business.model.domain.BusinessUi
import com.example.feature.business.ui.components.AddressDialog
import com.example.feature.business.ui.components.AddressHeader
import com.example.feature.business.ui.components.BusinessTestTags
import com.example.feature.business.ui.components.BusinessItem
import com.example.feature.business.ui.components.SearchAndFilterBar
import com.example.feature.business.ui.components.BusinessSectionHeader
import com.example.feature.business.ui.viewmodel.BusinessesViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@Composable
fun BusinessesScreen(
    modifier: Modifier = Modifier,
    viewModel: BusinessesViewModel = hiltViewModel(),
    onBusinessSelected: (String) -> Unit
) {
    val title = stringResource(R.string.title_business)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Location services
    val requestLocationPermission = rememberLocationPermissionRequester { granted ->
        viewModel.onAction(BusinessListAction.OnLocationPermissionResult(granted))
    }
    val locationPermissionsDeniedMessage = stringResource(R.string.location_permission_denied)
    val settingsLabel = stringResource(R.string.settings)

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            is BusinessListEvent.ShowSnackbar -> scope.launch {
                val message = event.messageId?.let(context::getString) ?: event.message
                if (message.isNullOrBlank()) return@launch
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message)
            }
            is BusinessListEvent.NavigateToDetail -> onBusinessSelected(event.id)
            is BusinessListEvent.ShowLocationPermissionDenied -> scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = locationPermissionsDeniedMessage,
                    actionLabel = settingsLabel,
                    withDismissAction = true
                )
                if (result == SnackbarResult.ActionPerformed) {
                    context.openAppSettings()
                }
            }
            is BusinessListEvent.RequestLocationPermission -> requestLocationPermission()
        }
    }

    BusinessListScreen(
        modifier = modifier,
        title = title,
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun BusinessListScreen(
    modifier: Modifier = Modifier,
    title: String,
    state: BusinessListState,
    onAction: (BusinessListAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = title
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AddressHeader(
                address = state.address,
                isLocating = state.isLocatingAddress,
                onClick = { onAction(BusinessListAction.OnAddressClick) }
            )
            SearchAndFilterBar(
                query = state.searchQuery,
                selectedFilter = state.filter,
                onQueryChanged = { onAction(BusinessListAction.OnSearchQueryUpdate(query = it)) },
                onFilterSelected = { onAction(BusinessListAction.OnFilterSelect(it)) }
            )
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onAction(BusinessListAction.OnRefresh) },
                modifier = Modifier.weight(1f)
            ) {
                UIStatefulContent(
                    state = state.content,
                    loadingContent = { SimpleLoadingView() },
                    errorContent = { message, _ ->
                        ErrorView(message = message)
                    },
                    successContent = { sections ->
                        BusinessListContent(
                            modifier = Modifier.fillMaxSize(),
                            sections = sections,
                            onAction = onAction
                        )
                    }
                )
            }
        }
    }

    AddressDialog(dialogUi = state.addressDialogUi, onAction = onAction)
}

@Composable
private fun BusinessListContent(
    modifier: Modifier = Modifier,
    sections: ImmutableList<BusinessSectionUi>,
    onAction: (BusinessListAction) -> Unit
) {
    BusinessList(
        modifier = modifier,
        sections = sections,
        onBusinessClick = {
            onAction(BusinessListAction.OnBusinessSelected(id = it.id))
        }
    )
}

@Composable
fun BusinessList(
    modifier: Modifier = Modifier,
    sections: ImmutableList<BusinessSectionUi>,
    onBusinessClick: (BusinessUi) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(BusinessTestTags.BUSINESS_LIST),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        sections.forEach { section ->
            stickyHeader(key = "header_${section.category}", contentType = "header") {
                BusinessSectionHeader(
                    category = section.category,
                    count = section.businesses.size
                )
            }
            items(
                items = section.businesses,
                key = { business -> "${section.category}_${business.id}" },
                contentType = { "business" }
            ) { business ->
                BusinessItem(
                    business = business,
                    onClick = {
                        onBusinessClick(it)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 104.dp))
            }
        }
    }
}
