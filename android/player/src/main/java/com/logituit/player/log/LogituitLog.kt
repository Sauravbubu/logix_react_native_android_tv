package com.logituit.player.log

import android.util.Log


/**
 * Created by Aditya.
 * Logituit
 * aditya.rout@logituit.com
 */
object LogituitLog {
    fun debugLog(tag: String, message: String) {
        Log.d(tag, message)
    }
    fun errorLog(tag: String, message: String) {
        Log.e(tag, message)
    }
    fun infoLog(tag: String, message: String) {
        Log.i(tag, "Test$message")
    }
}