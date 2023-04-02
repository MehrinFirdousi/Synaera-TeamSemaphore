package com.example.synaera

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class SharedPref private constructor() {

    fun setIsFirstLaunchToFalse() {
        editor!!.putBoolean(IS_FIRST_LAUNCH, false)
        editor!!.commit()
    }

    fun isFirstLaunch() : Boolean {
        return sharedPreferences!!.getBoolean(IS_FIRST_LAUNCH, true)
    }

    companion object {
        private val sharedPref = SharedPref()
        private var sharedPreferences: SharedPreferences? = null
        private var editor: SharedPreferences.Editor? = null
        private val IS_FIRST_LAUNCH = "is_first_launch"

        @Synchronized
        fun getInstance(context: Context): SharedPref {

            if (sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
                editor = sharedPreferences!!.edit()
            }

            return sharedPref
        }
    }
}