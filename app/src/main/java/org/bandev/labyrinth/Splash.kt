package org.bandev.labyrinth

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import org.bandev.labyrinth.intro.First


class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = getSharedPreferences("Settings", 0)

        if (pref.getString("token", "null") != "null") {
            val i = Intent(this, MainAct::class.java)
            val mBundle = Bundle()
            i.putExtras(mBundle)
            startActivity(i)
            finish()
        }else{
            val i = Intent(this, First::class.java)
            val mBundle = Bundle()
            i.putExtras(mBundle)
            startActivity(i)
            finish()
        }

    }
}
