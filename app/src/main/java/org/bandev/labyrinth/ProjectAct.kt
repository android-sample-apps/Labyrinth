package org.bandev.labyrinth

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.Drawable.createFromResourceStream
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.projects.ReadMe
import org.bandev.labyrinth.projects.issues_list
import org.bandev.labyrinth.projects.reduceStorageSize
import org.json.JSONArray
import org.json.JSONObject


class ProjectAct : AppCompatActivity() {

    var latest_commit: View? = null
    var progress_bar: ProgressBar? = null
    var info_bar: View? = null

    var projectId = ""
    var projectPath = ""
    var url = ""
    var webUrl = ""
    var branch = ""
    var data = ""


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.project_act)


        latest_commit = findViewById(R.id.contentView3)
        progress_bar = findViewById(R.id.progressBar)
        info_bar = findViewById(R.id.contentView4)


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
        data = intent.extras?.getString("data").toString()


        val dataJson = JSONObject(data)

        val nameTextView: TextView = findViewById(R.id.name_1)
        val slugTextView: TextView = findViewById(R.id.slug)
        val descriptionTextView: TextView = findViewById(R.id.description)
        val starsTextView: TextView = findViewById(R.id.stars)
        val forksTextView: TextView = findViewById(R.id.forks)

        val avatar = findViewById<ImageView>(R.id.avatar)
        if (dataJson.getString("visibility") == "public") {

            Picasso.get().load(dataJson.getString("avatar_url")).transform(RoundedTransform(90, 0)).resize(400, 400)
                .into(avatar)
        } else {
            Picasso.get().load("file:///android_asset/lock.png").transform(RoundedTransform(90, 0)).resize(400, 400)
                .into(avatar)
        }

        url = dataJson.getString("web_url")

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
        projectPath = dataJson.getString("path_with_namespace")
        webUrl = dataJson.getString("web_url")
        branch = dataJson.getString("default_branch")
        val pref = getSharedPreferences("User", 0)
        val token = pref.getString("token", "no")

        AndroidNetworking.initialize(this)
        AndroidNetworking.get("https://gitlab.com/api/v4/projects/$projectId/repository/commits/$branch?access_token=$token")
                .addHeaders("PRIVATE-TOKEN: $token")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(objec: JSONObject) {

                        val commitTitle = findViewById<TextView>(R.id.name)

                        commitTitle.text = objec.getString("title")


                    val commitTitle2 = findViewById<TextView>(R.id.visibility)


                        commitTitle2.text = objec.getString("short_id")

                        val avatar = findViewById<ImageView>(R.id.avatar_list)

                        val em = objec.getString("committer_email")

                        try {
                            var pipeline = objec.getJSONObject("last_pipeline").getString("status")

                            var pipeline_status = findViewById<ImageView>(R.id.pipeline)

                            if (pipeline == "success") {
                                pipeline_status.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_success))
                            }
                        }catch( e: Error){

                        }

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



        var infoListView = findViewById<ListView>(R.id.infoList)

        var infoList : MutableList<String> = mutableListOf()

        var issueNum = dataJson.getInt("open_issues_count")
        var defaultBranch = dataJson.getString("default_branch")

        infoList.add("{ 'left' : 'Issues', 'right' : '$issueNum' }")
        infoList.add("{ 'left' : 'Branch', 'right' : '$defaultBranch' }")
        infoList.add("{ 'left' : 'View Files', 'right' : '' }")
        infoList.add("{ 'left' : 'Commits', 'right' : '' }")


        val infoListAdapter = InfoListAdapter(this, infoList.toTypedArray())
        infoListView.adapter = infoListAdapter
        infoListView.divider = null


        infoListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                var obj = JSONObject(selectedItem)
                if(obj.getString("left") == "Issues"){
                    val intent = Intent(this, issues_list::class.java)
                    val bundle = Bundle()
                    bundle.putString("repo", data)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }


        }





    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.project_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open -> {
                var builder : CustomTabsIntent.Builder = CustomTabsIntent.Builder()
                builder.setToolbarColor(Color.parseColor("#0067f4"))
                var customTabsIntent: CustomTabsIntent = builder.build()
                customTabsIntent.launchUrl(this, Uri.parse(url))
                super.onOptionsItemSelected(item)
            }
            R.id.readme -> {
                val i = Intent(this, ReadMe::class.java)
                i.putExtra("projectId", projectId.toInt())
                i.putExtra("projectPath", projectPath)
                i.putExtra("webUrl", webUrl)
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


    fun hideAll(){
        latest_commit?.isGone = true
        info_bar?.isGone = true
        progress_bar?.isGone = false
    }

    fun showAll(){
        latest_commit?.isGone = false
        info_bar?.isGone = false
        progress_bar?.isGone = true
    }

}