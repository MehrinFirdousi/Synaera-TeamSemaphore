package com.example.synaera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream


open class VideoItem(var title : String, var status : String, var transcript : String) {
    lateinit var bitmap: Bitmap
    var deleteMode : Boolean = false

    constructor(
        title: String,
        status: String,
        bitmapString: String,
        transcript: String,
        deleteModeInt: Int
    ) : this(title, status, transcript) {
        this.deleteMode = (deleteModeInt == 1)
        val byteArray1: ByteArray = Base64.decode(bitmapString, Base64.DEFAULT)
        bitmap = BitmapFactory.decodeByteArray(
            byteArray1, 0,
            byteArray1.size
        )
    }

    constructor(
        title: String,
        status: String,
        bitmap: Bitmap,
        transcript: String,
        deleteMode: Boolean
    ) : this(title, status, transcript) {
        this.bitmap = bitmap
        this.deleteMode = deleteMode
    }

    open fun convertBitmapToString(): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    open fun deleteModeToInt() : Int{
        return if (deleteMode) 1
        else 0
    }
}