package com.adilmulimani.todoApp.utility

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class MySharedPreference(context: Context)
{

    private val preference: String = "user_todo"

    private var appShared : SharedPreferences = context.getSharedPreferences(preference, Activity.MODE_PRIVATE)
    private var  preferenceEditor :SharedPreferences.Editor = appShared.edit()

    //To get and set Integer Data
    fun getIntValue(key:String): Int {
        return appShared.getInt(key,0)
    }

    fun setIntValue(key:String,value:Int)
    {
        preferenceEditor.putInt(key,value).apply()
    }

    //To get and set String data
    fun getStringValue(key:String): String? {
        return appShared.getString(key,"")
    }

    fun setStringValue(key:String,value:String)
    {
        preferenceEditor.putString(key,value).apply()
    }

    //To get and set Boolean data
    fun getBooleanValue(key:String): Boolean {
        return appShared.getBoolean(key,false)
    }

    fun setBooleanValue(key:String,value:Boolean)
    {
        preferenceEditor.putBoolean(key,value).apply()
    }

    fun clearUserData()
    {
        preferenceEditor.clear().apply()
    }

}