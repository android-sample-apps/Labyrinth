package org.bandev.labyrinth.projects

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.IssueAdapter
import org.bandev.labyrinth.core.Api
import org.json.JSONArray
import org.json.JSONObject

class IssuesList : AppCompatActivity() {

    var issueArryIn: JSONArray? = null
    private var repoObjIn: JSONObject? = null
    var token: String = ""
    var projectId: String = ""
    var listView: ListView? = null
    var listView2: ListView? = null
    private var progressBar: ProgressBar? = null

    var profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.issues_list_act)

        profile.login(this, 0)

        token = profile.getData("token")

        listView = findViewById(R.id.listView)
        listView2 = findViewById(R.id.listView2)
        progressBar = findViewById(R.id.progressBar2)

        repoObjIn = JSONObject(intent.getStringExtra("repo").toString())

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        val title: TextView = findViewById(R.id.title)
        title.text = (repoObjIn ?: return).getString("name") + " / Issues"

        projectId = (repoObjIn ?: return).getString("id")

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
        var responseArray: JSONArray? = null
        //Get a list of issues from GitLab#
        var done = false
        val context = this
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking
                .get("https://gitlab.com/api/v4/projects/$projectId/issues?token=$token&state=opened")
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray?) {
                        for (i in 0 until (response ?: return).length()) {
                            list.add(response.getJSONObject(i).toString())
                        }

                        val adapter = IssueAdapter(context, list.toTypedArray())
                        (listView ?: return).adapter = adapter
                        (listView ?: return).divider = null
                        justifyListViewHeightBasedOnChildren(listView ?: return)
                        done = true

                        AndroidNetworking.initialize(applicationContext)
                        AndroidNetworking
                                .get("https://gitlab.com/api/v4/projects/$projectId/issues?token=$token&state=closed")
                                .build()
                                .getAsJSONArray(object : JSONArrayRequestListener {
                                    override fun onResponse(response: JSONArray?) {

                                        for (i in 0 until (response ?: return).length()) {
                                            list.add(response.getJSONObject(i).toString())
                                        }

                                        val adapter2 = IssueAdapter(context, list.toTypedArray())
                                        (listView2 ?: return).adapter = adapter2
                                        (listView2 ?: return).divider = null
                                        justifyListViewHeightBasedOnChildren(listView2 ?: return)
                                        showAll()
                                    }

                                    override fun onError(anError: ANError?) {
                                        Toast.makeText(context, "Error 1", LENGTH_SHORT).show()
                                    }
                                })
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