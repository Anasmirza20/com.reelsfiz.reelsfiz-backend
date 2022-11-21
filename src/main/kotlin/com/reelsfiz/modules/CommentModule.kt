package com.reelsfiz.modules

import com.reelsfiz.Constants.REEL_ID
import com.reelsfiz.Extensions.badRequest
import com.reelsfiz.db.DatabaseConnection
import com.reelsfiz.models.BaseModel
import com.reelsfiz.models.CommentModel
import com.reelsfiz.tables.CommentEntity
import com.reelsfiz.tables.ReelsEntity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.util.*

private val db = DatabaseConnection.database

fun Application.commentModule() {
    routing {
        postComment()
        getComments()
    }
}


private fun Route.postComment() {
    post("/postComment") {
        val request = call.receive<CommentModel>()
        val response = db.insertAndGenerateKey(CommentEntity) {
            set(it.content, request.content)
            set(it.userId, request.userId)
            set(it.userProfileImage, request.userProfileImage)
            set(it.reelId, request.reelId)
            set(it.videoName, request.videoName)
            set(it.username, request.username)
            set(it.createdAt, Date().toString())
        }

        if (response is Int) {
            db.update(ReelsEntity) {
                set(
                    it.commentCount,
                    db.from(CommentEntity).select().where { CommentEntity.reelId eq request.reelId!! }.totalRecords
                )
                where { ReelsEntity.id eq request.reelId!! }
            }
            db.from(CommentEntity).select().where { CommentEntity.id eq response }.limit(1).map {
                call.respond(HttpStatusCode.OK, BaseModel(data = it.getCommentModule()))
            }
        } else
            call.badRequest()
    }
}

private fun Route.getComments() {
    get("/getComments") {
        kotlin.runCatching {
            val queryParams = call.request.queryParameters
            db.from(CommentEntity).select().where { CommentEntity.reelId eq queryParams[REEL_ID]?.toInt()!! }
                .map { set ->
                    set.getCommentModule()
                }
        }.onSuccess {
            call.respond(HttpStatusCode.OK, BaseModel(data = it))
        }.onFailure {
            call.badRequest(it.message)
        }
    }
}


private fun QueryRowSet.getCommentModule() = this[CommentEntity.id]?.let {
    CommentModel(
        id = it,
        content = this[CommentEntity.content],
        username = this[CommentEntity.username],
        videoName = this[CommentEntity.videoName],
        createdAt = this[CommentEntity.createdAt],
        userId = this[CommentEntity.userId],
        reelId = this[CommentEntity.reelId],
        userProfileImage = this[CommentEntity.userProfileImage]
    )
}

