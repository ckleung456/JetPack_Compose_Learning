package com.example.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

object Utils {
    inline fun <reified T> Any?.isListOfType(): Boolean {
        if (this !is List<*>) return false
        return this.all { it is T }
    }

    inline fun <reified T> Any.asSafeList(): List<T>? {
        return if (this.isListOfType<T>()) {
            @Suppress("UNCHECKED_CAST")
            this as List<T>
        } else null
    }

    @Composable
    fun <T> ObserveAsEvents(
        flow: Flow<T>,
        key1: Any? = null,
        key2: Any? = null,
        action: (T) -> Unit
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(flow, lifecycleOwner, key1, key2) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect(action)
            }
        }
    }
}