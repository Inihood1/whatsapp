package com.inihood.android.whatsappclone.fileAttachment.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inihood.android.whatsappclone.fileAttachment.adapter.GridMenuAdapter
import com.inihood.android.whatsappclone.fileAttachment.widget.decoration.MenuGridDecoration

class MenuRecyclerView : RecyclerView {

    private val spanCount = 3

    private val manager = GridLayoutManager(context, spanCount)
    private val adapter = GridMenuAdapter()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        setHasFixedSize(true)
        layoutManager = manager
        setAdapter(adapter)
        addItemDecoration(MenuGridDecoration())
    }

    fun addMenuClickListener(listener: GridMenuAdapter.GridMenuListener) {
        adapter.listener = listener
    }

}