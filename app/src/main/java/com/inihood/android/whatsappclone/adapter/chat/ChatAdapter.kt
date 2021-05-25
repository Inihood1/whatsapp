package com.inihood.android.whatsappclone.adapter.chat

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.inihood.android.whatsappclone.R
import com.inihood.android.whatsappclone.emojicon.EmojiconTextView
import com.inihood.android.whatsappclone.model.MessageType
import com.inihood.android.whatsappclone.model.UserMessage
import com.inihood.android.whatsappclone.utils.Constants
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class ChatAdapter(val chatMessages: List<UserMessage>):
        RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val SENT = 0
    private val RECEIVED = 1
    private val currentUser: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view: View = if (viewType == SENT) {
            LayoutInflater.from(parent.context).inflate(
                    R.layout.item_chat_sent,
                    parent,
                    false
            )
        } else {
            LayoutInflater.from(parent.context).inflate(
                    R.layout.item_chat_received,
                    parent,
                    false)
        }
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].senderId.contentEquals(currentUser.currentUser!!.uid)) {
            SENT
        } else {
            RECEIVED
        }
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatMessages[position], holder)
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
        val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX

        var image: ImageView
        var message: EmojiconTextView
        var date: TextView
        fun bind(chat: UserMessage, holder: ChatViewHolder) {
          //  val sms = decrypt(chat.text, chat.key)
            // check for chat type
            if (chat.type == MessageType.TEXT){
                image.visibility = GONE
                message.visibility = VISIBLE
                message.setText(chat.text)
                date.setText(chat.sms_date)
            }else{
                image.visibility = VISIBLE
                message.visibility = GONE
                Glide.with(holder.itemView.getContext())
                        .load(chat.imagePath)
                        .placeholder(R.drawable.ic_person)
                        .thumbnail(0.5f)
                        .into(image)
                date.setText(chat.sms_date)
            }


        }
        init {
            image = itemView.findViewById(R.id.chat_image)
            message = itemView.findViewById(R.id.chat_message)
            date = itemView.findViewById(R.id.chat_date)

        }

        fun decrypt(strToDecrypt : String, key: String) : String? {
            try {
                val ivParameterSpec =  IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

                val factory = SecretKeyFactory.getInstance(Constants.secreteKeyFactory)
                val spec =  PBEKeySpec(key.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
                val tmp = factory.generateSecret(spec)
                val secretKey =  SecretKeySpec(tmp.encoded, "AES")

                val cipher = Cipher.getInstance(Constants.encryptionModeAndPadding)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
                return  String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
            } catch (e : Exception) {
                println("Error while decrypting: $e");
            }
            return null
        }
    }

}