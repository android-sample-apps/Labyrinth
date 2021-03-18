package org.bandev.labyrinth.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import okhttp3.*
import okio.IOException
import org.bandev.labyrinth.R
import org.bandev.labyrinth.databinding.ActivityOpeningBinding

/**
 * First activity the user sees
 */

class First : AppCompatActivity() {

    private lateinit var binding: ActivityOpeningBinding
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Load the view - R.layout.activity_opening
        binding = ActivityOpeningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Setup the handler
        handler = Handler(Looper.getMainLooper())

        //Load the logo with coil to get a circle crop
        binding.logo.load(R.drawable.labyrinth_logo) {
            transformations(CircleCropTransformation())
        }

        //Get the user's account
        binding.next.setOnClickListener { getUser() }
    }

    private fun getUser() {
        //Get the token given by the user
        val token = binding.twoInputInner.text.toString()

        //Build a request to GitLab
        val request = Request.Builder()
            .url("https://gitlab.com/api/v4/user")
            .addHeader("PRIVATE-TOKEN", token)
            .build()

        //Queue and await callback
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //Some error
                e.printStackTrace()
                handler.post { error("Something went wrong, try again") }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.code == 401) {
                        //If the access token was wrong
                        handler.post { error("Invalid Access Token") }
                    } else {
                        //Everything is ok, send the user to the welcome screen
                        val intent = Intent(this@First, Second::class.java)
                        intent.putExtra("response", response.body!!.string())
                        intent.putExtra("token", token)
                        startActivity(intent)
                    }
                }
            }
        })

    }

    internal fun error(message: String) {
        binding.twoInputOuter.error = message
    }
}