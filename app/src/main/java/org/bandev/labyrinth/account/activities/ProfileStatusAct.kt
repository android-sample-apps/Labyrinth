package org.bandev.labyrinth.account.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import io.wax911.emojify.parser.EmojiParser
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.StatusListAdapter
import org.bandev.labyrinth.databinding.ProfileStatusActBinding
import org.json.JSONObject


class ProfileStatusAct : AppCompatActivity() {

    private var profile: Profile = Profile()
    private lateinit var binding: ProfileStatusActBinding
    var emoji = ""
    var message = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Login user
        profile.login(this, 0)
        binding = ProfileStatusActBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = backDrawable

        binding.pull.setColorSchemeColors(
            ContextCompat.getColor(
                this,
                R.color.colorPrimary
            )
        )
        binding.pull.setOnRefreshListener {
            updateList()
            binding.pull.isRefreshing = false
        }

        updateList()

/*        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            filldata()
            refresher.isRefreshing = false
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_status, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add ->
                startActivityForResult(
                    Intent(this, NewStatus::class.java)
                        .putExtra("emoji", ":$emoji:")
                        .putExtra("message", message), 0
                )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            0 -> {
                val emoji = (data ?: return).getStringExtra("emoji")
                val title = data.getStringExtra("title")
                var glEmoji = EmojiParser.parseToAliases(emoji.toString())
                glEmoji = glEmoji.removePrefix(":").removeSuffix(":")
                /*val sharedPreferences = getSharedPreferences("emoji", 0)
                val editor = sharedPreferences.edit()
                val emojis = sharedPreferences.getString("main", "@|")
                val newEmojis = "$emojis$glEmoji!$title!0|"
                editor.putString("main", newEmojis)
                editor.apply()*/
                val json = JSONObject()
                json.put("emoji", glEmoji)
                json.put("message", title)

                AndroidNetworking
                    .put("https://gitlab.com/api/v4/user/status")
                    .addHeaders("PRIVATE-TOKEN", profile.getData("token"))
                    .addHeaders("Content-Type", "application/json")
                    .addJSONObjectBody(json)
                    .build()
                    .getAsString(object : StringRequestListener {
                        override fun onResponse(response: String) {
                            Snackbar.make(binding.root, "Status updated", Snackbar.LENGTH_SHORT)
                                .show()
                            updateList()
                        }

                        override fun onError(error: ANError) {
                            // handle error
                        }
                    })

            }
            // Other result codes
            else -> {
            }
        }
    }

    internal fun updateList() {
        binding.error.visibility = View.GONE
        hideAll()
        AndroidNetworking
            .get("https://gitlab.com/api/v4/user/status")
            .addQueryParameter("access_token", profile.getData("token"))
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {


                    val sharedPreferences = getSharedPreferences("emoji", 0)

                    val emojis = sharedPreferences.getString("main", "@|")?.replace(
                        "@",
                        ":" + response.getString("emoji") + ":!" + response.getString("message") + "!1|"
                    )

                    emoji = response.getString("emoji")
                    message = response.getString("message")

                    if (emoji == "null") {
                        error(1)
                    }

                    val emojiList = emojis?.split("|")?.toMutableList()
                    emojiList?.removeAll(listOf(""))
                    val list = mutableListOf<String>()
                    list.add(emojiList?.get(0).toString())
                    val infoListAdapter = StatusListAdapter(
                        this@ProfileStatusAct,
                        list.toTypedArray()
                    )
                    binding.currentStatus.adapter = infoListAdapter
                    showAll()
                }

                override fun onError(error: ANError) {
                    // handle error
                    error(0)
                }
            })

    }

    fun error(type: Int) {
        binding.error.visibility = View.VISIBLE
        binding.refresher.visibility = View.INVISIBLE
        val statusDrawable = IconicsDrawable(this, Octicons.Icon.oct_hubot).apply { sizeDp = 24 }
        val errorDrawable =
            IconicsDrawable(this, Octicons.Icon.oct_x_circle_fill).apply { sizeDp = 24 }
        when (type) {
            1 -> {
                binding.icon.setImageDrawable(statusDrawable)
                binding.title.text = "No status"
                binding.description.text = "You have no status on your account"
            }
            else -> {
                binding.icon.setImageDrawable(errorDrawable)
                binding.title.text = "Something went wrong"
                binding.description.text = "Try again or check your internet connection"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun showAll() {
        binding.refresher.isGone = true
        binding.options.isGone = false
    }

    fun hideAll() {
        binding.refresher.isGone = false
        binding.options.isGone = true
    }
}