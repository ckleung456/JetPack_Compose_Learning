package com.example.presentation.location

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.example.shared.lib.LOCATION_PERMISSIONS
import com.example.shared.lib.hasLocationPermissions

@Composable
fun rememberLocationPermissionRequester(
    onResult: (granted: Boolean) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        currentOnResult(grants.values.any { it })
    }
    return remember(context, launcher) {
        {
            if (context.hasLocationPermissions()) {
                currentOnResult(true)
            } else {
                launcher.launch(LOCATION_PERMISSIONS)
            }
        }
    }
}
