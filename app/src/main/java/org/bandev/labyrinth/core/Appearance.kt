package org.bandev.labyrinth.core

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class Appearance {

    fun setAppTheme(theme: String) {
        when (theme) {
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            "sys" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    fun getAppTheme(context: Context?): String {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val theme = sharedPrefs.getString("theme", "sys").toString()
        return theme
    }

}