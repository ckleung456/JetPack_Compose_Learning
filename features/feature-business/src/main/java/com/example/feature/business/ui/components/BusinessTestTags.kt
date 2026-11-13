package com.example.feature.business.ui.components

import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.BusinessFilter

/** Semantics test tags for the business screens, shared by the composables and their UI tests. */
object BusinessTestTags {
    const val ADDRESS_HEADER = "business_address_header"
    const val SEARCH_FIELD = "business_search_field"
    const val BUSINESS_LIST = "business_list"
    const val ADDRESS_SOURCE_DIALOG = "business_address_source_dialog"
    const val ADDRESS_EDIT_DIALOG = "business_address_edit_dialog"
    const val ADDRESS_EDIT_FIELD = "business_address_edit_field"

    fun filterChip(filter: BusinessFilter) = "business_filter_${filter.name.lowercase()}"
    fun sectionHeader(category: BusinessCategory) = "business_section_${category.name.lowercase()}"
}
