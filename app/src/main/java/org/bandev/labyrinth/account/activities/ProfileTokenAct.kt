package org.bandev.labyrinth.account.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.TokenListAdapter
import org.bandev.labyrinth.core.obj.AccessToken
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
        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = backDrawable

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