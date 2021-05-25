package com.inihood.android.whatsappclone

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import com.inihood.android.whatsappclone.adapter.media.MyAdapter
import kotlinx.android.synthetic.main.activity_media.*

class MediaActivity : AppCompatActivity() {

    private val requestCodePicker = 100
    private lateinit var myAdapter: MyAdapter
    private lateinit var options: Options
    private var returnValue = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)


        recyclerView.layoutManager = StaggeredGridLayoutManager(2,
            StaggeredGridLayoutManager.VERTICAL)
        myAdapter = MyAdapter(this)

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
        recyclerView.adapter = myAdapter

        fab.setOnClickListener {
            options.preSelectedUrls = returnValue
            Pix.start(this@MediaActivity, options)
        }

    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            requestCodePicker -> {
                if (resultCode == Activity.RESULT_OK) {
                    returnValue = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)!!
                    myAdapter.addImage(returnValue)
                }
            }
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
}