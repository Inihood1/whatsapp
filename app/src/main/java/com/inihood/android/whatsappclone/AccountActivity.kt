package com.inihood.android.whatsappclone

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : App() {

    private var mAuth: FirebaseAuth? = null
    private var TAG: String = "AccountActivity"
    private var firestoreListener: ListenerRegistration? = null
    val RECORD_REQUEST_CODE = 100
    val IMAGE_PICK_CODE = 200
    private var filePath: Uri? = null
    private var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
       // val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        toolbar?.setTitle("My Account")

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("Users").document(mAuth!!.uid.toString())
        Log.d("IDENT", "ID: " + mAuth!!.currentUser!!.uid)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                name_tv.text = snapshot.get("name").toString()
                if (snapshot.get("image").toString() != "image"){
                    Glide.with(applicationContext)
                        .load(snapshot.get("image"))
                        .into(profile_image)
                }else{
                    profile_image.setImageResource(R.drawable.ic_person)
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
        profile_image.setOnClickListener {
            setupPermissions()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        //firestoreListener!!.remove()
    }
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            makeRequest()
        }else{
            openGallery()
        }
    }
    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            RECORD_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                             permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                    openGallery()
                }
            }
        }
    }

    private fun openGallery() {
      //  Toast.makeText(this, "Opening gallery", Toast.LENGTH_LONG).show()
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            // I'M GETTING THE URI OF THE IMAGE AS DATA AND SETTING IT TO THE IMAGEVIEW
            profile_image.setImageURI(data?.data)
            filePath = data?.data
           // val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
            uploadToDb(filePath)
        }
    }

    private fun uploadToDb(filePath: Uri?) {
        val ref = storageReference?.child("profile_pic/${mAuth?.uid}.jpg")
        val uploadTask = ref?.putFile(this.filePath!!)
        Toast.makeText(this, "Uploading...", Toast.LENGTH_LONG).show()
        val urlTask = uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val items = HashMap<String, Any>()
                items.put("image", downloadUri.toString())
                val db = FirebaseFirestore.getInstance()
                mAuth?.uid?.let { db.collection("Users").document(it).
                update(items).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Toast.makeText(this, "Updated", Toast.LENGTH_LONG).show()
                    }
                }
                }
            } else {
                // Handle failures
                // ...
            }
        }
    }
}