package com.inihood.android.whatsappclone

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val TAG = "FirebaseEmailPassword"
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email_sign_in_button.setOnClickListener {
            signIn(email_et.text.toString(), password_et.text.toString())
        }

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        if (mAuth!!.currentUser != null){
            val currentUser = mAuth!!.currentUser
            updateUI(currentUser)
        }
    }

    fun hideKeyboard() {
        val view: View? = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun signIn(email: String, password: String) {
        hideKeyboard()
        Log.e(TAG, "signIn:" + email)
        if (!validateForm(email, password)) {
            return
        }
        login_progress.visibility = View.VISIBLE
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.e(TAG, "signIn: Success!")
                    login_progress.visibility = View.GONE
                    // update UI with the signed-in user's information
                    val user = mAuth!!.getCurrentUser()
                    updateUI(user)
                } else {
                    login_progress.visibility = View.GONE
                    Log.e(TAG, "signIn: Fail!", task.exception)
                    Toast.makeText(applicationContext, "Authentication failed!", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

            }
    }



    private fun validateForm(email: String, password: String): Boolean {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(applicationContext, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
          //  tvStatus.text = "User Email: " + user.email + "(verified: " + user.isEmailVerified + ")"
          //  tvDetail.text = "Firebase User ID: " + user.uid
            Toast.makeText(this, "email:  ${user.email}", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "email:  ${user.uid}", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, ConversationListActivity::class.java))
            finish()
        }
    }

    fun regUser(view: View) {
        val intent = Intent(this, RegActivity::class.java)
        startActivity(intent)
    }

}