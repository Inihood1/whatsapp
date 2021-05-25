package com.inihood.android.whatsappclone.fileAttachment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.inihood.android.whatsappclone.R
import com.inihood.android.whatsappclone.fragments.dialogs.MediaPreview
import kotlinx.android.synthetic.main.item_menu.view.*

class GridMenuAdapter : RecyclerView.Adapter<GridMenuAdapter.MenuViewHolder>() {

    var listener: GridMenuListener? = null
    var mediaListener: MediaListener? = null

    private val menus = arrayListOf(
        Menu(
            "Document",
            R.drawable.attach_document
        ),
        Menu(
            "Camera",
            R.drawable.attach_camera
        ),
        Menu(
            "Gallery",
            R.drawable.attach_gallery
        ),
        Menu(
            "Audio",
            R.drawable.attach_audio
        ),
        Menu(
            "Location",
            R.drawable.attach_location
        ),
        Menu(
            "Contact",
            R.drawable.attach_contact
        )
    )

    interface GridMenuListener {
        fun dismissPopup()
    }

    private val data = ArrayList<Menu>().apply {
        addAll(menus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        mediaListener = parent.context as MediaListener
        return MenuViewHolder.create(
                parent,
                viewType
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(data[position], listener, mediaListener)
    }

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            menu: Menu,
            listener: GridMenuListener?,
            mediaListener: MediaListener?
        ) {
            with(itemView) {
                tvTitle.text = menu.name
                ivIcon.setImageDrawable(ContextCompat.getDrawable(context, menu.drawable))
                itemView.setOnClickListener {
                    when (menu.name) {
                        "Document" -> {
                            listener?.dismissPopup()
                            mediaListener?.onDocument()
                        }
                        "Contact" -> {
                            listener?.dismissPopup()
                            mediaListener?.onContact()
                        }
                        "Camera" -> {
                            listener?.dismissPopup()
                            mediaListener?.onMedia()
                        }
                        "Location" -> {
                            listener?.dismissPopup()
                            mediaListener?.onLocation()
                        }
                        "Audio" -> {
                            listener?.dismissPopup()
                            mediaListener?.onAudio()
                        }
                        "Gallery" -> {
                            listener?.dismissPopup()
                            mediaListener?.onMedia()
                        }
                    }
//                    Toast.makeText(it.context, "Menu ${menu.name} clicked", Toast.LENGTH_SHORT)
//                        .show()
                }
            }
        }

        companion object {
            val LAYOUT = R.layout.item_menu

            fun create(parent: ViewGroup, viewType: Int): MenuViewHolder {
                return MenuViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        LAYOUT,
                        parent,
                        false
                    )
                )
            }
        }
    }

    data class Menu(val name: String, @DrawableRes val drawable: Int) {

    }

    interface MediaListener {
        fun onMedia()
        fun onLocation()
        fun onContact()
        fun onDocument()
        fun onAudio()
    }
}