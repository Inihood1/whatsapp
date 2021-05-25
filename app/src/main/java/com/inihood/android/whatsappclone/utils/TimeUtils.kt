package com.inihood.android.whatsappclone.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun getFormattedTimeEvent(time: Long?): String {
        val newFormat = SimpleDateFormat("h:mm a")
        return newFormat.format(time?.let { Date(it) })
    }

    fun getDate(): String{
//        val c = Calendar.getInstance()
//        val year = c.get(Calendar.YEAR)
//        val month = c.get(Calendar.MONTH)
//        val day = c.get(Calendar.DAY_OF_MONTH)
//        val hour = c.get(Calendar.HOUR_OF_DAY)
//        val minute = c.get(Calendar.MINUTE)

        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateInstance() //or use getDateInstance()
        val formatedDate = formatter.format(date)

        // Log.d("DATE", "onStart: $day $month $year")
        Log.d("DATE", "the date is: $formatedDate")
        return formatedDate
    }
}