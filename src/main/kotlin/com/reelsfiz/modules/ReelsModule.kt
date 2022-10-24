package com.reelsfiz.modules

import com.reelsfiz.Constants
import com.reelsfiz.Constants.DEFAULT_PAGE_SIZE
import com.reelsfiz.Extensions.badRequest
import com.reelsfiz.Extensions.checkNull
import com.reelsfiz.Extensions.notFound
import com.reelsfiz.Extensions.success
import com.reelsfiz.Utils.isFileNameValid
import com.reelsfiz.db.DatabaseConnection
import com.reelsfiz.models.BaseModel
import com.reelsfiz.models.LikeModel
import com.reelsfiz.models.ReelsKeys.CATEGORY_ID
import com.reelsfiz.models.ReelsKeys.CREATED_AT
import com.reelsfiz.models.ReelsKeys.NAME
import com.reelsfiz.models.ReelsKeys.PAGE_NO
import com.reelsfiz.models.ReelsKeys.PAGE_SIZE
import com.reelsfiz.models.ReelsKeys.PATH
import com.reelsfiz.models.ReelsKeys.SHARE_LINK
import com.reelsfiz.models.ReelsKeys.SIZE_IN_MB
import com.reelsfiz.models.ReelsKeys.URL
import com.reelsfiz.models.ReelsKeys.USER_ID
import com.reelsfiz.models.ReelsKeys.USER_NAME
import com.reelsfiz.models.ReelsKeys.USER_PROFILE_URL
import com.reelsfiz.models.ReelsModel
import com.reelsfiz.putObject
import com.reelsfiz.tables.LikeEntity
import com.reelsfiz.tables.ReelsEntity
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.io.File


private val db = DatabaseConnection.database

fun Application.reelsRoutes() {
    routing {
        get("/getReels") {
            call.checkExceptionAndRespond(db.from(ReelsEntity).select().where { ReelsEntity.url.isNotNull() })
            /*            kotlin.runCatching {
                            val request = call.request.queryParameters
                            val reels =
                                db.from(ReelsEntity).select().where { ReelsEntity.url.isNotNull() }.setLimit(request)
                            call.checkNull(data = reels)
                        }.onFailure {
                            call.badRequest(it.message)
                        }*/
        }

        get("/getUserReels") {
            call.checkExceptionAndRespond(db.from(ReelsEntity).select()
                .where { ReelsEntity.url.isNotNull() and (ReelsEntity.userId eq call.request.queryParameters[USER_ID]?.toLong()!!) })/*kotlin.runCatching {
                val request = call.request.queryParameters
                val reels = db.from(ReelsEntity).select()
                    .where { ReelsEntity.url.isNotNull() and (ReelsEntity.userId eq request[USER_ID]?.toLong()!!) }
                    .setLimit(request)
                call.checkNull(data = reels)
            }.onFailure {
                call.badRequest(it.message)
            }*/
        }


        get("/getReelsByCategory") {
            call.checkExceptionAndRespond(
                db.from(ReelsEntity).select()
                    .where { ReelsEntity.url.isNotNull() and (ReelsEntity.categoryId eq call.request.queryParameters[CATEGORY_ID]?.toIntOrNull()!!) }
            )
            /* kotlin.runCatching {
                 val request = call.request.queryParameters
                 val reels = db.from(ReelsEntity).select()
                     .where { ReelsEntity.url.isNotNull() and (ReelsEntity.categoryId eq request[CATEGORY_ID]?.toIntOrNull()!!) }
                     .setLimit(request)
                 call.checkNull(data = reels)
             }.onFailure {
                 call.badRequest(it.message)
             }*/
        }

        get("/getReelById") {
            kotlin.runCatching {
                val request = call.request.queryParameters
                db.from(ReelsEntity).select()
                    .where { ReelsEntity.url.isNotNull() and (ReelsEntity.id eq request[Constants.REEL_ID]?.toIntOrNull()!!) }
                    .map {
                        call.success(it.getReelModel())
                        return@get
                    }
                call.notFound()
            }.onFailure {
                call.badRequest(it.message)
            }
        }

        post("/postReel") {
//            val reel = call.receive<ReelsModel>()

            kotlin.runCatching {
                val multipartData = call.receiveMultipart()
                var fileName: String
                val reel = ReelsModel()
                var url: String? = null
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                NAME -> reel.name = part.value
                                PATH -> reel.path = part.value
                                URL -> reel.url = part.value
                                USER_ID -> reel.userId = part.value.toLong()
                                USER_PROFILE_URL -> reel.userProfileUrl = part.value
                                SHARE_LINK -> reel.shareLink = part.value
                                SIZE_IN_MB -> reel.sizeInMB = part.value
                                CATEGORY_ID -> reel.categoryId = part.value.toInt()
                                USER_NAME -> reel.userName = part.value
                                CREATED_AT -> reel.createdAt = part.value
                            }
                        }

                        is PartData.FileItem -> {
                            kotlin.runCatching {
                                fileName = part.originalFileName as String
                                if (fileName.isFileNameValid()) {
                                    val fileBytes = part.streamProvider().readBytes()
                                    val file = File(fileName)
                                    file.writeBytes(fileBytes)
                                    putObject(Constants.REELS_BUCKET, fileName, fileName) {
                                        if (file.exists()) file.delete()
                                        url = it + fileName
                                        url?.replace(" ", "+")
                                        reel.url = url
                                    }
                                } else {
                                    call.badRequest("File format not supported")
                                }
                            }.onFailure {
                                call.badRequest(it.message)
                            }
                        }

                        else -> {}
                    }
                }

                val result = db.insertAndGenerateKey(ReelsEntity) {
                    set(it.name, reel.name)
                    set(it.userId, reel.userId)
                    set(it.categoryId, reel.categoryId)
                    set(it.url, url)
                    set(it.commentCount, reel.commentCount)
                    set(it.downloadCount, reel.downloadCount)
                    set(it.likeCount, reel.likeCount)
                    set(it.path, reel.path)
                    set(it.createdAt, reel.createdAt)
                    set(it.userName, reel.userName)
                    set(it.sizeInMB, reel.sizeInMB)
                    set(it.userProfileUrl, reel.userProfileUrl)
                    set(it.shareLink, reel.shareLink)
                }

                if (result is Int) db.from(ReelsEntity).select().where { ReelsEntity.id eq result }.limit(1)
                    .map { set ->
                        call.respond(HttpStatusCode.OK, BaseModel(data = set.getReelModel()))
                    }

            }.onFailure {
                call.badRequest(it.message)
            }
        }
        likeReel()
    }
}

private fun Route.likeReel() {
    post("/likeReel") {
        val request = call.receive<LikeModel>()
        db.from(LikeEntity).select().where {
            (LikeEntity.reelId eq request.reelId and (LikeEntity.userId eq request.userId))
        }.map {
            call.respond(
                HttpStatusCode.OK, BaseModel<String?>(
                    message = "Already Liked"
                )
            )
            return@post
        }
        val response = db.insert(LikeEntity) {
            set(it.reelId, request.reelId)
            set(it.userId, request.userId)
        }

        if (response == 1) {
            db.update(ReelsEntity) {
                set(
                    it.likeCount,
                    db.from(LikeEntity).select().where { LikeEntity.reelId eq request.reelId }.totalRecords
                )
                where { ReelsEntity.id eq request.reelId }
            }
            call.respond(HttpStatusCode.OK, BaseModel<String?>())
        } else call.badRequest()
    }
}

private fun QueryRowSet.getReelModel(request: Parameters? = null) = ReelsModel(
    id = this[ReelsEntity.id]!!,
    name = this[ReelsEntity.name]!!,
    url = this[ReelsEntity.url]!!,
    userName = this[ReelsEntity.userName]!!,
    createdAt = this[ReelsEntity.createdAt]!!,
    path = this[ReelsEntity.path]!!,
    userId = this[ReelsEntity.userId]!!,
    userProfileUrl = this[ReelsEntity.userProfileUrl]!!,
    shareLink = this[ReelsEntity.shareLink],
    sizeInMB = this[ReelsEntity.sizeInMB],
    categoryId = this[ReelsEntity.categoryId]!!,
    likeCount = this[ReelsEntity.likeCount]!!,
    commentCount = this[ReelsEntity.commentCount]!!,
    downloadCount = this[ReelsEntity.downloadCount]!!,
).also {
    if (request != null) it.isAlreadyLiked =
        if (request[USER_ID].isNullOrEmpty() || request[USER_ID]?.toIntOrNull() == null) false
        else db.from(LikeEntity).select()
            .where { LikeEntity.reelId eq this[ReelsEntity.id]!! and (LikeEntity.userId eq request[USER_ID]?.toIntOrNull()!!) }.totalRecords > 0

}

fun Query.setLimit(request: Parameters): List<ReelsModel>? {
    val pageSize = request[PAGE_SIZE]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE
    val pageNo = request[PAGE_NO]?.toIntOrNull()
    if (pageNo != null)
        return this.limit(if (pageNo <= 1) 0 else (pageNo.minus(1)).times(pageSize), pageSize).map {
            it.getReelModel(request)
        }
    return null
}

private suspend inline fun ApplicationCall.checkExceptionAndRespond(query: Query) {
    kotlin.runCatching {
        val request = this.request.queryParameters
        val reels = query.setLimit(request)
        this.checkNull(data = reels)
    }.onFailure {
        this.badRequest(it.message)
    }
}