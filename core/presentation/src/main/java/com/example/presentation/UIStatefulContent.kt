package com.example.presentation

import androidx.compose.runtime.Composable
import com.example.domain.Kind
import com.example.presentation.model.UIState

@Composable
fun<T> UIStatefulContent(
    state: UIState<T>,
    loadingContent: @Composable () -> Unit = { SimpleLoadingView() },
    errorContent: @Composable (String, Kind) -> Unit,
    successContent: @Composable (T) -> Unit
) {
    when(state) {
        is UIState.Loading ->  loadingContent.invoke()
        is UIState.Error -> errorContent.invoke(state.message, state.kind)
        is UIState.Success -> successContent.invoke(state.data)
        else -> {}
    }
}