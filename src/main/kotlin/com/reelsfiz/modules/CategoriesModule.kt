package com.reelsfiz.modules

import com.reelsfiz.db.DatabaseConnection
import com.reelsfiz.models.BaseModel
import com.reelsfiz.models.CategoryModel
import com.reelsfiz.tables.CategoryEntity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select

private val db = DatabaseConnection.database


fun Application.categoriesRouting() {
    routing {
        getCategories()
        postCategories()
    }
}

private fun Route.getCategories() {
    get("/categories") {
        val categories = db.from(CategoryEntity).select().map {
            CategoryModel(
                id = it[CategoryEntity.id]!!,
                name = it[CategoryEntity.name]!!,
                image = it[CategoryEntity.image]!!,
                createdAt = it[CategoryEntity.createdAt]!!
            )
        }
        call.respond(
            HttpStatusCode.OK, BaseModel(
                data = categories
            )
        )
    }
}

private fun Route.postCategories() {
    post("/postCategories") {
        val user = call.receive<CategoryModel>()
        kotlin.runCatching {
            db.insert(CategoryEntity) {
                set(it.name, user.name)
                set(it.createdAt, user.createdAt)
                set(it.image, user.image)
            }
        }.onSuccess {
            if (it == 1)
                call.respond(HttpStatusCode.OK, BaseModel<String?>())
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