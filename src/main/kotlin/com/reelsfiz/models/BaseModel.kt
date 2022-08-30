package com.reelsfiz.models

import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class BaseModel<T>(
    val data: T? = null,
    val message: String? = "Success",
    val success: Boolean = true,
    val statusCode: Int = HttpStatusCode.OK.value,
    val timeStamp: String = Date().toString()
)
