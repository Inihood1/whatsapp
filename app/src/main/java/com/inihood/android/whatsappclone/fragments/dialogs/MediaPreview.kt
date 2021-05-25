package com.inihood.android.whatsappclone.fragments.dialogs

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.inihood.android.whatsappclone.R
import kotlinx.android.synthetic.main.media_preview_layout.view.*

class MediaPreview : DialogFragment() {

    lateinit var listener: compressAndUpLoad

    companion object {
        const val TAG = "MediaPreview"
        private val KEY_IMAGE: String? = null
        private val KEY_DOC_INFO: String? = null
        private val DOC_URI: String? = null

        fun newInstance(imageUri: Uri?, fileInfo: String?, docUri: Uri?): MediaPreview {
            val args = Bundle()
            args.putString(KEY_IMAGE, imageUri.toString())
            args.putString(KEY_DOC_INFO, fileInfo)
            args.putString(DOC_URI, docUri.toString())
            val fragment = MediaPreview()
            fragment.arguments = args
            return fragment
        }

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.media_preview_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as compressAndUpLoad
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupView(view: View) {
        val image = arguments?.getString(KEY_IMAGE)
        val docInfo = arguments?.getString(KEY_DOC_INFO)
        val docUri = arguments?.getString(DOC_URI)
        if (image != null){
            view.viewimage.visibility = View.VISIBLE
            view.viewdoc.visibility = View.GONE
            view.file_prop.visibility = View.GONE
            val uri = Uri.parse(image)
            view.viewimage.setImageURI(uri)
            setupClickListeners(view, uri)
        } else if (docInfo != null){
            view.viewimage.visibility = View.GONE
            view.viewdoc.visibility = View.VISIBLE
            view.file_prop.visibility = View.VISIBLE
            view.file_prop.text = "$docInfo - 20mb"
            val uri = Uri.parse(docUri)
            setupClickListeners(view, uri)
        }

    }

    private fun setupClickListeners(view: View, uri: Uri) {
        view.fab.setOnClickListener {
            listener.uploadOrCompress(uri)
            dismiss()
        }
        view.btnNegative.setOnClickListener {
            // TODO: Do some task here
            dismiss()
        }
    }
    interface compressAndUpLoad {
        fun uploadOrCompress(uri: Uri)
    }
}
