package com.inihood.android.whatsappclone.model

import com.google.firebase.Timestamp


object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
    const val LOCATION = "LOCATION"
    const val DOCUMENT = "DOCUMENT"
    const val CONTACT = "CONTACT"
}

interface Message {
    val time: Timestamp
    val senderId: String
    val recipientId: String
    val imagePath: String
    val type: String
    val key: String
    val sms_date: String
    val sms_section_date: String
}