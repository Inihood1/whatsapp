package com.inihood.android.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inihood.android.whatsappclone.adapter.conversation.ConversationUsersAdapter
import com.inihood.android.whatsappclone.adapter.users.UsersAdapter
import com.inihood.android.whatsappclone.model.Conversation
import com.inihood.android.whatsappclone.model.User
import com.inihood.android.whatsappclone.utils.Constants


class ConversationListActivity : App(), ConversationUsersAdapter.ConvoUsersAdapterListener {

    private var mAuth: FirebaseAuth? = null
    private lateinit var adapter: ConversationUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

       init()

    }

    private fun initList() {
//          code for getting last message
//        val queery: Query = FirebaseFirestore.getInstance().
//        collection(Constants.messages).
//        document(mAuth!!.currentUser!!.uid).collection(other_user_id!!).
//        orderBy("time", Query.Direction.DESCENDING).limit(1)

    }

    private fun init(){
        mAuth = FirebaseAuth.getInstance()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar?.setTitle("Chat List")

        if(mAuth?.currentUser != null) {
            val query: Query = FirebaseFirestore.getInstance().collection(Constants.users).document(mAuth?.currentUser?.uid!!)
                    .collection(Constants.chats)
                    .orderBy("time", Query.Direction.DESCENDING)
            val recyclerView: RecyclerView = findViewById(R.id.users_list)
            recyclerView.layoutManager = LinearLayoutManager(this)
            adapter = ConversationUsersAdapter(query, this)
            recyclerView.adapter = adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                signOut()
                true
            }
            R.id.action_edit_account -> {
                startActivity(Intent(this, AccountActivity::class.java))
                true
            }
            R.id.all_users -> {
                startActivity(Intent(this, DiscoverUsers::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun signOut() {
        mAuth!!.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onUsersSelected(user: Conversation?, user_id: String) {
        if (user_id != mAuth?.uid){
            val intent = Intent(applicationContext, ChatActivity::class.java)
            intent.putExtra(Constants.intentUserId, user_id)
            startActivity(intent)
        }
    }
    override fun onStop() {
        super.onStop()
        if(mAuth?.currentUser != null) {
            adapter.stopListening()
        }
    }

    override fun onStart() {
        super.onStart()
        if(mAuth?.currentUser != null){
            adapter.startListening()
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

}