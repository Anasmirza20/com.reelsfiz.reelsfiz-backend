package com.reelsfiz

object Utils {

    private val supportedImageTypes = listOf(
        "jpg",
        "jpeg",
        "png"
    )

    fun String.isFileNameValid(): Boolean {
        val names = this.split(".")
        if (names.size != 2)
            return false
        return names[1] == "mp4" || names[1] == "m3u8"
    }

    fun String.isImageFileNameValid(): Boolean {
        val names = this.split(".")
        if (names.size != 2)
            return false
        return supportedImageTypes.contains(names[1])
    }


    fun getExtension(name: String): String? {
        val names = name.split(".")
        if (names.size > 1)
            return names[1]
        return null
    }

    fun getFileNameFromUrl(uri: String?): String? {
        if (uri.isNullOrEmpty()) {
            return null
        }
        val slash = uri.lastIndexOf("/") + 1
        return if (slash >= 1) {
            uri.substring(slash)
        } else {
            // No filename.
            ""
        }
    }

    val AVATARS = listOf(
        "https://reelsfiz-user-images.s3.ap-south-1.amazonaws.com/avatars/avatar_1.png",
        "https://reelsfiz-user-images.s3.ap-south-1.amazonaws.com/avatars/avatar_2.png",
        "https://reelsfiz-user-images.s3.ap-south-1.amazonaws.com/avatars/avatar_3.png",
        "https://reelsfiz-user-images.s3.ap-south-1.amazonaws.com/avatars/avatar_4.png",
        "https://reelsfiz-user-images.s3.ap-south-1.amazonaws.com/avatars/avatar_5.png",
        "https://reelsfiz-user-images.s3.ap-south-1.amazonaws.com/avatars/avatar_6.png"
    )
}