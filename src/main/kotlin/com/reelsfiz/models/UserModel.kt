package com.reelsfiz.models

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val userName: String? = null,
    val email: String,
    val authToken: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: String? = null,
    val id: Int = 0
)