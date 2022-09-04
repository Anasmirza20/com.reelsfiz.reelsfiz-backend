package com.reelsfiz.reelsModule

import com.reelsfiz.Constants
import com.reelsfiz.db.DatabaseConnection
import com.reelsfiz.models.BaseModel
import com.reelsfiz.models.ReelsKeys.CATEGORY_ID
import com.reelsfiz.models.ReelsKeys.CREATED_AT
import com.reelsfiz.models.ReelsKeys.NAME
import com.reelsfiz.models.ReelsKeys.PATH
import com.reelsfiz.models.ReelsKeys.SIZE_IN_MB
import com.reelsfiz.models.ReelsKeys.TIME_STAMP
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
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import java.io.File


fun Application.reelsRoutes() {
    val db = DatabaseConnection.database
    routing {

        uploadReel()
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

        post("/postReel") {
//            val reel = call.receive<ReelsModel>()

            val multipartData = call.receiveMultipart()

            var fileName: String
            val reel = ReelsModel()
            var url: String
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
                            TIME_STAMP -> reel.timeStamp = part.value
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
                                if (file.exists())
                                    file.delete()
                                url = it + fileName
                                url.replace(" ", "+")
                                reel.url = url
                            }
                        }
                    }

                    else -> {}
                }
            }

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

        post("/uploadReel") {


        }
    }

}