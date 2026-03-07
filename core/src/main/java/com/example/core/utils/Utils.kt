package com.example.core.utils

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
}