package com.inihood.android.whatsappclone.adapter.users

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.inihood.android.whatsappclone.R
import com.inihood.android.whatsappclone.model.User
import de.hdodenhof.circleimageview.CircleImageView

class UsersAdapter(
    query: Query,
    private val listener: UsersAdapterListener
) : FirestoreAdapter<UsersAdapter.UsersViewHolder>(query) {

    class UsersViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardView: CardView = itemView.findViewById(R.id.user_card)
        private val name: TextView = itemView.findViewById(R.id.user_name)
        private var user_id: String? = null
        private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        private val profileImage: CircleImageView = itemView.findViewById(R.id.user_image)

        fun bind(
            snapshot: DocumentSnapshot,
            listener: UsersAdapterListener,
            holder: UsersViewHolder
        ) {
            val user: User? = snapshot.toObject(User::class.java)
            user_id = snapshot.id.toString()
            if (user_id != mAuth.currentUser?.uid){
                name.text = user?.name
                user?.name?.let { Log.d("Get_data", it)}
                user?.image?.let { Log.d("Get_data", it) }
                Glide.with(holder.itemView.getContext())
                        .load(user?.image)
                        .placeholder(R.drawable.ic_person)
                        .thumbnail(0.5f)
                        .into(profileImage)
            }

            cardView.setOnClickListener {
                listener.onUsersSelected(user, user_id!!)
            }
        }
    }

    interface UsersAdapterListener {
        fun onUsersSelected(user: User?, user_id: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.user_single_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        getSnapshot(position)?.let { snapshot ->
            holder.bind(snapshot, listener, holder)
        }
    }

}