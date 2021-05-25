package com.inihood.android.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.inihood.android.whatsappclone.utils.Constants


open class App: AppCompatActivity() {


    fun initOnlineStatus(user_id: String){
        val firebaseFirestore: FirebaseFirestore?
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseFirestore.collection(Constants.users).
        document(user_id).update(Constants.onlineStatus, "true")
    }

    override fun onStart() {
        super.onStart()
        val authId = FirebaseAuth.getInstance()
        if (authId.currentUser != null){
            initOnlineStatus(authId.currentUser!!.uid)
           // startActivity(Intent(this, TestActivity::class.java))
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        val authId = FirebaseAuth.getInstance()
        if (authId.currentUser != null){
            removeOnlineStatus(authId.currentUser!!.uid)
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        val authId = FirebaseAuth.getInstance()
        if (authId.currentUser != null){
            removeOnlineStatus(authId.currentUser!!.uid)
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    fun removeOnlineStatus(user_id: String){
        val firebaseFirestore: FirebaseFirestore?
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseFirestore.collection(Constants.users).
        document(user_id).update(Constants.onlineStatus, Timestamp.now())

    }
}