package com.example.feature.business.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.feature.business.R
import com.example.feature.business.model.domain.AddressDialogUi
import com.example.feature.business.model.domain.BusinessListAction

@Composable
fun AddressDialog(
    dialogUi: AddressDialogUi,
    onAction: (BusinessListAction) -> Unit
) {
    when (dialogUi) {
        is AddressDialogUi.Hidden -> Unit
        is AddressDialogUi.EnterAddress -> EditAddressDialog(
            text = dialogUi.address,
            onTextChange = { onAction(BusinessListAction.OnAddressTextChange(it)) },
            onConfirm = { onAction(BusinessListAction.OnAddressConfirm) },
            onDismiss = { onAction(BusinessListAction.OnDialogDismiss) }
        )
        is AddressDialogUi.ChooseSource -> AddressSourceDialog(
            onEnterAddress = { onAction(BusinessListAction.OnChooseEnterAddress) },
            onUserCurrentLocation = { onAction(BusinessListAction.OnChooseUseLocation) },
            onDismiss = { onAction(BusinessListAction.OnDialogDismiss) }
        )
    }
}

@Composable
fun AddressSourceDialog(
    onEnterAddress: () -> Unit,
    onUserCurrentLocation: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(""),
        title = { Text(stringResource(R.string.location_change_location)) },
        text = {
            Column {
                SourceOption(
                    text = stringResource(R.string.location_enter_address),
                    icon = { Icon(Icons.Filled.EditLocationAlt, contentDescription = stringResource(R.string.location_enter_address)) },
                    onClick = onEnterAddress
                )
                SourceOption(
                    text = stringResource(R.string.location_use_current_location),
                    icon = { Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.location_use_current_location)) },
                    onClick = onUserCurrentLocation
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun SourceOption(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = icon,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}

@Composable
fun EditAddressDialog(
    text: String,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val requestFocus = remember { FocusRequester() }
    val canConfirm = text.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(""),
        title = { Text(stringResource(R.string.location_enter_address)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                label = { Text(stringResource(R.string.location_address)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(requestFocus)
                    .testTag(""),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.location_enter_address)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (canConfirm) {
                            onConfirm()
                        }
                    }
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = canConfirm) {
                Text(stringResource(R.string.cd_search))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
    LaunchedEffect(Unit) {
        requestFocus.requestFocus()
    }
}