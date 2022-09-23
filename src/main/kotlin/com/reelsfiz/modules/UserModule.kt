package com.reelsfiz.modules

import com.reelsfiz.db.DatabaseConnection
import com.reelsfiz.models.BaseModel
import com.reelsfiz.models.ReelsModel
import com.reelsfiz.models.UserModel
import com.reelsfiz.tables.ReelsEntity
import com.reelsfiz.tables.UserEntity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*


private val db = DatabaseConnection.database

fun Application.userModule() {
    routing {
        signUp()
    }
}

private fun Route.signUp() {
    post("/signIn") {
        val user = call.receive<UserModel>()
        val query = db.from(UserEntity).select().limit(1)
        kotlin.runCatching {
            query.where { UserEntity.email eq user.email }.map {
                call.respond(
                    HttpStatusCode.OK, BaseModel<UserModel?>(
                        data = it.getUserModel(),
                        success = true,
                        statusCode = HttpStatusCode.OK.value
                    )
                )
                return@post
            }
            db.insertAndGenerateKey(UserEntity) {
                setUserValues(user, it)
            }
        }.onSuccess {
            if (it is Int) {
                db.from(UserEntity).select().where { UserEntity.id eq it }.limit(1).map { user ->
                    call.respond(
                        HttpStatusCode.OK, BaseModel<UserModel?>(
                            data = user.getUserModel(),
                            success = true,
                            statusCode = HttpStatusCode.OK.value
                        )
                    )
                }

            } else
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

private fun QueryRowSet.getUserModel(): UserModel = UserModel(
    id = this[UserEntity.id]!!,
    userName = this[UserEntity.userName]!!,
    createdAt = this[UserEntity.createdAt]!!,
    authToken = this[UserEntity.authToken]!!,
    profileImageUrl = this[UserEntity.profileImageUrl]!!,
    email = this[UserEntity.email]!!,
)

private fun AssignmentsBuilder.setUserValues(userModel: UserModel, userEntity: UserEntity) {
    set(userEntity.userName, userModel.userName)
    set(userEntity.authToken, userModel.authToken)
    set(userEntity.createdAt, userModel.createdAt)
    set(userEntity.email, userModel.email)
    set(userEntity.profileImageUrl, userModel.profileImageUrl)
}

private fun Route.success() {

}

