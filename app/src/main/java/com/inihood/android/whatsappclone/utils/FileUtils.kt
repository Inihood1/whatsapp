package com.inihood.android.whatsappclone.utils

import android.database.Cursor
import android.net.Uri
import java.text.CharacterIterator
import java.text.DecimalFormat
import java.text.StringCharacterIterator
import java.util.logging.Logger.global

object FileUtils {
    fun getSizeOfFile(size: Long): String? {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        if (size < sizeMb) return df.format(size / sizeKb).toString() +
                " Kb" else if (size < sizeGb) return df.format(size / sizeMb).toString() +
                " Mb" else if (size < sizeTerra) return df.format(size / sizeGb).toString() +
                " Gb"
        return ""
    }

    fun readableFileSize(bytes: Long): String {
        var bytes = bytes
        if (-1000 < bytes && bytes < 1000) {
            return "$bytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000
            ci.next()
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current())
    }




}
