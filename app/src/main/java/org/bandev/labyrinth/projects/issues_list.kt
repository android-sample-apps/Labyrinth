package org.bandev.labyrinth.projects

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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



    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.issues_list_act)

        token = Api().getUserToken(this)

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

    fun fillData(){
        var responseArray :JSONArray? = null
        //Get a list of issues from GitLab#
        var context = this
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking
                .get("https://gitlab.com/api/v4/projects/$projectId/issues?token=$token&state=opened")
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray?) {
                        val list: MutableList<String> = ArrayList()
                        for (i in 0 until response!!.length()) {
                            list.add(response!!.getJSONObject(i).toString())
                        }
                        val listView = findViewById<ListView>(R.id.listView)
                        val adapter = issues(context, list.toTypedArray())
                        listView.adapter = adapter
                        listView.divider = null
                    }

                    override fun onError(anError: ANError?) {
                        TODO("Not yet implemented")
                    }
                })

    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}