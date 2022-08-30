package com.reelsfiz.models

import kotlinx.serialization.Serializable


@Serializable
data class ReelsModel(
    val name: String? = null,
    var path: String? = null,
    var url: String? = null,
    val userId: Long? = null,
    val userProfileUrl: String? = null,
    var sizeInMB: String? = null,
    val categoryId: Int? = null,
    val userName: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    var downloadCount: Int = 0,
    val createdAt: String? = null,
    val timeStamp: String? = null,
    var id: Int = 0
)