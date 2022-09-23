package com.reelsfiz

import com.reelsfiz.modules.categoriesRouting
import com.reelsfiz.modules.reelsRoutes
import com.reelsfiz.modules.userModule
import com.reelsfiz.plugins.configureRouting
import com.reelsfiz.plugins.configureSecurity
import com.reelsfiz.plugins.configureSerialization
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*

fun main() {
    embeddedServer(Netty, port = (System.getenv("PORT") ?: "5000").toInt(), host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        configureSerialization()
        configureSecurity()
        configureRouting()
        reelsRoutes()
        userModule()
        categoriesRouting()
    }.start(wait = true)
}
