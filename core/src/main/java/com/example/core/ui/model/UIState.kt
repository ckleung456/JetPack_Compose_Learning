package com.example.core.ui.model

import com.example.core.network.Kind

sealed interface UIState<out T> {
    object Loading : UIState<Nothing>
    data class Success<T>(val data: T) : UIState<T>
    data class Error(val message: String, val kind: Kind) : UIState<Nothing>
    object Default : UIState<Nothing>
}
