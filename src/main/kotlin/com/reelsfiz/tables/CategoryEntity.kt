package com.reelsfiz.tables

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object CategoryEntity : Table<Nothing>("categories") {
    val name                    = varchar("name")
    val image                       = varchar("image")
    val createdAt                   = varchar("createdAt")
    val id                          = int("id").primaryKey()
}