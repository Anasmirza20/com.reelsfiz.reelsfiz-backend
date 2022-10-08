package com.reelsfiz.models

import kotlinx.serialization.Serializable


@Serializable
data class ReelsModel(
    var name: String? = null,
    var path: String? = null,
    var url: String? = null,
    var userId: Long? = null,
    var userProfileUrl: String? = null,
    var shareLink: String? = null,
    var sizeInMB: String? = null,
    var categoryId: Int? = null,
    var userName: String? = null,
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    var downloadCount: Int = 0,
    var createdAt: String? = null,
    var isAlreadyLiked: Boolean? = null,
    var id: Int = 0
)

object ReelsKeys {
    const val NAME = "name"
    const val PATH = "path"
    const val URL = "url"
    const val USER_ID = "userId"
    const val PAGE_SIZE = "pageSize"
    const val PAGE_NO = "pageNo"
    const val USER_PROFILE_URL = "userProfileUrl"
    const val SHARE_LINK = "shareLink"
    const val SIZE_IN_MB = "sizeInMB"
    const val CATEGORY_ID = "categoryId"
    const val USER_NAME = "userName"
    const val LIKE_COUNT = "likeCount"
    const val COMMENT_COUNT = "commentCount"
    const val DOWNLOAD_COUNT = "downloadCount"
    const val CREATED_AT = "createdAt"
    const val TIME_STAMP = "timeStamp"
    const val ID = "id"
}