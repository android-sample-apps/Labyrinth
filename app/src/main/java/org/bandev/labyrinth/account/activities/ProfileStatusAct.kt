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
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.EmailListAdapter
import org.bandev.labyrinth.adapters.GpgKeysItemAdapter
import org.bandev.labyrinth.adapters.SshKeysItemAdapter
import org.bandev.labyrinth.adapters.StatusListAdapter
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.databinding.ProfileEmailsActBinding
import org.bandev.labyrinth.databinding.ProfileKeysActBinding
import org.bandev.labyrinth.databinding.ProfileStatusActBinding
import org.json.JSONArray
import org.json.JSONObject


class ProfileStatusAct : AppCompatActivity() {

    private var profile: Profile = Profile()
    private lateinit var binding: ProfileStatusActBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Login user
        profile.login(this, 0)
        binding = ProfileStatusActBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        Compatibility().edgeToEdge2(window, View(this), toolbar, resources)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(android.R.color.transparent)

        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        filldata()

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            val token = profile.getData("token")
            Api().getUserGroups(this, token)
            Api().getUserProjects(this, token)
            filldata()
            refresher.isRefreshing = false
        }
    }

    private fun filldata() {
        hideAll()
        val statusList: MutableList<String> = mutableListOf()

        AndroidNetworking
                .get("https://gitlab.com/api/v4/user/status")
                .addQueryParameter("access_token", profile.getData("token"))
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        statusList.add(response.toString())
                        val infoListAdapter = StatusListAdapter(this@ProfileStatusAct, statusList.toTypedArray())
                        binding.content.currentStatus.adapter = infoListAdapter
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

    fun showAll(){
        binding.content.refresher.isGone = true
        binding.content.options.isGone = false
        binding.content.constraintLayout.isGone = false
    }

    fun hideAll(){
        binding.content.refresher.isGone = false
        binding.content.options.isGone = true
        binding.content.constraintLayout.isGone = true
    }
}