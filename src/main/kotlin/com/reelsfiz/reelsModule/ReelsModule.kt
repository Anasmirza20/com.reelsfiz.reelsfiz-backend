package com.reelsfiz.reelsModule

import com.reelsfiz.db.DatabaseConnection
import com.reelsfiz.models.BaseModel
import com.reelsfiz.models.ReelsModel
import com.reelsfiz.tables.ReelsEntity
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import java.io.File


fun Application.reelsRoutes() {
    val db = DatabaseConnection.database
    routing {
        get("/getReels") {
            val reels = db.from(ReelsEntity).select().map {
                ReelsModel(
                    id = it[ReelsEntity.id]!!,
                    name = it[ReelsEntity.name]!!,
                    url = it[ReelsEntity.url]!!,
                    userName = it[ReelsEntity.userName]!!,
                    createdAt = it[ReelsEntity.createdAt]!!,
                    path = it[ReelsEntity.path]!!,
                    userId = it[ReelsEntity.userId]!!,
                    userProfileUrl = it[ReelsEntity.userProfileUrl]!!,
                    sizeInMB = it[ReelsEntity.sizeInMB]!!,
                    categoryId = it[ReelsEntity.categoryId]!!,
                    likeCount = it[ReelsEntity.likeCount]!!,
                    commentCount = it[ReelsEntity.commentCount]!!,
                    downloadCount = it[ReelsEntity.downloadCount]!!,
                    timeStamp = it[ReelsEntity.timeStamp]!!,
                )
            }
            call.respond(
                HttpStatusCode.OK, BaseModel(
                    data = reels,
                    statusCode = HttpStatusCode.OK.value,
                    message = "Success",
                    success = true
                )
            )
        }

        post("/postReels") {
            val reel = call.receive<ReelsModel>()
            val result = db.insert(ReelsEntity) {
                set(it.name, reel.name)
                set(it.userId, reel.userId)
                set(it.categoryId, reel.categoryId)
                set(it.url, reel.url)
                set(it.commentCount, reel.commentCount)
                set(it.downloadCount, reel.downloadCount)
                set(it.likeCount, reel.likeCount)
                set(it.path, reel.path)
                set(it.createdAt, reel.createdAt)
                set(it.timeStamp, reel.timeStamp)
                set(it.userName, reel.userName)
                set(it.sizeInMB, reel.sizeInMB)
                set(it.userProfileUrl, reel.userProfileUrl)
            }

            if (result == 1)
                call.respond(HttpStatusCode.OK, BaseModel(data = "Reel uploaded successfully"))
            else
                call.respond(
                    HttpStatusCode.NotFound,
                    BaseModel(
                        data = null,
                        message = "Something went wrong",
                        success = false,
                        statusCode = HttpStatusCode.NotFound.value
                    )
                )
        }

        var fileDescription = ""
        var fileName = ""

        post("/uploadReel") {
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }

                    is PartData.FileItem -> {
                        fileName = part.originalFileName as String
                        var fileBytes = part.streamProvider().readBytes()
                        File("uploads/$fileName").writeBytes(fileBytes)
                    }

                    else -> {}
                }
            }

            call.respondText("$fileDescription is uploaded to 'uploads/$fileName'")
        }
    }

}