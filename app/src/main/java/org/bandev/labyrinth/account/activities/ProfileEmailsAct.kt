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

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            filldata()
            refresher.isRefreshing = false
        }
    }

    private fun filldata() {
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
                        val infoListAdapter = EmailListAdapter(this@ProfileEmailsAct, emailList.toTypedArray())
                        binding.content.infoList.adapter = infoListAdapter
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