package com.reelsfiz

import com.reelsfiz.modules.categoriesRouting
import com.reelsfiz.plugins.configureRouting
import com.reelsfiz.plugins.configureSecurity
import com.reelsfiz.plugins.configureSerialization
import com.reelsfiz.modules.reelsModule.reelsRoutes
import com.reelsfiz.modules.userModule
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*

fun main() {
    embeddedServer(Netty, port = 8082, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        configureSerialization()
        configureSecurity()
        configureRouting()
        reelsRoutes()
        userModule()
        categoriesRouting()
/*        GlobalScope.launch(IO) {
            putDataInBucket()

        }*/

    }.start(wait = true)
}
