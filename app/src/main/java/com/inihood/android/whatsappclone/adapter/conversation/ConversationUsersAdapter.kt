package com.inihood.android.whatsappclone.adapter.conversation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.curioustechizen.ago.RelativeTimeTextView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inihood.android.whatsappclone.R
import com.inihood.android.whatsappclone.emojicon.EmojiconTextView
import com.inihood.android.whatsappclone.model.Conversation
import com.inihood.android.whatsappclone.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ConversationUsersAdapter(
    query: Query,
    private val listener: ConvoUsersAdapterListener
) : ConversationFirestoreAdapter<ConversationUsersAdapter.UsersViewHolder>(query) {

    class UsersViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardView: CardView = itemView.findViewById(R.id.user_card)
        private val name: TextView = itemView.findViewById(R.id.user_name)
        private val lastSms: EmojiconTextView = itemView.findViewById(R.id.user_last_sms)
        private var user_id: String? = null
        private val profileImage: CircleImageView = itemView.findViewById(R.id.user_image)
        private val v = itemView.findViewById(R.id.user_last_date) as RelativeTimeTextView
        private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

        fun bind(
                snapshot: DocumentSnapshot,
                listener: ConvoUsersAdapterListener,
                holder: UsersViewHolder) {

            val conversation: Conversation? = snapshot.toObject(Conversation::class.java)
            user_id = snapshot.id.toString()

            // get user info
            val info: FirebaseFirestore =  FirebaseFirestore.getInstance()
            info.collection(Constants.users).document(user_id!!)
                    .addSnapshotListener { value, _ ->

                        name.text = value?.get("name") as CharSequence?

                        Glide.with(holder.itemView.getContext())
                            .load(value?.get("image"))
                            .placeholder(R.drawable.ic_person)
                            .thumbnail(0.5f)
                            .into(profileImage)


            }

            // get message info
            val messageInfo: FirebaseFirestore =  FirebaseFirestore.getInstance()
            messageInfo.collection(Constants.users).document(mAuth.currentUser?.uid!!)
                    .collection(Constants.chats).document(user_id!!)
                    .addSnapshotListener { value, _ ->

                        lastSms.text = value?.get("last_sms") as CharSequence?

                        val time: Timestamp = value?.data?.get("time") as Timestamp
                        val date: Date = time.toDate()
                        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
                        try {
                            val mDate: Date = sdf.parse(date.toString())
                            val timeInMilliseconds = mDate.time
                            v.setReferenceTime(timeInMilliseconds)
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }

                    }


            cardView.setOnClickListener {
                listener.onUsersSelected(conversation, user_id!!)
            }
        }

    }

    interface ConvoUsersAdapterListener {
        fun onUsersSelected(user: Conversation?, user_id: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.conversation_single_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        getSnapshot(position)?.let { snapshot ->
            holder.bind(snapshot, listener, holder)
        }
    }
}