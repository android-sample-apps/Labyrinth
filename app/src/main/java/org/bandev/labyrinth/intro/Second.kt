package org.bandev.labyrinth.intro

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.MainAct
import org.bandev.labyrinth.R
import org.bandev.labyrinth.RoundedTransform

class Second : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_second)

        val button: Button = findViewById(R.id.button)
        val button2: Button = findViewById(R.id.button2)

        val title = findViewById<TextView>(R.id.title)

        val pref = getSharedPreferences("User", 0)
        val avatar = findViewById<ImageView>(R.id.avatar)
        Picasso.get().load(pref?.getString("avatarUrl", "null")).transform(RoundedTransform(30, 0))
            .into(avatar)

        val usernameTextView: TextView = findViewById(R.id.usernmame)
        val emailTextView: TextView = findViewById(R.id.email)

        usernameTextView.text = pref.getString("username", "null")
        emailTextView.text = pref.getString("email", "null")

        title.text = "Hi " + pref.getString("username", "null")



        button.setOnClickListener {

            val intent = Intent(this, MainAct::class.java)
            this.startActivity(intent)

        }

        button2.setOnClickListener {

            finish()

        }


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