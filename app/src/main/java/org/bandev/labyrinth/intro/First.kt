package org.bandev.labyrinth.intro

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import org.bandev.labyrinth.R
import org.bandev.labyrinth.core.Api

class First : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_intro_first)

        val button: Button = findViewById(R.id.button)

        button.setOnClickListener {
            val name = findViewById<EditText>(R.id.name)

            val pref = getSharedPreferences("Settings", 0)
            val editor = pref.edit()
            editor.putString("token", name.text.toString())
            editor.apply()

            val token = name.text.toString()

            val intent = Intent(this, Second::class.java)
            intent.putExtra("token", token)
            this.startActivity(intent)


        }


    }

    fun moveOn() {
        val pref = getSharedPreferences("User", 0)
        val intent = Intent(this, Second::class.java)
        this.startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}