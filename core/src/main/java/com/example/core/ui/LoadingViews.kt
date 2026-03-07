package com.example.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Simple centered loading
@Composable
fun SimpleLoadingView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Loading with text
@Composable
fun LoadingWithText(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Linear progress at top
@Composable
fun TopLoadingBar(
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        LinearProgressIndicator(
            modifier = modifier
                .fillMaxWidth()
                .height(2.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

//@Composable
//fun PullToRefreshScreen(
//    isLoading: Boolean,
//    onRefresh: () -> Unit,
//    content: @Composable () -> Unit
//) {
//    val pullRefreshState = rememberPullRefreshState(
//        refreshing = isLoading,
//        onRefresh = onRefresh
//    )
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .pullRefresh(pullRefreshState)
//    ) {
//        content()
//
//        PullRefreshIndicator(
//            refreshing = isLoading,
//            state = pullRefreshState,
//            modifier = Modifier.align(Alignment.TopCenter)
//        )
//    }
//}