package com.reelsfiz

import com.reelsfiz.modules.categoriesRouting
import com.reelsfiz.modules.commentModule
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
import io.netty.handler.codec.http.HttpObjectDecoder.*
import io.netty.handler.codec.http.HttpServerCodec

fun main() {
    embeddedServer(Netty, port = (System.getenv("PORT") ?: "5000").toInt(), configure = {
        this.httpServerCodec = {
            HttpServerCodec(
                DEFAULT_MAX_INITIAL_LINE_LENGTH,
                DEFAULT_MAX_HEADER_SIZE,
                1000000 * 100,
                true, DEFAULT_INITIAL_BUFFER_SIZE, true, true
            )
        }
    }) {
        install(ContentNegotiation) {
            json()
        }
        configureSerialization()
        configureSecurity()
        configureRouting()
        reelsRoutes()
        userModule()
        categoriesRouting()
        commentModule()
    }.start(wait = true)
}
