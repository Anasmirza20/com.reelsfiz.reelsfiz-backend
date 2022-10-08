package com.reelsfiz.models

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CommentModel(
    val content: String? = null,
    val userId: Int? = null,
    val userProfileImage: String? = null,
    val reelId: Int? = null,
    val videoName: String? = null,
    val username: String? = null,
    val createdAt: String? = null,
    var id: Int = 0
)
