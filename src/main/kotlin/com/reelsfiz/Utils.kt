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
}