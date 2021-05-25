package com.inihood.android.whatsappclone.model

import com.google.firebase.Timestamp


data class UserMessage(val text: String,
                   override val time: Timestamp,
                   override val senderId: String,
                       override val imagePath: String,
                   override val recipientId: String,
                   override val type: String = MessageType.TEXT,
                   override val key: String,
                   override val sms_date: String,
                   override val sms_section_date: String): Message