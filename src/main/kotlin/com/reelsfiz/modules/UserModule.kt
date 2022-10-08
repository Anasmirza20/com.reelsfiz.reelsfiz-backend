package com.reelsfiz.modules

import com.reelsfiz.Extensions.badRequest
import com.reelsfiz.Extensions.notFound
import com.reelsfiz.Extensions.success
import com.reelsfiz.db.DatabaseConnection
import com.reelsfiz.models.ReelsKeys
import com.reelsfiz.models.UserModel
import com.reelsfiz.tables.UserEntity
import io.ktor.server.application.*
import io.ktor.server.request.*
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
                call.success(it.getUserModel())
                return@post
            }
            db.insertAndGenerateKey(UserEntity) {
                setUserValues(user, it)
            }
        }.onSuccess {
            if (it is Int) {
                db.from(UserEntity).select().where { UserEntity.id eq it }.limit(1).map { user ->
                    call.success(user.getUserModel())
                }
            } else
                call.badRequest("Something went wrong")
        }.onFailure {
            val message = it.message
            call.badRequest(message)
        }
    }

    get("/getProfile") {
        kotlin.runCatching {
            val request = call.request.queryParameters
            db.from(UserEntity).select()
                .where { UserEntity.id eq request[ReelsKeys.USER_ID]?.toIntOrNull()!! }.map {
                    call.success(it.getUserModel())
                    return@get
                }
            call.notFound()
        }.onFailure {
            call.badRequest(it.message)
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

