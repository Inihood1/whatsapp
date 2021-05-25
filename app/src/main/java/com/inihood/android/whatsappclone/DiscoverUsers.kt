package com.inihood.android.whatsappclone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inihood.android.whatsappclone.adapter.users.UsersAdapter
import com.inihood.android.whatsappclone.model.User
import com.inihood.android.whatsappclone.utils.Constants

class DiscoverUsers : App(), UsersAdapter.UsersAdapterListener {

    private var mAuth: FirebaseAuth? = null
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_users)

        mAuth = FirebaseAuth.getInstance()

        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        // toolbar?.title = "Users" // using property access syntax
        toolbar?.setTitle(Constants.users) // using setter method
        val query: Query = FirebaseFirestore.getInstance().collection(Constants.users)
        val recyclerView: RecyclerView = findViewById(R.id.users_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UsersAdapter(query, this)
        recyclerView.adapter = adapter
    }
//    private fun signOut() {
//        mAuth!!.signOut()
//        startActivity(Intent(this, LoginActivity::class.java))
//        finish()
//    }

    override fun onUsersSelected(user: User?, user_id: String) {
        if (user_id != mAuth?.uid){
            val intent = Intent(applicationContext, ChatActivity::class.java)
            intent.putExtra(Constants.intentUserId, user_id)
            startActivity(intent)
        }
    }
    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }
}