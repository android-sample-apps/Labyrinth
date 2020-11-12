package org.bandev.labyrinth.projects

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import org.bandev.labyrinth.adapters.issues
import org.bandev.labyrinth.core.Api
import org.json.JSONArray
import org.json.JSONObject


class issues_list : AppCompatActivity() {

    var issueArryIn: JSONArray? = null
    var repoObjIn: JSONObject? = null
    var token: String = ""
    var projectId: String = ""
    var listView: ListView? = null
    var listView2: ListView? = null
    var progress_bar: ProgressBar? = null


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.issues_list_act)

        token = Api().getUserToken(this)

        listView = findViewById<ListView>(R.id.listView)
        listView2 = findViewById<ListView>(R.id.listView2)
        progress_bar = findViewById(R.id.progressBar2)

        repoObjIn = JSONObject(intent.getStringExtra("repo").toString())

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        var title: TextView = findViewById(R.id.title)
        title.text = repoObjIn!!.getString("name") + " / Issues"

        projectId = repoObjIn!!.getString("id")

        fillData()

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(R.color.colorPrimary)
        refresher.setOnRefreshListener {
            fillData()
            refresher.isRefreshing = false
        }

    }

    fun fillData() {
        hideAll()
        val list: MutableList<String> = ArrayList()
        var responseArray: JSONArray? = null
        //Get a list of issues from GitLab#
        var done = false
        var context = this
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking
                .get("https://gitlab.com/api/v4/projects/$projectId/issues?token=$token&state=opened")
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray?) {
                        for (i in 0 until response!!.length()) {
                            list.add(response!!.getJSONObject(i).toString())
                        }

                        val adapter = issues(context, list.toTypedArray())
                        listView!!.adapter = adapter
                        listView!!.divider = null
                        justifyListViewHeightBasedOnChildren(listView!!)
                        done = true

                        AndroidNetworking.initialize(applicationContext)
                        AndroidNetworking
                                .get("https://gitlab.com/api/v4/projects/$projectId/issues?token=$token&state=closed")
                                .build()
                                .getAsJSONArray(object : JSONArrayRequestListener {
                                    override fun onResponse(response: JSONArray?) {

                                        for (i in 0 until response!!.length()) {
                                            list.add(response!!.getJSONObject(i).toString())

                                        }

                                        val adapter2 = issues(context, list.toTypedArray())
                                        listView2!!.adapter = adapter2
                                        listView2!!.divider = null
                                        justifyListViewHeightBasedOnChildren(listView2!!)
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

    private fun hideAll() {
        listView?.isGone = true
        listView2?.isGone = true
        progress_bar?.isGone = false
    }

    fun showAll() {
        listView?.isGone = false
        listView2?.isGone = false
        progress_bar?.isGone = true
    }

}