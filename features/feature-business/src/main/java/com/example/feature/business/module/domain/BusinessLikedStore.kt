package com.example.feature.business.module.domain

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessLikedStore @Inject constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        private const val BUSINESS_LIKED_IDS = "businessLikedIds"
    }

    private val _businessLikedIds = MutableStateFlow(prefs.getStringSet(BUSINESS_LIKED_IDS, emptySet()).orEmpty().toSet())
    val businessLikedIds = _businessLikedIds.asStateFlow()

    fun toggleBusinessLiked(businessId: String): Boolean {
        val updated = _businessLikedIds.updateAndGet { ids -> if (businessId in ids) ids - businessId else ids + businessId }
        prefs.edit { putStringSet(BUSINESS_LIKED_IDS, updated) }
        return businessId in updated
    }

    fun clear() {
        _businessLikedIds.value = emptySet()
        prefs.edit { putStringSet(BUSINESS_LIKED_IDS, _businessLikedIds.value) }
    }
}
