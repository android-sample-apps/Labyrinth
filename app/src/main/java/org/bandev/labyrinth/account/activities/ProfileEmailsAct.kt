package org.bandev.labyrinth.account.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.EmailListAdapter
import org.bandev.labyrinth.adapters.GpgKeysItemAdapter
import org.bandev.labyrinth.adapters.SshKeysItemAdapter
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.databinding.ProfileEmailsActBinding
import org.bandev.labyrinth.databinding.ProfileKeysActBinding
import org.json.JSONArray


class ProfileEmailsAct : AppCompatActivity() {

    private var profile: Profile = Profile()
    private lateinit var binding: ProfileEmailsActBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Login user
        profile.login(this, 0)
        binding = ProfileEmailsActBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        filldata()

        binding.pull.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        binding.pull.setOnRefreshListener {
            filldata()
            binding.pull.isRefreshing = false
        }
    }

    private fun filldata() {
        binding.error.visibility = View.GONE
        hideAll()
        val emailList: MutableList<String> = mutableListOf()

        AndroidNetworking
            .get("https://gitlab.com/api/v4/user/emails")
            .addQueryParameter("access_token", profile.getData("token"))
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    var index = 0
                    while (index != response.length()) {
                        emailList.add(response[index].toString())
                        index++
                    }

                    if (index == 0) error(1)

                    val infoListAdapter =
                        EmailListAdapter(this@ProfileEmailsAct, emailList.toTypedArray())
                    binding.infoList.adapter = infoListAdapter
                    binding.infoList.divider = null
                    showAll()
                }

                override fun onError(error: ANError) {
                    error(0)
                }
            })
    }

    fun error(type: Int) {
        binding.error.visibility = View.VISIBLE
        binding.refresher.visibility = View.INVISIBLE
        when (type) {
            1 -> {
                binding.icon.setImageResource(R.drawable.ic_email)
                binding.title.text = "No emails"
                binding.description.text = "You have no emails on your account"
            }
            else -> {
                binding.icon.setImageResource(R.drawable.ic_error)
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
        binding.infoList.isGone = false
    }

    fun hideAll() {
        binding.refresher.isGone = false
        binding.infoList.isGone = true
    }
}