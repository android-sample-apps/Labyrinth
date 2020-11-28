package org.bandev.labyrinth

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.adapters.IssueAdapter
import org.bandev.labyrinth.adapters.UserAdapter
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.core.User
import org.bandev.labyrinth.widgets.NonScrollListView
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.GlobalScope as GlobalScope


class GroupsAct : AppCompatActivity() {

    var data = ""
    var listView: NonScrollListView? = null

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_act)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        filldata()

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            val pref = getSharedPreferences("Settings", 0)
            val token = pref.getString("token", "null").toString()
            Api().getUserGroups(this, token)
            Api().getUserProjects(this, token)
            filldata()
            refresher.isRefreshing = false
        }
    }

    private fun filldata() {
        data = intent.extras?.getString("data").toString()

        val dataJson = JSONObject(data)

        val pref = getSharedPreferences("User", 0)

        val avatar = findViewById<ImageView>(R.id.avatar)
        Picasso.get().load(dataJson?.getString("avatar_url")).resize(400, 400)
            .transform(RoundedTransform(90, 0))
            .into(avatar)

        val usernameTextView: TextView = findViewById(R.id.usernmame)
        val emailTextView: TextView = findViewById(R.id.email)
        val descriptionTextView: TextView = findViewById(R.id.description)
        val locationTextView: TextView = findViewById(R.id.location)

        usernameTextView.text = dataJson?.getString("name")
        emailTextView.text = dataJson?.getString("path")
        if (dataJson?.getString("description") == "") {
            descriptionTextView.isGone = true
        } else {
            descriptionTextView.text = dataJson?.getString("description")
        }
        if (dataJson?.getString("visibility") == "public") {
            locationTextView.text = "Public"
            locationTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_globe, 0, 0, 0)
        } else {
            locationTextView.text = "Private"
            locationTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_padlock, 0, 0, 0)
        }

        //Show group members

        val token = User().getToken(this)
        val id = dataJson?.getInt("id")


        listView = findViewById<NonScrollListView>(R.id.groupsList)
        val list: MutableList<String?> = mutableListOf()


        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.get("https://gitlab.com/api/v4/groups/$id/members?access_token=$token")
            .setTag("getUsers")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?) {
                    var index = 0
                    while (index != response?.length()) {
                        val string = response?.get(index)?.toString().toString()
                        list.add(string)
                        index++

                    }
                    val adapter = UserAdapter(this@GroupsAct, list.toTypedArray())
                    (listView ?: return).adapter = adapter
                    (listView ?: return).divider = null
                }

                override fun onError(error: ANError?) {
                    // handle error
                }

            })

        val listView2 = findViewById<NonScrollListView>(R.id.groupsList)
        val list2: MutableList<String?> = mutableListOf()

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.get("https://gitlab.com/api/v4/groups/$id/projects?access_token=$token")
            .setTag("getUsers")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?) {
                    var index = 0
                    while (index != response?.length()) {
                        val string = response?.get(index)?.toString().toString()
                        list2.add(string)
                        index++

                    }
                    val adapter2 = GroupOrProjectListAdapter(this@GroupsAct, list2.toTypedArray())
                    (listView2 ?: return).adapter = adapter2
                    (listView2  ?: return).divider = null
                }

                override fun onError(error: ANError?) {
                    // handle error
                }

            })


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open -> {
                val pref = getSharedPreferences("User", 0)

                val url = pref!!.getString("webUrl", "https://gitlab.com")
                val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
                builder.setToolbarColor(Color.parseColor("#0067f4"))
                val customTabsIntent: CustomTabsIntent = builder.build()
                customTabsIntent.launchUrl(this, Uri.parse(url))
                super.onOptionsItemSelected(item)
            }
            R.id.settings -> {
                val i = Intent(this, SettingsAct::class.java)
                startActivity(i)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    internal fun justifyListViewHeightBasedOnChildren(listView: ListView) {
        val adapter = listView.adapter ?: return
        val vg: ViewGroup = listView
        var totalHeight = 0
        for (i in 0 until adapter.count) {
            val listItem: View = adapter.getView(i, null, vg)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        val par = listView.layoutParams
        par.height = totalHeight + listView.dividerHeight * (adapter.count - 1)
        listView.layoutParams = par
        listView.requestLayout()
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}