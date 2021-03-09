package org.bandev.labyrinth.account.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import org.bandev.labyrinth.GroupsAct
import org.bandev.labyrinth.ProjectAct
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.*
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.databinding.ProfileEmailsActBinding
import org.bandev.labyrinth.databinding.ProfileGroupsActBinding
import org.bandev.labyrinth.databinding.ProfileKeysActBinding
import org.bandev.labyrinth.databinding.ProfileTokenActBinding
import org.json.JSONArray
import org.json.JSONObject


class ProfileGroupsAct : AppCompatActivity() {

    private var profile: Profile = Profile()
    private lateinit var binding: ProfileGroupsActBinding
    private var type: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Login user
        profile.login(this, 0)
        binding = ProfileGroupsActBinding.inflate(layoutInflater)
        setContentView(binding.root)

        type = (intent.extras ?: return).getInt("type")

        val id = (intent.extras ?: return).getInt("id")

        binding.content.title.text = when (type) {
            1 -> "Projects"
            else -> "Groups"
        }

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        filldata(id)

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            filldata(id)
            refresher.isRefreshing = false
        }
    }

    private fun filldata(id: Int) {
        hideAll()
        val emailList: MutableList<String> = mutableListOf()

        var url = when (type) {
            1 -> "https://gitlab.com/api/v4/users/$id/projects"
            else -> "https://gitlab.com/api/v4/$id/groups"
        }

        if (id == profile.getData("id").toInt() && type == 1) {
            url = "https://gitlab.com/api/v4/projects"
        } else if (id == profile.getData("id").toInt() && type == 0) {
            url = "https://gitlab.com/api/v4/groups"
        }

        val activity = when (type) {
            1 -> ProjectAct::class.java
            else -> GroupsAct::class.java
        }

        AndroidNetworking
            .get(url)
            .addQueryParameter("access_token", profile.getData("token"))
            .addQueryParameter("membership", "true")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    var index = 0
                    while (index != response.length()) {
                        emailList.add(response[index].toString())
                        index++
                    }
                    val infoListAdapter =
                        GroupOrProjectListAdapter(this@ProfileGroupsAct, emailList.toTypedArray())
                    binding.content.infoList.adapter = infoListAdapter
                    binding.content.infoList.divider = null
                    binding.content.infoList.onItemClickListener =
                        AdapterView.OnItemClickListener { parent, view, position, id ->
                            val selectedItem = parent.getItemAtPosition(position) as String
                            val intent = Intent(applicationContext, activity)
                            val bundle = Bundle()
                            bundle.putInt("id", JSONObject(selectedItem).getInt("id"))
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }
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
        binding.content.infoList.isGone = false
    }

    fun hideAll() {
        binding.content.refresher.isGone = false
        binding.content.infoList.isGone = true
    }
}