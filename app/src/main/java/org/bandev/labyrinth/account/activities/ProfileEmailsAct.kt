package org.bandev.labyrinth.account.activities

import android.os.Bundle
import android.view.View
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
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.EmailListAdapter
import org.bandev.labyrinth.databinding.ProfileEmailsActBinding
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
        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = backDrawable

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
        val emailDrawable = IconicsDrawable(this, Octicons.Icon.oct_mail).apply { sizeDp = 24 }
        val errorDrawable =
            IconicsDrawable(this, Octicons.Icon.oct_x_circle_fill).apply { sizeDp = 24 }
        when (type) {
            1 -> {
                binding.icon.setImageDrawable(emailDrawable)
                binding.title.text = "No emails"
                binding.description.text = "You have no emails on your account"
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
        binding.infoList.isGone = false
    }

    fun hideAll() {
        binding.refresher.isGone = false
        binding.infoList.isGone = true
    }
}