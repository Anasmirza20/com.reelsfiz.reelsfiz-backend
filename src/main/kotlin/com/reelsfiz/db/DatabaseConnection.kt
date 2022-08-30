package com.reelsfiz.db

import org.ktorm.database.Database

object DatabaseConnection {
    val database = Database.connect(
        url = "jdbc:mysql://localhost:3306/reelsfiz",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "Reels@3569"
    )
}