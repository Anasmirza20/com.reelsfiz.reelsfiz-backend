package com.reelsfiz

import com.reelsfiz.plugins.configureRouting
import com.reelsfiz.plugins.configureSecurity
import com.reelsfiz.plugins.configureSerialization
import com.reelsfiz.reelsModule.reelsRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        configureSerialization()
        configureSecurity()
        configureRouting()
        reelsRoutes()
        GlobalScope.launch(IO) {
            putDataInBucket()

        }

    }.start(wait = true)
}
