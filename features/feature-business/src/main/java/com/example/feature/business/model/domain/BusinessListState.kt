package com.example.feature.business.model.domain

import androidx.compose.runtime.Immutable
import com.example.presentation.model.UIState
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class BusinessListState(
    val address: String = DEFAULT_ADDRESS,
    val filter: BusinessFilter = BusinessFilter.ALL,
    val isRefreshing: Boolean = false,
    val content: UIState<ImmutableList<BusinessSectionUi>> = UIState.Loading,
    val searchQuery: String = "",
    val isLocatingAddress: Boolean = false,
    val addressDialogUi: AddressDialogUi = AddressDialogUi.Hidden,
) {
    companion object {
        const val DEFAULT_ADDRESS = "1000 Montgomery Street, San Francisco, CA"
    }
}

@Immutable
sealed interface AddressDialogUi {
    object Hidden : AddressDialogUi
    data class EnterAddress(val address: String) : AddressDialogUi
    object ChooseSource : AddressDialogUi
}
