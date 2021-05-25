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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_reg.*
import kotlinx.android.synthetic.main.activity_reg.email_et
import kotlinx.android.synthetic.main.activity_reg.password_et

class RegActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private val TAG = "FirebaseEmailPassword"
    private var firestore: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

    }

    fun register(view: View) {
        createAccount(email_et.text.toString(), password_et.text.toString(), name_et.text.toString())
    }

    fun login(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
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

    fun hideKeyboard() {
        val view: View? = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun createAccount(email: String, password: String, name: String) {
        hideKeyboard()
        Log.e(TAG, "createAccount:" + email)
        if (!validateForm(email, password)) {
            return
        }
        reg_progress.visibility = View.VISIBLE
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.e(TAG, "createAccount: Success!")
                    val user = mAuth!!.currentUser
                    // add data to firestore


                    val items = HashMap<String, String>()
                    items.put("name", name)
                    items.put("image", "image")
                   // if (user != null) {
                    firestore?.collection("Users")?.document(user!!.uid)?.set(items)
                        ?.addOnCompleteListener{ task ->
                            reg_progress.visibility = View.GONE
                            if (task.isSuccessful){
                                Toast.makeText(this, "Account created", Toast.LENGTH_LONG).show()
                                updateUI(user)
                            }
                        }?.addOnFailureListener { fail ->
                            reg_progress.visibility = View.GONE
                            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                        }
                 //   }

                    // update UI with the signed-in user's information
                } else {
                    reg_progress.visibility = View.GONE
                    Log.e(TAG, "createAccount: Fail!", task.exception)
                    Toast.makeText(applicationContext, "Authentication failed!", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            //  tvStatus.text = "User Email: " + user.email + "(verified: " + user.isEmailVerified + ")"
            //  tvDetail.text = "Firebase User ID: " + user.uid
            Toast.makeText(this, "email:  ${user.email}", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "id:  ${user.uid}", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, ConversationListActivity::class.java))
            finish()
        }
    }
}