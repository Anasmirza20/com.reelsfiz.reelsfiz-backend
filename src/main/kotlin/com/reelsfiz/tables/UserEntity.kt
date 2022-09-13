package com.reelsfiz.tables

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object UserEntity : Table<Nothing>("users") {
    val userName                    = varchar("userName")
    val email                       = varchar("email")
    val authToken                   = varchar("authToken")
    val profileImageUrl             = varchar("profileImageUrl")
    val createdAt                   = varchar("createdAt")
    val id                          = int("id").primaryKey()
}