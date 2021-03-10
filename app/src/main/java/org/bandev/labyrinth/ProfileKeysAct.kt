package org.bandev.labyrinth

import android.os.Bundle
import android.widget.ListView
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
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.GpgKeysItemAdapter
import org.bandev.labyrinth.adapters.SshKeysItemAdapter
import org.bandev.labyrinth.databinding.ProfileKeysActBinding
import org.json.JSONArray


class ProfileKeysAct : AppCompatActivity() {

    private var profile: Profile = Profile()
    private lateinit var binding: ProfileKeysActBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Login user
        profile.login(this, 0)
        binding = ProfileKeysActBinding.inflate(layoutInflater)
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
        val sshListView: ListView = findViewById(R.id.infoList)
        val sshList: MutableList<String> = mutableListOf()

        val gpgListView: ListView = findViewById(R.id.gpgList)
        val gpgList: MutableList<String> = mutableListOf()

        AndroidNetworking
            .get("https://gitlab.com/api/v4/user/keys")
            .addQueryParameter("access_token", profile.getData("token"))
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    var index = 0
                    while (index != response.length()) {
                        sshList.add(response[index].toString())
                        index++
                    }

                    val infoListAdapter =
                        SshKeysItemAdapter(this@ProfileKeysAct, sshList.toTypedArray())
                    sshListView.adapter = infoListAdapter
                    sshListView.divider = null

                    AndroidNetworking
                        .get("https://gitlab.com/api/v4/user/gpg_keys")
                        .addQueryParameter("access_token", profile.getData("token"))
                        .build()
                        .getAsJSONArray(object : JSONArrayRequestListener {
                            override fun onResponse(response: JSONArray) {
                                var index = 0
                                while (index != response.length()) {
                                    gpgList.add(response[index].toString())
                                    index++
                                }

                                val gpgAdapter =
                                    GpgKeysItemAdapter(this@ProfileKeysAct, gpgList.toTypedArray())
                                gpgListView.adapter = gpgAdapter
                                gpgListView.divider = null

                                showAll()
                            }

                            override fun onError(error: ANError) {
                                // handle error
                            }
                        })
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
        binding.content.options.isGone = false
        binding.content.options2.isGone = false
        binding.content.gpg.isGone = false
        binding.content.constraintLayout.isGone = false
    }

    fun hideAll() {
        binding.content.refresher.isGone = false
        binding.content.options.isGone = true
        binding.content.options2.isGone = true
        binding.content.gpg.isGone = true
        binding.content.constraintLayout.isGone = true
    }
}