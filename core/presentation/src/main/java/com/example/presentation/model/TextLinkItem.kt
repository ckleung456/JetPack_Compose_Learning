package com.example.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class TextLinkItem(
    @StringRes val textResId: Int,
    val name: String? = null,
    val url: String
)