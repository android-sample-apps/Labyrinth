package org.bandev.labyrinth.core

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle


class User {

    fun getUsername(context: Context): String{
        val pref = context.getSharedPreferences("User", 0)
        return pref.getString("username", "null").toString()
    }

    fun getToken(context: Context): String {
        val pref = context.getSharedPreferences("User", 0)
        return pref.getString("token", "null").toString()
    }



}