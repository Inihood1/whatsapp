package com.inihood.android.whatsappclone.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
class Conversation : Parcelable {

    var user_id: String? = null
   // var time: Timestamp? = null

    constructor()

    private constructor(parcel: Parcel) {
        user_id = parcel.readString()
      //  image = parcel.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(user_id)
       // dest?.writeString(image)
    }

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel): Conversation {
            return Conversation(parcel)
        }

        override fun newArray(size: Int): Array<Conversation?> {
            return arrayOfNulls(size)
        }
    }
}