package com.example.presentation.model

import com.example.domain.DataError
import com.example.domain.Error
import com.example.domain.Kind

/**
 * Maps a domain [Error] to a [UIState.Error] for display.
 * [DataError]s keep their message and kind; any other error is treated as unexpected.
 */
fun Error.toUiError(): UIState.Error = when (this) {
    is DataError -> UIState.Error(message = message.orEmpty(), kind = kind)
    else -> UIState.Error(message = "", kind = Kind.UNEXPECTED)
}
