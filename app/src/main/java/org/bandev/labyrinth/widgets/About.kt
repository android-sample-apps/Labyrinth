package org.bandev.labyrinth.widgets

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import org.bandev.labyrinth.R

class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val versionElement = Element()
        versionElement.setTitle("Version 6.2")
        versionElement.setIconDrawable(R.mipmap.ic_launcher_round)

        val aboutPage = AboutPage(this)
            .isRTL(false)
            .enableDarkMode(false)
            .setImage(R.drawable.bandev)
            .setCustomFont(ResourcesCompat.getFont(this, R.font.jb))
            .addItem(versionElement)
            .addGroup("Connect with us")
            .addWebsite("https://bandev.computub.com")
            .addTwitter("BanDevApps")
            .addPlayStore("org.bandeb.labyrinth")
            .create()

        setContentView(aboutPage)
    }
}