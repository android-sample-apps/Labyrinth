package org.bandev.labyrinth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
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

        // Login user
        profile.login(this, 0)
        binding = ProfileKeysActBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(binding.content.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.content.toolbar.navigationIcon = backDrawable

        filldata()

        with(binding.pullToRefresh) {
            setColorSchemeColors(ContextCompat.getColor(this@ProfileKeysAct, R.color.colorPrimary))
            setOnRefreshListener {
                filldata()
                binding.pullToRefresh.isRefreshing = false
            }
        }
    }

    private fun filldata() {
        hideAll()
        val sshList: MutableList<String> = mutableListOf()
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
                    binding.content.sshListView.adapter = infoListAdapter
                    binding.content.sshListView.divider = null

                    AndroidNetworking
                        .get("https://gitlab.com/api/v4/user/gpg_keys")
                        .addQueryParameter("access_token", profile.getData("token"))
                        .build()
                        .getAsJSONArray(object : JSONArrayRequestListener {
                            override fun onResponse(response: JSONArray) {
                                var indexInt = 0
                                while (indexInt != response.length()) {
                                    gpgList.add(response[indexInt].toString())
                                    indexInt++
                                }

                                val gpgAdapter =
                                    GpgKeysItemAdapter(this@ProfileKeysAct, gpgList.toTypedArray())
                                binding.content.gpgListView.adapter = gpgAdapter
                                binding.content.gpgListView.divider = null

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
        with(binding.content) {
            refresher.isGone = true
            options.isGone = false
            options2.isGone = false
            gpg.isGone = false
            constraintLayout.isGone = false
        }
    }

    fun hideAll() {
        with(binding.content) {
            refresher.isGone = false
            options.isGone = true
            options2.isGone = true
            gpg.isGone = true
            constraintLayout.isGone = true
        }
    }
}