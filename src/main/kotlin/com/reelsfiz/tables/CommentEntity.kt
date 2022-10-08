package com.reelsfiz.tables

import org.ktorm.schema.*

object CommentEntity : Table<Nothing>("comments") {
    val content =               varchar("content")
    val userId =                int("userId")
    val userProfileImage =        varchar("userProfileImage")
    val reelId =                int("reelId")
    val videoName =             varchar("videoName")
    val username =             varchar("username")
    val createdAt =             varchar("createdAt")
    val id =                    int("id").primaryKey()
}

