package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.adapters.UserAdapter
import org.bandev.labyrinth.widgets.NonScrollListView
import org.json.JSONArray
import org.json.JSONObject


class GroupsAct : AppCompatActivity() {

    private var data = ""
    var listView: NonScrollListView? = null

    private lateinit var members: View
    lateinit var projects: View
    private lateinit var progress: ProgressBar

    var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_act)

        profile.login(this, 0)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        members = findViewById(R.id.contentView3)
        projects = findViewById(R.id.contentView4)
        progress = findViewById(R.id.progressBar)

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
        data = intent.extras?.getString("data").toString()

        val dataJson = JSONObject(data)

        val avatar = findViewById<ImageView>(R.id.avatar)

        avatar.load(dataJson.getString("avatar_url"))

        val usernameTextView: TextView = findViewById(R.id.usernmame)
        val emailTextView: TextView = findViewById(R.id.email)
        val descriptionTextView: TextView = findViewById(R.id.description)
        val locationTextView: TextView = findViewById(R.id.location)

        usernameTextView.text = dataJson.getString("name")
        emailTextView.text = dataJson.getString("path")
        if (dataJson.getString("description") == "") {
            descriptionTextView.isGone = true
        } else {
            descriptionTextView.text = dataJson.getString("description")
        }
        if (dataJson.getString("visibility") == "public") {
            locationTextView.text = "Public"
            locationTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_globe, 0, 0, 0)
        } else {
            locationTextView.text = "Private"
            locationTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_padlock, 0, 0, 0)
        }

        //Show group members

        val token = profile.getData("token")
        val id = dataJson.getInt("id")


        listView = findViewById(R.id.groupsList)
        (listView ?: return).isClickable = true
        (listView ?: return).isFocusable = true
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
                    (listView ?: return).isClickable = true

                    (listView ?: return).onItemClickListener =
                        AdapterView.OnItemClickListener { parent, view, position, id ->
                            val selectedItem = parent.getItemAtPosition(position) as String
                            val json = JSONObject(selectedItem)
                            if (json.getString("username") == profile.getData("username")) {
                                val intent = Intent(applicationContext, ProfileAct::class.java)
                                val bundle = Bundle()
                                bundle.putString("data", selectedItem)
                                intent.putExtras(bundle)
                                startActivity(intent)
                            } else {
                                val intent =
                                    Intent(applicationContext, ProfileAct::class.java)
                                val bundle = Bundle()
                                bundle.putString("data", selectedItem)
                                intent.putExtras(bundle)
                                startActivity(intent)
                            }


                        }
                }

                override fun onError(error: ANError?) {
                    // handle error
                }

            })

        val listView2 = findViewById<NonScrollListView>(R.id.projectsList2)
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
                    listView2.divider = null

                    listView2.onItemClickListener =
                        AdapterView.OnItemClickListener { parent, view, position, id ->
                            val selectedItem = parent.getItemAtPosition(position) as String
                            val intent = Intent(applicationContext, ProjectAct::class.java)
                            val bundle = Bundle()
                            bundle.putString("data", selectedItem)
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }

                    showAll()
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

    private fun hideAll() {
        progress.isGone = false
        members.isGone = true
        projects.isGone = true
    }

    fun showAll() {
        progress.isGone = true
        members.isGone = false
        projects.isGone = false
    }
}