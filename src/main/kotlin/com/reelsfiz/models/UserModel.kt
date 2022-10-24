package com.reelsfiz.models

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    var userName: String? = null,
    var email: String? = null,
    var mobile: String? = null,
    var authToken: String? = null,
    var profileImageUrl: String? = null,
    var createdAt: String? = null,
    var id: Int = 0
)