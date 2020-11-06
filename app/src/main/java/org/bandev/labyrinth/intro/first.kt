package org.bandev.labyrinth.intro

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.bandev.labyrinth.R
import org.bandev.labyrinth.core.api
import org.bandev.labyrinth.mainAct
import org.json.JSONObject

class first : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_first)

        val button: Button = findViewById(R.id.button)

        val button2: Button = findViewById(R.id.button2)

        button.setOnClickListener {
            var name = findViewById<EditText>(R.id.name)

            val pref = getSharedPreferences("Settings", 0)
            val editor = pref.edit()
            editor.putString("token", name.text.toString())
            editor.apply()

            var token = name.text.toString()

            AndroidNetworking.initialize(this)
            AndroidNetworking.get("https://gitlab.com/api/v4/user?access_token=$token")
                .build()
                .getAsJSONObject(object: JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?){
                        val pref = getSharedPreferences("User", 0)
                        val editor = pref.edit()
                        editor.putString("token", name.text.toString())
                        editor.putString("username", response!!.getString("username"))
                        editor.putString("email", response!!.getString("email"))
                        editor.putString("bio", response!!.getString("bio"))
                        editor.putString("location", response!!.getString("location"))
                        editor.putInt("id", response!!.getInt("id"))
                        editor.putString("avatarUrl", response.getString("avatar_url"))
                        editor.putString("webUrl", response.getString("web_url"))
                        editor.apply()
                    }

                    override fun onError(error: ANError?) {
                        // handle error
                    }

                })

            api().getUserGroups(this, token)
            api().getUserProjects(this, token)

            val intent = Intent(this, mainAct::class.java)
            this.startActivity(intent)

        }

        val pref = getSharedPreferences("Settings", 0)
        pref.getString("token", "null")

        if(pref.getString("token", "null") != "null"){
            val intent = Intent(this, mainAct::class.java)
            this.startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this, mainAct::class.java)
            this.startActivity(intent)

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