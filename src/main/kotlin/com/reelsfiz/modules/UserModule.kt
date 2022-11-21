package com.reelsfiz.modules

import com.reelsfiz.Constants
import com.reelsfiz.Extensions.badRequest
import com.reelsfiz.Extensions.notFound
import com.reelsfiz.Extensions.success
import com.reelsfiz.Utils.AVATARS
import com.reelsfiz.Utils.getFileNameFromUrl
import com.reelsfiz.Utils.isImageFileNameValid
import com.reelsfiz.db.DatabaseConnection
import com.reelsfiz.deleteBucketObs
import com.reelsfiz.models.ReelsKeys
import com.reelsfiz.models.UserModel
import com.reelsfiz.putImage
import com.reelsfiz.tables.UserEntity
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.io.File


private val db = DatabaseConnection.database

fun Application.userModule() {
    routing {
        signIn()
        updateProfile()
        getProfile()
        updateProfileImage()
    }
}

private fun Route.signIn() {
    post("/signIn") {
        val user = call.getUserModelFromMultiPart()
        val query = db.from(UserEntity).select().limit(1)
        when (user.loginType) {
            Constants.MOBILE -> {
                query.where { UserEntity.mobile eq user.mobile!! }.map {
                    call.success(it.getUserModel())
                    return@post
                }
            }

            Constants.EMAIL -> {
                query.where { UserEntity.email eq user.email!! }.map {
                    call.success(it.getUserModel())
                    return@post
                }
            }
        }
        kotlin.runCatching {
            db.insertAndGenerateKey(UserEntity) {
                setUserValues(user, it, false)
                set(it.profileImageUrl, AVATARS.random())
            }
        }.onSuccess {
            call.respondUserModel(it)
        }.onFailure {
            val message = it.message
            call.badRequest(message)
        }
    }

}

private fun Route.updateProfileImage() {
    post("/updateProfileImage") {
        call.updateProfile()
    }
}

private fun Route.getProfile() {
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

private fun Route.updateProfile() {
    post("/updateProfile") {
        call.updateProfile()
    }
}

private suspend fun ApplicationCall.updateProfile() {
    kotlin.runCatching {
        val user = getUserModelFromMultiPart(true)
        val result = db.update(UserEntity) {
            updateUserValues(user, it)
            where { UserEntity.id eq user.id }
        }
        if (result == 1) {
            respondUserModel(user.id)
            // todo : update the values in reels and comment table
        } else
            notFound()
    }.onFailure {
        badRequest(it.message)
    }
}

private suspend fun ApplicationCall.respondUserModel(response: Any?) {
    if (response is Int) {
        db.from(UserEntity).select().where { UserEntity.id eq response }.limit(1).map { user ->
            val userModel = user.getUserModel()
            if (userModel != null) {
                if (userModel.userName.isNullOrEmpty()) {
                    val userName = "User:" + userModel.id // create a default username if username is not available
                    val result = db.update(UserEntity) {
                        set(it.userName, userName)
                        where { UserEntity.id eq userModel.id }
                    }
                    if (result == 1) {
                        userModel.userName = userName
                    }
                }
                success(userModel)
                return
            }
        }
    } else
        badRequest("Something went wrong")
}

private fun QueryRowSet.getUserModel(): UserModel? = this[UserEntity.id]?.let {
    UserModel(
        id = it,
        userName = this[UserEntity.userName],
        createdAt = this[UserEntity.createdAt],
        authToken = this[UserEntity.authToken],
    ).also { user ->
        val image = this[UserEntity.profileImageUrl]
        image?.let { user.profileImageUrl = image }

        val email = this[UserEntity.email]
        email?.let { user.email = email }
        val mobile = this[UserEntity.mobile]
        email?.let { user.mobile = mobile }

    }
}

private fun AssignmentsBuilder.setUserValues(
    userModel: UserModel,
    userEntity: UserEntity,
    setProfileUrl: Boolean = true
) {
    set(userEntity.userName, userModel.userName)
    set(userEntity.authToken, userModel.authToken)
    set(userEntity.createdAt, userModel.createdAt)
    set(userEntity.email, userModel.email)
    set(userEntity.mobile, userModel.mobile)
    if (setProfileUrl)
        set(userEntity.profileImageUrl, userModel.profileImageUrl)
}

private fun UpdateStatementBuilder.updateUserValues(userModel: UserModel, userEntity: UserEntity) {
    userModel.apply {
        userModel.userName?.let { set(userEntity.userName, it) }
        userModel.email?.let { set(userEntity.email, userModel.email) }
        userModel.mobile?.let { set(userEntity.mobile, it) }
        userModel.profileImageUrl?.let { set(userEntity.profileImageUrl, it) }
    }

}

private suspend fun ApplicationCall.getUserModelFromMultiPart(fromUpdate: Boolean = false): UserModel {
    val multipartData = this.receiveMultipart()

    val user = UserModel()
    var fileName: String
    var url: String?

    multipartData.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                when (part.name) {
                    ReelsKeys.USER_NAME -> user.userName = part.value
                    ReelsKeys.ID -> user.id = part.value.toIntOrNull() ?: -1
                    ReelsKeys.EMAIL -> user.email = part.value
                    ReelsKeys.LOGIN_TYPE -> user.loginType = part.value
                    ReelsKeys.AUTH_TOKEN -> user.authToken = part.value
                    ReelsKeys.MOBILE -> user.mobile = part.value
                    ReelsKeys.CREATED_AT -> user.createdAt = part.value
                }
            }

            is PartData.FileItem -> {
                fileName = part.originalFileName as String
                if (fileName.isImageFileNameValid()) {
                    val fileBytes = part.streamProvider().readBytes()
                    val file = File(fileName)
                    file.writeBytes(fileBytes)
                    putImage(Constants.IMAGES_BUCKET, fileName, fileName) {
                        if (file.exists()) file.delete()
                        url = it + fileName
                        url?.replace(" ", "+")
                        user.profileImageUrl = url
                    }

                } else {
                    if (fileName.isNotEmpty())
                        deleteBucketObs(Constants.IMAGES_BUCKET, fileName)
                    throw BadRequestException("File format not supported")
                }
            }

            else -> {}
        }
    }
    if (fromUpdate) {
        db.from(UserEntity).select().where { UserEntity.id eq user.id }.limit(1).map { userEntity ->
            getFileNameFromUrl(userEntity.getUserModel()?.profileImageUrl)?.let {
                deleteBucketObs(Constants.IMAGES_BUCKET, it)
            }
        }
    }
    return user
}

