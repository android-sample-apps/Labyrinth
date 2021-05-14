package org.bandev.labyrinth.projects

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.imageLoader
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.CommitAdapterVague
import org.bandev.labyrinth.core.Animations
import org.bandev.libraries.NonScroll.NonScrollListView
import org.json.JSONArray

class Commits : AppCompatActivity() {

    var issueArryIn: JSONArray? = null
    var token: String = ""
    var projectId: Int = 0
    var listView: NonScrollListView? = null
    private var listView2: ListView? = null
    private var progressBar: ProgressBar? = null

    private var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commits_list)

        profile.login(this, 0)
        token = profile.getData("token")
        listView = findViewById(R.id.listView)
        progressBar = findViewById(R.id.progressBar2)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = backDrawable


        //Toolbar shadow animation
        val scroll = findViewById<ScrollView>(R.id.scroll)
        Animations().toolbarShadowScroll(scroll, toolbar)

        val title: TextView = findViewById(R.id.title)
        title.text = "Commits"

        projectId = (intent.extras ?: return).getInt("id")

        fillData()

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            fillData()
            refresher.isRefreshing = false
        }
    }

    private fun fillData() {
        hideAll()
        val list: MutableList<String> = ArrayList()

        val context = this
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking
            .get("https://gitlab.com/api/v4/projects/$projectId/repository/commits?access_token=$token&with_stats=true")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?) {
                    for (i in 0 until (response ?: return).length()) {
                        list.add(response.getJSONObject(i).toString())
                    }

                    val adapter = CommitAdapterVague(context, list.toTypedArray(), imageLoader)
                    (listView ?: return).adapter = adapter
                    (listView ?: return).divider = null

                    (listView ?: return).onItemClickListener =
                        AdapterView.OnItemClickListener { parent, _, position, _ ->
                            val selectedItem = parent.getItemAtPosition(position) as String
                            val intent = Intent(applicationContext, IndividualCommit::class.java)
                            val bundle = Bundle()
                            bundle.putString("commitDataIn", selectedItem)
                            bundle.putInt("projectId", projectId)
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }

                    showAll()
                }

                override fun onError(anError: ANError?) {
                    Toast.makeText(context, "Error 1", LENGTH_SHORT).show()
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun hideAll() {
        listView?.isGone = true
        listView2?.isGone = true
        progressBar?.isGone = false
    }

    fun showAll() {
        listView?.isGone = false
        listView2?.isGone = false
        progressBar?.isGone = true
    }

}