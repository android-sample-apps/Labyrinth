package org.bandev.labyrinth.intro

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.bandev.labyrinth.MainAct
import org.bandev.labyrinth.R
import org.json.JSONObject

class Second : AppCompatActivity() {

    var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_second)

        val button: Button = findViewById(R.id.button)
        val button2: Button = findViewById(R.id.button2)
        val userData = Bundle()
        val title = findViewById<TextView>(R.id.title)
        val token = (intent.extras ?: return).get("token").toString()

        AndroidNetworking.initialize(this)
        AndroidNetworking.get("https://gitlab.com/api/v4/user?access_token=$token")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {


                    val avatar = findViewById<ImageView>(R.id.avatar)

                    avatar.load((response ?: return).getString("avatar_url")) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }

                    val usernameTextView: TextView = findViewById(R.id.usernmame)
                    val emailTextView: TextView = findViewById(R.id.email)

                    usernameTextView.text = response.getString("username")
                    emailTextView.text = response.getString("email")

                    title.text = "Hi " + response.getString("username")

                    userData.putString("token", token)
                    userData.putString("server", "https://gitlab.com")
                    userData.putString("username", response.getString("username"))
                    userData.putString("email", response.getString("email"))
                    userData.putString("bio", response.getString("bio"))
                    userData.putString("location", response.getString("location"))
                    userData.putInt("id", response.getInt("id"))
                    userData.putString("avatarUrl", response.getString("avatar_url"))
                    userData.putString("webUrl", response.getString("web_url"))
                }

                override fun onError(error: ANError?) {
                    finish()
                    Toast.makeText(applicationContext, "Wrong token", LENGTH_SHORT).show()
                }
            })

        button.setOnClickListener {

            val accountManager: AccountManager = AccountManager.get(this)
            username = userData.getString("username").toString()

            Account(username, "org.bandev.labyrinth.account").also { account ->
                accountManager.addAccountExplicitly(account, token, userData)
            }

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