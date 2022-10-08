package com.reelsfiz

import com.reelsfiz.Extensions.success
import com.reelsfiz.models.BaseModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

object Extensions {

    suspend fun ApplicationCall.badRequest(message: String? = "Bad Request") {
        respond(
            HttpStatusCode.BadRequest, BaseModel<String?>(
                data = null,
                message = message,
                success = false,
                statusCode = HttpStatusCode.BadRequest.value
            )
        )
    }


    suspend fun ApplicationCall.notFound(message: String? = "Not Found") {
        respond(
            HttpStatusCode.NotFound, BaseModel<String?>(
                data = null,
                message = message,
                success = false,
                statusCode = HttpStatusCode.NotFound.value
            )
        )
    }

    suspend inline fun <reified T> ApplicationCall.success(data: T, statusCode: Int = HttpStatusCode.OK.value) {
        respond(
            HttpStatusCode.OK, BaseModel<T>(
                data = data,
                statusCode = statusCode
            )
        )
    }

    suspend inline fun <reified T> ApplicationCall.checkNull(data: T?, statusCode: Int = HttpStatusCode.OK.value) {
        if (data != null)
            this.success(data = data)
        else this.badRequest()
    }

}