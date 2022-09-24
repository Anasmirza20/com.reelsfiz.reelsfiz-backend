package com.reelsfiz.tables

import org.ktorm.schema.Table
import org.ktorm.schema.int

object LikeEntity : Table<Nothing>("likes") {
    val userId = int("userId")
    val reelId = int("reelId")
}