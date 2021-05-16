package org.bandev.labyrinth.intro

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import org.bandev.labyrinth.R
import org.bandev.labyrinth.activities.HomeActivity
import org.bandev.labyrinth.databinding.ActivityOpeningTwoBinding
import org.json.JSONObject

class Second : AppCompatActivity() {

    private lateinit var binding: ActivityOpeningTwoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Load the view - R.layout.activity_opening_two
        binding = ActivityOpeningTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Get the response & token passed in from the first activity
        val response = JSONObject(intent.extras?.getString("response").toString())
        val token = intent.extras?.getString("token").toString()

        //Build a userData bundle for android to store
        val userData = Bundle()
        userData.putString("token", token)
        userData.putString("server", "https://gitlab.com")
        userData.putString("username", response.getString("username"))
        userData.putString("email", response.getString("email"))
        userData.putString("bio", response.getString("bio"))
        userData.putString("location", response.getString("location"))
        userData.putString("id", response.getInt("id").toString())
        userData.putString("avatarUrl", response.getString("avatar_url"))
        userData.putString("webUrl", response.getString("web_url"))

        //Load the data into the view
        with(binding) {
            //Load the circle labyrinth logo
            logo.load(R.drawable.labyrinth_logo) {
                transformations(CircleCropTransformation())
            }

            //Load the user's pfp
            avatar.load(response.getString("avatar_url")) {
                transformations(CircleCropTransformation())
            }

            //Set view text
            ("Hello " + response.getString("username")).also { binding.title.text = it }
        }

        //On continue save the account
        binding.next.setOnClickListener { openApp(userData) }
    }

    private fun openApp(userData: Bundle) {
        val accountManager: AccountManager = AccountManager.get(this)
        val username = userData.getString("username").toString()
        val token = userData.getString("token").toString()

        //Create new account
        Account(username, "org.bandev.labyrinth.account").also { account ->
            accountManager.addAccountExplicitly(account, token, userData)
        }

        //Open main activity
        val intent = Intent(this, HomeActivity::class.java)
        this.startActivity(intent)
        finish()
    }
}