package com.inihood.android.whatsappclone

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import com.github.curioustechizen.ago.RelativeTimeTextView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.inihood.android.whatsappclone.adapter.chat.ChatAdapter
import com.inihood.android.whatsappclone.adapter.media.MyAdapter
import com.inihood.android.whatsappclone.emojicon.EmojiconGridView.OnEmojiconClickedListener
import com.inihood.android.whatsappclone.emojicon.EmojiconsPopup
import com.inihood.android.whatsappclone.emojicon.EmojiconsPopup.OnSoftKeyboardOpenCloseListener
import com.inihood.android.whatsappclone.fileAttachment.adapter.GridMenuAdapter
import com.inihood.android.whatsappclone.fileAttachment.widget.SoftKeyBoardPopup
import com.inihood.android.whatsappclone.fragments.dialogs.MediaPreview
import com.inihood.android.whatsappclone.model.MessageType
import com.inihood.android.whatsappclone.model.UserMessage
import com.inihood.android.whatsappclone.utils.ChatUtils
import com.inihood.android.whatsappclone.utils.Constants
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.activity_account.profile_image
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.recyclerView
import kotlinx.android.synthetic.main.activity_media.*
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatActivity : App(), GridMenuAdapter.MediaListener, MediaPreview.compressAndUpLoad{

    private var mAuth: FirebaseAuth? = null
    private var TAG: String = "ChatActivity"
    val firestore = FirebaseFirestore.getInstance()
    val chatMessages = ArrayList<UserMessage>()
    var chatRegistration: ListenerRegistration? = null
    var other_user_id: String? = null
    private var other_user_name: String? = null
    private var other_user_image: String? = null
    lateinit var menuKeyboard: SoftKeyBoardPopup
    private var recording: Boolean = true

    private val requestCodePicker = 100
    private val PICKFILE_RESULT_CODE = 200
    private lateinit var myAdapter: MyAdapter
    private lateinit var options: Options
    private var returnValue = ArrayList<String>()

    // media recorder
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

    //progress bar
    //var notificationID: Int? = 100

    // firebase storage
    private var filePath: Uri? = null
    private var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        init()
        other_user_id = intent.getStringExtra(Constants.intentUserId)
        mAuth = FirebaseAuth.getInstance()
        initFirebaseStuff(other_user_id)
        text_message.addTextChangedListener(contentWatcher)
        lyt_back.setOnClickListener { finish() }
      //  initAudioConfig()
        btn_send.setOnClickListener {
            if (recording){
                checkAudioPermission()
            }else{
                sendText(MessageType.TEXT)
            }
        }

        initList()
        initCamAndGallery()
        cam.setOnClickListener {
           openCam()
        }

        //setLastSeen()

//
//        val query: Query = FirebaseFirestore.getInstance()
//            .collection(Constants.messages).document(mAuth!!.currentUser!!.uid)
//            .collection(other_user_id!!).orderBy("time", Query.Direction.DESCENDING).limit(1)
//
//        query.addSnapshotListener { value, error ->
//            if (value != null) {
//                for (messageDocument in value.documents) {
//                    val sms = messageDocument["text"] as String
//                    Log.d("last", "the last message is: $sms")
//                }
//            }
//        }

    }

    private fun initAudioConfig() {
        output = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"
        mediaRecorder = MediaRecorder()

        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)
    }

    private fun recordAudio() {

    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, 0)
        }else{
            recordAudio()
        }
    }

    private fun openCam() {
        options.preSelectedUrls = returnValue
        Pix.start(this@ChatActivity, options)
    }

    private fun initCamAndGallery() {
        options = Options.init()
            .setRequestCode(requestCodePicker)
            .setCount(1)
            .setFrontfacing(false)
            .setPreSelectedUrls(returnValue)
            .setMode(Options.Mode.All)
            .setSpanCount(4)
            .setVideoDurationLimitinSeconds(50)
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
            .setPath("whatsapp/IniHood")
    }

    private fun setLastSeen() {
        val v = findViewById<View>(R.id.date) as RelativeTimeTextView
        val firebaseFirestore: FirebaseFirestore?
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseFirestore.collection(Constants.users).
        document(other_user_id!!).addSnapshotListener{ snapshot, e ->
           if (snapshot!!.exists()){
               if(snapshot.data?.get(Constants.onlineStatus) == "true"){
                   v.setText("Online")
               }else {
                   val time: Timestamp = snapshot.data?.get(Constants.onlineStatus) as Timestamp
                   val date: Date = time.toDate()
                   val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
                   try {
                       val mDate: Date = sdf.parse(date.toString())
                       val timeInMilliseconds = mDate.time
                       // println("Date in milli :: $timeInMilliseconds")
                       Log.d("App", "onStart: $timeInMilliseconds")
                       v.setReferenceTime(timeInMilliseconds)
                   } catch (e: ParseException) {
                       e.printStackTrace()
                   }
               }
           }
        }
    }

    private fun init() {
        val rootView = findViewById<View>(R.id.root_view)
        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        val popup = EmojiconsPopup(rootView, this)
        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard()
        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener {
            changeEmojiKeyboardIcon(emoji, R.drawable.ic_sentiment_satisfied) }
        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(object : OnSoftKeyboardOpenCloseListener {
            override fun onKeyboardOpen(keyBoardHeight: Int) {}
            override fun onKeyboardClose() {
                if (popup.isShowing) popup.dismiss()
            }
        })

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(OnEmojiconClickedListener { emojicon ->
            if (text_message == null || emojicon == null) {
                return@OnEmojiconClickedListener
            }
            val start: Int = text_message.getSelectionStart()
            val end: Int = text_message.getSelectionEnd()
            if (start < 0) {
                text_message.append(emojicon.emoji)
            } else {
                text_message.getText()?.replace(Math.min(start, end),
                        Math.max(start, end), emojicon.emoji, 0,
                        emojicon.emoji.length)
            }
        })

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener {
            val event = KeyEvent(
                    0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL)
            text_message.dispatchKeyEvent(event)
        }


        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emoji.setOnClickListener(View.OnClickListener {
            //If popup is not showing => emoji keyboard is not visible, we need to show it
            if (!popup.isShowing) {

                //If keyboard is visible, simply show the emoji popup
                if (popup.isKeyBoardOpen) {
                    popup.showAtBottom()
                    changeEmojiKeyboardIcon(emoji, R.drawable.ic_action_keyboard)
                } else {
                    text_message.setFocusableInTouchMode(true)
                    text_message.requestFocus()
                    popup.showAtBottomPending()
                    val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(text_message, InputMethodManager.SHOW_IMPLICIT)
                    changeEmojiKeyboardIcon(emoji, R.drawable.ic_action_keyboard)
                }
            } else {
                popup.dismiss()
            }
        })

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar?.setTitle("")

        menuKeyboard = SoftKeyBoardPopup(
                this,
                root_view,
                text_message,
                text_message,
                containeer
        )

        attach.setOnClickListener { toggle() }

    }

    private fun changeEmojiKeyboardIcon(iconToBeChanged: ImageView, drawableResourceId: Int) {
        iconToBeChanged.setImageResource(drawableResourceId)
    }

    fun sendText(smsType: String) {
       // Toast.makeText(applicationContext, imagePath, Toast.LENGTH_LONG).show()
        val msg: String = text_message.getText().toString()
        text_message.setText("")
        if (msg.isEmpty()) return
       // Toast.makeText(applicationContext, "imagePath", Toast.LENGTH_LONG).show()
        ChatUtils.processMessage(mAuth!!.currentUser!!.uid, other_user_id!!,
                msg, other_user_image, other_user_name, smsType, "imagePath")
    }


    fun sendImage(smsType: String, imagePath: String) {
        // Toast.makeText(applicationContext, imagePath, Toast.LENGTH_LONG).show()
        val msg =  "sms"
//        text_message.setText("")
//        if (msg.isEmpty()) return
        Log.d("MEDIATAG", "ImageUri: $imagePath")
       // Toast.makeText(applicationContext, imagePath, Toast.LENGTH_LONG).show()
        ChatUtils.processMessage(mAuth!!.currentUser!!.uid, other_user_id!!,
                msg, other_user_image, other_user_name, smsType, imagePath)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("TAG", "getFile: onActivityResult")
        when (requestCode) {
            requestCodePicker -> {
                Log.d("MEDIATAG", "getImage")
                if (resultCode == Activity.RESULT_OK) {
                    getImage(data)
                    //  myAdapter.addImage(returnValue)
                }
            }
            PICKFILE_RESULT_CODE -> {
                Log.d("MEDIATAG", "getFile: code")
                if (resultCode == Activity.RESULT_OK) {
                    getFile(data)
                }
            }
        }
    }

    private fun getImage(data: Intent?) {
        returnValue = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)!!
        val file = File(returnValue[0])
        val uri = Uri.fromFile(file)
        val fileName = File(uri.path).name
        Log.d("MEDIATAG", "getFileName: $fileName")
//        MediaPreview.newInstance(uri,
//                null, null).show(supportFragmentManager, MediaPreview.TAG)

        uploadToDb(uri, fileName)
    }


    private fun uploadToDb(uri: Uri?, fileName: String) {
        val randomID = UUID.randomUUID()
        val ref = storageReference?.child("chat_images/$fileName")
        val uploadTask = ref?.putFile(uri!!)
      //  Toast.makeText(this, "Uploading...", Toast.LENGTH_LONG).show()
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

                // add to chat
                sendImage(MessageType.IMAGE, downloadUri.toString())
            }
        }
        uploadTask?.addOnProgressListener {
            val progress: Long = 100 * it.bytesTransferred / it.totalByteCount
            Log.d("MEDIATAG", "uploadImage: $progress")
            Toast.makeText(applicationContext, "$progress", Toast.LENGTH_LONG).show()
           // println("Upload is $progress% done")
           // val currentprogress = progress.toInt()
           // progressBar.setProgress(currentprogress)
            //FileUploadNotification().updateNotification(progress, "filename", "content")
            //updateNotification("$progress", "file", "desc", applicationContext)

        }
    }

    private fun getFile(data: Intent?) {
        val uri: Uri = data?.data!!
        val src = uri.path
        val file = File(src!!).length()
       // Log.d("TAG", "FileLength: $file")
        data.data?.let { returnUri ->
            contentResolver.query(returnUri, null, null, null, null)
        }?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            Log.d("TAG", "getFileName: ${cursor.getString(nameIndex)}")
            Log.d("TAG", "getFileSize: ${cursor.getLong(sizeIndex)}")
            //Log.d("TAG", "getFileSize: ${FileUtils.readableFileSize(sizeIndex)}")
            val a = android.text.format.Formatter.formatShortFileSize(this, sizeIndex.toLong());
            Log.d("TAG", "getFileSize: $a")

//            MediaPreview.newInstance(null,
//                    cursor.getString(nameIndex), uri).show(supportFragmentManager, MediaPreview.TAG)

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(this, options)
                } else {
                    Toast.makeText(this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun initFirebaseStuff(id: String?) {
        val db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        val docRef = db.collection(Constants.users).document(id!!)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            user_name.text = snapshot?.get(Constants.name).toString()
            other_user_name = snapshot?.get(Constants.name).toString()
            other_user_image = snapshot?.get(Constants.image).toString()
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                if (snapshot.get(Constants.image).toString() != Constants.image) {
                    Glide.with(this)
                            .load(snapshot.get(Constants.image))
                            .into(profile_image)
                } else {
                    profile_image.setImageResource(R.drawable.ic_person)
                }
            } else {
                Log.d(TAG, "Current data: null")
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.chat, menu)
        return true
    }

    fun hideKeyboard() {
        val view: View? = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    val contentWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(etd: Editable) {
            if (etd.toString().trim { it <= ' ' }.length == 0) {
                recording = true
                btn_send.setImageResource(R.drawable.ic_mic)
            } else {
                recording = false
                btn_send.setImageResource(R.drawable.ic_send)
            }
        }

        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
    }

//    override fun onDestroy() {
//        chatRegistration?.remove()
//        super.onDestroy()
//    }


    fun initList() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        listenForChatMessages()
    }

    private fun listenForChatMessages() {

        val query: Query = FirebaseFirestore.getInstance()
                .collection(Constants.messages).document(mAuth!!.currentUser!!.uid)
                .collection(other_user_id!!).orderBy("time", Query.Direction.ASCENDING)

        query.addSnapshotListener { value, _ ->
            if (value == null || value.isEmpty)
                return@addSnapshotListener
            chatMessages.clear()
            for (messageDocument in value.documents) {
                chatMessages.add(
                        UserMessage(
                                messageDocument["text"] as String,
                                messageDocument["time"] as Timestamp,
                                messageDocument["senderId"] as String,
                                messageDocument["imagePath"] as String,
                                messageDocument["recipientId"] as String,
                                messageDocument["type"] as String,
                                messageDocument["key"] as String,
                                messageDocument["sms_date"] as String,
                                messageDocument["sms_section_date"] as String
                        ))
            }
            val adapter = ChatAdapter(chatMessages)
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(adapter.itemCount - 1)
        }

    }

    private fun toggle() {
        if (menuKeyboard.isShowing) {
            menuKeyboard.dismiss()
        } else {
            menuKeyboard.show()
        }
    }

    override fun onDestroy() {
        menuKeyboard.clear()
        super.onDestroy()
    }

    override fun onMedia() {
       openCam()
       // Log.d("MediaSelectedFor", "onMedia: ")
    }

    override fun onLocation() {

    }

    override fun onContact() {

    }

    override fun onDocument() {
        pickAFile()
    }

    override fun onAudio() {
        pickAFile()
    }

    override fun uploadOrCompress(uri: Uri) {
        Log.d("MediaSelected", "onMedia: $uri")
    }

    private fun pickAFile(){
        var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.type = "*/*"
        chooseFile = Intent.createChooser(chooseFile, "Choose a file")
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE)
    }

}
