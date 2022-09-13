package com.reelsfiz.models

import kotlinx.serialization.Serializable

@Serializable
data class CategoryModel(
    val name: String? = null,
    val image: String? = null,
    val createdAt: String? = null,
    val id: Int = 0
)
