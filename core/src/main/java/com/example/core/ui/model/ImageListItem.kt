package com.example.core.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageListItem(
    val id: String? = null,
    val imageUrl: String,
    val title: String? = null,
    val subTitle: String? = null,
    val type: ImageItemType = ImageItemType.DEFAULT,
    val metadata: String? = null
)

enum class ImageItemType {
    DEFAULT, LARGE, BADGE,
}