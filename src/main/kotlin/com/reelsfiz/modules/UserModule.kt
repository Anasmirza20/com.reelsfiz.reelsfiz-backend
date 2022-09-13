package com.reelsfiz.modules

import com.reelsfiz.db.DatabaseConnection
import com.reelsfiz.models.BaseModel
import com.reelsfiz.models.UserModel
import com.reelsfiz.tables.UserEntity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.insert


private val db = DatabaseConnection.database

fun Application.userModule() {
    routing {
        signUp()
    }
}

private fun Route.signUp() {
    post("/signIn") {
        val user = call.receive<UserModel>()
        kotlin.runCatching {
            db.insert(UserEntity) {
                set(it.userName, user.userName)
                set(it.authToken, user.authToken)
                set(it.createdAt, user.createdAt)
                set(it.email, user.email)
                set(it.profileImageUrl, user.profileImageUrl)
            }
        }.onSuccess {
            if (it == 1)
                call.respond(
                    HttpStatusCode.OK, BaseModel<String?>(
                        success = false,
                        statusCode = HttpStatusCode.NotFound.value
                    )
                )
            else
                call.respond(
                    HttpStatusCode.BadRequest,
                    BaseModel<String?>(
                        message = "Something went wrong",
                        success = false,
                        statusCode = HttpStatusCode.NotFound.value
                    )
                )
        }.onFailure {
            val message = it.message
            call.respond(
                HttpStatusCode.BadRequest,
                BaseModel<String?>(
                    message = message,
                    success = false,
                    statusCode = HttpStatusCode.NotFound.value
                )
            )
        }
    }


}