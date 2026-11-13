package com.example.feature.business.model.domain

sealed interface BusinessDetailAction {
    data object OnRetry: BusinessDetailAction
}