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
import org.bandev.labyrinth.adapters.TokenListAdapter
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.core.obj.AccessToken
import org.bandev.labyrinth.databinding.ProfileEmailsActBinding
import org.bandev.labyrinth.databinding.ProfileKeysActBinding
import org.bandev.labyrinth.databinding.ProfileTokenActBinding
import org.json.JSONArray


class ProfileTokenAct : AppCompatActivity() {

    private var profile: Profile = Profile()
    private lateinit var binding: ProfileTokenActBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Login user
        profile.login(this, 0)
        binding = ProfileTokenActBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        filldata()

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            filldata()
            refresher.isRefreshing = false
        }
    }

    private fun filldata() {
        hideAll()
        val tokens: MutableList<AccessToken> = mutableListOf()

        AndroidNetworking
            .get("https://gitlab.com/api/v4/personal_access_tokens?per_page=100")
            .addQueryParameter("access_token", profile.getData("token"))
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    for (i in 0 until response.length()) {
                        val token = AccessToken(response.getJSONObject(i))
                        if (!token.revoked) {
                            tokens.add(token)
                        }
                    }
                    binding.content.tokens.adapter = TokenListAdapter(this@ProfileTokenAct, tokens)
                    binding.content.tokens.divider = null
                    showAll()
                }

                override fun onError(error: ANError) {
                    // handle error
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun showAll() {
        binding.content.refresher.isGone = true
        binding.content.tokens.isGone = false
    }

    fun hideAll() {
        binding.content.refresher.isGone = false
        binding.content.tokens.isGone = true
    }
}