package org.bandev.labyrinth

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.google.android.material.imageview.ShapeableImageView
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.account.activities.ProfileEmailsAct
import org.bandev.labyrinth.account.activities.ProfileGroupsAct
import org.bandev.labyrinth.account.activities.ProfileStatusAct
import org.bandev.labyrinth.account.activities.ProfileTokenAct
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.projects.BranchSelector
import org.bandev.labyrinth.projects.Commits
import org.bandev.labyrinth.projects.FileViewer
import org.bandev.labyrinth.projects.IssuesList
import org.bandev.labyrinth.widgets.About
import org.json.JSONObject


class ProfileAct : AppCompatActivity() {

    private var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Login user
        profile.login(this, 0)

        setContentView(R.layout.profile_act)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        Compatibility().edgeToEdge2(window, View(this), toolbar, resources)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(android.R.color.transparent)

        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back_white)

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

        val avatar = findViewById<ImageView>(R.id.avatar2)
        avatar.load(profile.getData("avatarUrl")) {
            crossfade(true)
            transformations(
                    CircleCropTransformation()
            )
        }

        val usernameTextView: TextView = findViewById(R.id.name_)
        val emailTextView: TextView = findViewById(R.id.slug2)
        val infoListView: ListView = findViewById(R.id.infoList)
        // val descriptionTextView: TextView = findViewById(R.id.description2)
        //  val locationTextView: TextView = findViewById(R.id.forks2)

        usernameTextView.text = profile.getData("username")
        emailTextView.text = profile.getData("email")
        if (profile.getData("bio") == "") {
            // descriptionTextView.isGone = true
        } else {
            //   descriptionTextView.text = profile.getData("bio")
        }
        if (profile.getData("location") == "") {
            // locationTextView.isGone = true
        } else {
            // locationTextView.text = profile.getData("location")
        }

        val optionsList: MutableList<String> = mutableListOf()
        optionsList.add("{ 'left' : 'Status', 'right' : '>', 'icon' : 'status' }")
        optionsList.add("{ 'left' : 'Keys', 'right' : '>', 'icon' : 'key' }")
        optionsList.add("{ 'left' : 'Emails', 'right' : '>', 'icon' : 'email' }")
        optionsList.add("{ 'left' : 'Access Tokens', 'right' : '>', 'icon' : 'secure' }")

        val infoListAdapter = InfoListAdapter(this@ProfileAct, optionsList.toTypedArray())
        infoListView.adapter = infoListAdapter

        infoListView.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    val selectedItem = parent.getItemAtPosition(position) as String
                    val obj = JSONObject(selectedItem)
                    when {
                        obj.getString("left") == "Keys" -> {
                            val intent = Intent(applicationContext, ProfileKeysAct::class.java)
                            startActivity(intent)
                        }
                        obj.getString("left") == "Emails" -> {
                            val intent = Intent(applicationContext, ProfileEmailsAct::class.java)
                            startActivity(intent)
                        }
                        obj.getString("left") == "Status" -> {
                            val intent = Intent(applicationContext, ProfileStatusAct::class.java)
                            startActivity(intent)
                        }
                        obj.getString("left") == "Access Tokens" -> {
                            val intent = Intent(applicationContext, ProfileTokenAct::class.java)
                            startActivity(intent)
                        }
                    }
                }

        val extendedOptions: MutableList<String> = mutableListOf()
        extendedOptions.add("{ 'left' : 'Groups', 'right' : '>', 'icon' : 'groups' }")
        extendedOptions.add("{ 'left' : 'Projects', 'right' : '>', 'icon' : 'repo' }")

        val extendedList = findViewById<ListView>(R.id.extendedList)
        extendedList.adapter = InfoListAdapter(this@ProfileAct, extendedOptions.toTypedArray())

        extendedList.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    val selectedItem = parent.getItemAtPosition(position) as String
                    val obj = JSONObject(selectedItem)
                    val left = obj.getString("left")
                    when (left) {
                        "Groups" -> {
                            val intent = Intent(applicationContext, ProfileGroupsAct::class.java)
                            intent.putExtra("type", 0)
                            startActivity(intent)
                        }
                        "Projects" -> {
                            val intent = Intent(applicationContext, ProfileGroupsAct::class.java)
                            intent.putExtra("type", 1)
                            startActivity(intent)
                        }
                    }
                }


        val settingsOptions: MutableList<String> = mutableListOf()
        settingsOptions.add("{ 'left' : 'Settings', 'right' : '>', 'icon' : 'settings' }")
        settingsOptions.add("{ 'left' : 'About App', 'right' : '>', 'icon' : 'about' }")

        val settingsList = findViewById<ListView>(R.id.settingsList)
        settingsList.adapter = InfoListAdapter(this@ProfileAct, settingsOptions.toTypedArray())

        settingsList.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    val selectedItem = parent.getItemAtPosition(position) as String
                    val obj = JSONObject(selectedItem)
                    when (obj.getString("left")) {
                        "Settings" -> {
                            val intent = Intent(applicationContext, SettingsAct::class.java)
                            startActivity(intent)
                        }
                        "About App" -> {
                            val intent = Intent(applicationContext, About::class.java)
                            startActivity(intent)
                        }
                    }
                }


        /* val userGroups = getSharedPreferences("User-Groups", 0)

        val listView = findViewById<ListView>(R.id.groupsList)

        var i = 0
        val list: MutableList<String?> = mutableListOf()
        while (i != userGroups.getInt("numGroups", 0)) {
            list.add(userGroups.getString(i.toString(), "null"))
            i++
        }

        val adapter = GroupOrProjectListAdapter(this, list.toTypedArray())
        listView.adapter = adapter
        listView.divider = null
        justifyListViewHeightBasedOnChildren(listView)

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                val intent = Intent(this, GroupsAct::class.java)
                val bundle = Bundle()
                bundle.putString("data", selectedItem)
                intent.putExtras(bundle)
                startActivity(intent)
            }

        val projectLists = getSharedPreferences("User-Projects", 0)

        val listViewProjects = findViewById<ListView>(R.id.projectsList)

        var i2 = 0
        val list2: MutableList<String?> = mutableListOf()
        while (i2 != projectLists.getInt("numProjects", 0)) {
            list2.add(projectLists.getString(i2.toString(), "null"))
            i2++
        }

        val adapter2 = GroupOrProjectListAdapter(this, list2.toTypedArray())
        listViewProjects.adapter = adapter2
        listViewProjects.divider = null
        justifyListViewHeightBasedOnChildren(listViewProjects)

        listViewProjects.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                val intent = Intent(this, ProjectAct::class.java)
                val bundle = Bundle()
                bundle.putString("data", selectedItem)
                intent.putExtras(bundle)
                startActivity(intent)
            }*/
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}