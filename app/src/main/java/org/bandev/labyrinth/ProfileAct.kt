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
import coil.transform.RoundedCornersTransformation
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.core.Api


class ProfileAct : AppCompatActivity() {

    private var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Login user
        profile.login(this, 0)

        setContentView(R.layout.profile_act)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

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

        val avatar = findViewById<ImageView>(R.id.avatar2)
        avatar.load(profile.getData("avatarUrl")) {
            crossfade(true)
            transformations(
                    RoundedCornersTransformation(
                            20f,
                            20f,
                            20f,
                            20f
                    )
            )
        }

        val usernameTextView: TextView = findViewById(R.id.name_)
        val emailTextView: TextView = findViewById(R.id.slug2)
        val descriptionTextView: TextView = findViewById(R.id.description2)
        val locationTextView: TextView = findViewById(R.id.forks2)

        usernameTextView.text = profile.getData("username")
        emailTextView.text = profile.getData("email")
        if (profile.getData("bio") == "") {
            descriptionTextView.isGone = true
        } else {
            descriptionTextView.text = profile.getData("bio")
        }
        if (profile.getData("location") == "") {
            locationTextView.isGone = true
        } else {
            locationTextView.text = profile.getData("location")
        }

        val userGroups = getSharedPreferences("User-Groups", 0)

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
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open -> {
                val url = profile.getData("webUrl")
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

    private fun justifyListViewHeightBasedOnChildren(listView: ListView) {
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