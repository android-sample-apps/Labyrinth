package org.bandev.labyrinth.core

import android.content.Context

class User {

    fun getUsername(context: Context): String {
        val pref = context.getSharedPreferences("User", 0)
        return pref.getString("username", "null").toString()
    }

    fun getToken(context: Context): String {
        val pref = context.getSharedPreferences("User", 0)
        return pref.getString("token", "null").toString()
    }


}