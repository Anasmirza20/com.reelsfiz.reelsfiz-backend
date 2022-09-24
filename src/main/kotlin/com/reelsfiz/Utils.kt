package com.reelsfiz

object Utils {
    fun String.isFileNameValid(): Boolean {
        val names = this.split(".")
        if (names.size != 2)
            return false
        return names[1] == "mp4" || names[1] == "m3u8"
    }
}