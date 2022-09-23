package com.reelsfiz.modules

import com.reelsfiz.Constants
import com.reelsfiz.db.DatabaseConnection
import com.reelsfiz.models.BaseModel
import com.reelsfiz.models.ReelsKeys.CATEGORY_ID
import com.reelsfiz.models.ReelsKeys.CREATED_AT
import com.reelsfiz.models.ReelsKeys.NAME
import com.reelsfiz.models.ReelsKeys.PATH
import com.reelsfiz.models.ReelsKeys.SIZE_IN_MB
import com.reelsfiz.models.ReelsKeys.URL
import com.reelsfiz.models.ReelsKeys.USER_ID
import com.reelsfiz.models.ReelsKeys.USER_NAME
import com.reelsfiz.models.ReelsKeys.USER_PROFILE_URL
import com.reelsfiz.models.ReelsModel
import com.reelsfiz.putObject
import com.reelsfiz.tables.ReelsEntity
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.io.File


fun Application.reelsRoutes() {
    val db = DatabaseConnection.database
    routing {
        get("/getReels") {
            val reels = db.from(ReelsEntity).select().map {
                it.getReelModel()
            }
            call.respond(
                HttpStatusCode.OK, BaseModel(
                    data = reels, statusCode = HttpStatusCode.OK.value, message = "Success", success = true
                )
            )
        }

        post("/postReel") {
//            val reel = call.receive<ReelsModel>()

            kotlin.runCatching {
                val multipartData = call.receiveMultipart()

                var fileName: String
                val reel = ReelsModel()
                var url: String?=null
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                NAME -> reel.name = part.value
                                PATH -> reel.path = part.value
                                URL -> reel.url = part.value
                                USER_ID -> reel.userId = part.value.toLong()
                                USER_PROFILE_URL -> reel.userProfileUrl = part.value
                                SIZE_IN_MB -> reel.sizeInMB = part.value
                                CATEGORY_ID -> reel.categoryId = part.value.toInt()
                                USER_NAME -> reel.userName = part.value
                                CREATED_AT -> reel.createdAt = part.value
                            }
                        }

                        is PartData.FileItem -> {
                            kotlin.runCatching {
                                fileName = part.originalFileName as String
                                val fileBytes = part.streamProvider().readBytes()
                                val file = File(fileName)
                                file.writeBytes(fileBytes)
                                println("$fileBytes   $fileName")
                                putObject(Constants.REELS_BUCKET, fileName, fileName) {
                                    if (file.exists()) file.delete()
                                    url = it + fileName
                                    url?.replace(" ", "+")
                                    reel.url = url
                                }
                            }.onFailure {
                                println(it)
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
                }

                if (result is Int) db.from(ReelsEntity).select().where { ReelsEntity.id eq result }.limit(1)
                    .map { set ->
                        call.respond(HttpStatusCode.OK, BaseModel(data = set.getReelModel()))
                    }

            }.onFailure {
                call.respond(
                    HttpStatusCode.NotFound, BaseModel<String?>(
                        data = null, message = it.message, success = false, statusCode = HttpStatusCode.NotFound.value
                    )
                )

            }
        }
    }

}

private fun QueryRowSet.getReelModel() = ReelsModel(
    id = this[ReelsEntity.id]!!,
    name = this[ReelsEntity.name]!!,
    url = this[ReelsEntity.url]!!,
    userName = this[ReelsEntity.userName]!!,
    createdAt = this[ReelsEntity.createdAt]!!,
    path = this[ReelsEntity.path]!!,
    userId = this[ReelsEntity.userId]!!,
    userProfileUrl = this[ReelsEntity.userProfileUrl]!!,
    sizeInMB = this[ReelsEntity.sizeInMB]!!,
    categoryId = this[ReelsEntity.categoryId]!!,
    likeCount = this[ReelsEntity.likeCount]!!,
    commentCount = this[ReelsEntity.commentCount]!!,
    downloadCount = this[ReelsEntity.downloadCount]!!
)