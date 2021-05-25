package com.inihood.android.whatsappclone.utils

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.inihood.android.whatsappclone.model.Message
import android.util.Base64
import com.inihood.android.whatsappclone.model.MessageType
import com.inihood.android.whatsappclone.model.UserMessage
import com.inihood.android.whatsappclone.utils.ChatUtils.iv
import com.inihood.android.whatsappclone.utils.ChatUtils.salt
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec



object ChatUtils {

        //var secretKey = generateRandomIV()
        // val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sA=" //Secret key
        val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
        val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX

    fun sendMessage(message: Message, OtherUserId: String, currentUserId: String) {
        val firebaseFirestore: FirebaseFirestore?
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseFirestore.collection(Constants.messages)
                .document(currentUserId)
                .collection(OtherUserId)
                .add(message).addOnCompleteListener {
                    if (it.isSuccessful){
                        firebaseFirestore.collection(Constants.messages)
                                .document(OtherUserId)
                                .collection(currentUserId)
                                .add(message)
                    }
                }


    }


    fun processMessage(currentUserId: String, otherUserId: String,
                       sms: String, other_user_image: String?, other_user_name: String?,
                       messageType: String, imageUri: String){
//        val key = generateRandomIV()
//        val encryptedText = encrypt(sms, key)
//        Log.d("message", "processMessage: " + encryptedText)
//        Log.d("message", "processKey: " + key)
        val messageToSend = UserMessage(sms, Timestamp.now(),
                FirebaseAuth.getInstance().currentUser!!.uid,imageUri ,
                otherUserId,  messageType, "key",
                TimeUtils.getFormattedTimeEvent(System.currentTimeMillis()), TimeUtils.getDate())
        sendMessage(messageToSend, otherUserId, currentUserId)
        addToRecentChats(currentUserId, otherUserId, sms, other_user_image, other_user_name)

    }

private fun addToRecentChats(currentUserId: String, otherUserId: String, sms: String,
                             otherUserImage: String?, otherUserName: String?) {


    val itemsCurrentUser = HashMap<String, Any>()
    itemsCurrentUser["user_id"] = otherUserId
    itemsCurrentUser["time"] = Timestamp.now()
    itemsCurrentUser["last_sms"] = sms
    itemsCurrentUser["sms_type"] = "type"


    val itemsOtherUser = HashMap<String, Any>()
    itemsOtherUser["user_id"] = currentUserId
    itemsOtherUser["time"] = Timestamp.now()
    itemsOtherUser["last_sms"] = sms
    itemsOtherUser["sms_type"] = "type"


    val firebaseFirestore: FirebaseFirestore?
    firebaseFirestore = FirebaseFirestore.getInstance()
    firebaseFirestore.collection(Constants.users)
            .document(currentUserId)
            .collection(Constants.chats)
            .document(otherUserId)
            .set(itemsCurrentUser).addOnCompleteListener {
                if (it.isSuccessful){
                    // do the same for the other user
                    firebaseFirestore.collection(Constants.users)
                            .document(otherUserId)
                            .collection(Constants.chats)
                            .document(currentUserId)
                            .set(itemsOtherUser)
                }
            }
}
}

fun generateRandomIV(): String {
        val ranGen = SecureRandom()
        val aesKey = ByteArray(16)
        ranGen.nextBytes(aesKey)
        val result = StringBuffer()
        for (b in aesKey) {
            result.append(String.format("%02x", b))
        }
        return if (16 > result.toString().length) {
            result.toString()
        } else {
            result.toString().substring(0, 16)
        }
    }
    private fun encrypt(strToEncrypt: String, key: String) :  String?{
        try {
            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance(Constants.secreteKeyFactory)
            val spec =  PBEKeySpec(key.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey =  SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance(Constants.encryptionModeAndPadding)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            return Base64.encodeToString(cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)), Base64.DEFAULT)
        }
        catch (e: Exception) {
            println("Error while encrypting: $e")
        }
        return null
    }
