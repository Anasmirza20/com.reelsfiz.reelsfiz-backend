package com.reelsfiz.tables

import org.ktorm.schema.*

object ReelsEntity : Table<Nothing>("reels") {
    val name =                  varchar("name")
    val path =                  varchar("path")
    val url =                   varchar("url")
    val userId =                long("userId")
    val userProfileUrl =        varchar("userProfileUrl")
    val shareLink =             varchar("shareLink")
    val sizeInMB =              varchar("sizeInMB")
    val categoryId =            int("categoryId")
    val userName =              varchar("userName")
    val likeCount =             int("likeCount")
    val commentCount =          int("commentCount")
    val downloadCount =         int("downloadCount")
    val isAlreadyLiked =        boolean("isAlreadyLiked")
    val createdAt =             varchar("createdAt")
    val id =                    int("id").primaryKey()
}
