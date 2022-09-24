package com.reelsfiz.models

import kotlinx.serialization.Serializable

@Serializable
data class LikeModel(
    val userId: Int,
    val reelId: Int
)
