package org.bandev.labyrinth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.mukesh.MarkdownView
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.projects.ReadMe
import org.json.JSONArray
import org.json.JSONObject


class ProjectAct : AppCompatActivity() {

    private var latestCommit: View? = null
    private var progressBar: ProgressBar? = null

    private var projectId = ""


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.project_act)

        latestCommit = findViewById(R.id.contentView3)
        progressBar = findViewById(R.id.progressBar)


        filldata()


        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)


        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(R.color.colorPrimary)
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
        hideAll()
        val data = intent.extras?.getString("data")


        val dataJson = JSONObject(data)

        val nameTextView: TextView = findViewById(R.id.name_1)
        val slugTextView: TextView = findViewById(R.id.slug)
        val descriptionTextView: TextView = findViewById(R.id.description)
        val starsTextView: TextView = findViewById(R.id.stars)
        val forksTextView: TextView = findViewById(R.id.forks)

        val avatar = findViewById<ImageView>(R.id.avatar)
        if (dataJson.getString("visibility") == "public") {
            Picasso.get().load(dataJson.getString("avatar_url")).transform(RoundedTransform(90, 0))
                    .into(avatar)
        } else {
            Picasso.get().load("file:///android_asset/lock.png").transform(RoundedTransform(90, 0))
                    .into(avatar)
        }

        if (dataJson.getString("star_count") != "1") {
            starsTextView.text = dataJson.getString("star_count") + " Stars"
        } else {
            starsTextView.text = dataJson.getString("star_count") + " Star"
        }

        if (dataJson.getString("forks_count") != "1") {
            forksTextView.text = dataJson.getString("forks_count") + " Forks"
        } else {
            forksTextView.text = dataJson.getString("forks_count") + " Fork"
        }

        projectId = dataJson.getString("id")
        val pref = getSharedPreferences("User", 0)
        val token = pref.getString("token", "no")

        AndroidNetworking.initialize(this)
        AndroidNetworking.get("https://gitlab.com/api/v4/projects/$projectId/repository/commits?access_token=$token&order=topo")
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray?) {
                        val string = response?.get(0)?.toString()
                        val objec = JSONObject(string)

                        val commitTitle = findViewById<TextView>(R.id.name)

                        commitTitle.text = objec.getString("title")

                        val commitTitle2 = findViewById<TextView>(R.id.visibility)

                        commitTitle2.text = objec.getString("short_id")

                        val avatar = findViewById<ImageView>(R.id.avatar_list)

                        val em = objec.getString("committer_email")

                        AndroidNetworking.initialize(applicationContext)
                        AndroidNetworking.get("https://gitlab.com/api/v4/avatar?email=$em")
                                .build()
                                .getAsJSONObject(object : JSONObjectRequestListener {
                                    override fun onResponse(response: JSONObject?) {

                                        Picasso.get().load((response
                                                ?: return).getString("avatar_url"))
                                                .transform(CircleTransform()).into(avatar)


                                        showAll()


                                    }

                                    override fun onError(error: ANError?) {
                                        // handle error
                                        Toast.makeText(
                                                applicationContext,
                                                "Error retrieving committer avatar!",
                                                LENGTH_LONG
                                        ).show()
                                    }

                                })


                        Log.d("em", objec.getString("committer_email"))
                    }

                    override fun onError(error: ANError?) {
                        // handle error
                        Toast.makeText(applicationContext, "error", LENGTH_LONG).show()
                    }

                })




        nameTextView.text = dataJson.getString("name")
        slugTextView.text = dataJson.getString("name_with_namespace")
        if (dataJson.getString("description") != "") {
            descriptionTextView.text = dataJson.getString("description")
        } else {
            descriptionTextView.isGone = true
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.project_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open -> {
                val pref = getSharedPreferences("User", 0)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(pref.getString("webUrl", "https://gitlab.com"))
                startActivity(i)
                super.onOptionsItemSelected(item)
            }
            R.id.readme -> {
                val i = Intent(this, ReadMe::class.java)
                i.putExtra("projectId", projectId.toInt())
                startActivity(i)
                super.onOptionsItemSelected(item)
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

    private fun hideAll() {
        latestCommit?.isGone = true
        progressBar?.isGone = false
    }

    fun showAll() {
        latestCommit?.isGone = false
        progressBar?.isGone = true
    }

}