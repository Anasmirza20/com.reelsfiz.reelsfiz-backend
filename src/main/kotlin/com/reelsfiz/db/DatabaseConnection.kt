package com.reelsfiz.db

import org.ktorm.database.Database
import org.ktorm.support.mysql.MySqlDialect

object DatabaseConnection {
    val database = Database.connect(
        url = "jdbc:mysql://reelsfiz-db.cedyqoci5s9f.ap-south-1.rds.amazonaws.com:3306/reelsfiz",
        user = "root",
        password = "Reels$$356890$$",
        dialect = MySqlDialect()
    )
}

// Reels@3569