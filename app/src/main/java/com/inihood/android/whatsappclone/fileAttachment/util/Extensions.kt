package com.inihood.android.whatsappclone.fileAttachment.util

import android.content.res.Resources

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()