package org.bandev.labyrinth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import coil.transform.RoundedCornersTransformation
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.core.Helpful
import org.bandev.labyrinth.projects.*
import org.bandev.labyrinth.widgets.NonScrollListView
import org.json.JSONArray
import org.json.JSONObject


class ProjectAct : AppCompatActivity() {

    private var latestCommit: View? = null
    private var progressBar: ProgressBar? = null
    private var infoBar: View? = null
    private var projectId = ""
    private var projectPath = ""
    private var url = ""
    private var webUrl = ""
    private var branch = ""
    private var data = ""

    private var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.project_act)

        latestCommit = findViewById(R.id.contentView3)
        progressBar = findViewById(R.id.progressBar)
        infoBar = findViewById(R.id.contentView4)

        profile.login(this, 0)

        filldata()

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back_white)

        Compatibility().edgeToEdge(window, View(this), toolbar, resources)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.colorPrimary)
        }


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
        hideAll()
        data = intent.extras?.getString("data").toString()

        val dataJson = JSONObject(data)

        projectId = dataJson.getString("id")

        findViewById<TextView>(R.id.title).text =  dataJson.getString("name")


        val nameTextView: TextView = findViewById(R.id.name_1)
        val slugTextView: TextView = findViewById(R.id.slug)
        val descriptionTextView: TextView = findViewById(R.id.description)
        val starsTextView: TextView = findViewById(R.id.stars)
        val forksTextView: TextView = findViewById(R.id.forks)

        val Mainlang: TextView = findViewById(R.id.mainLang)


        AndroidNetworking.initialize(this)
        AndroidNetworking.get("https://gitlab.com/api/v4/projects/$projectId/languages")
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String) {
                    val languages = response.removeSurrounding("{", "}")
                    val languages_arr = languages.split(",")

                    val main_lang_arr = languages_arr[0].split(":")

                    Mainlang.text =
                        main_lang_arr[0].removeSurrounding('"'.toString(), '"'.toString())


                    AndroidNetworking.initialize(applicationContext)
                    AndroidNetworking.get("https://bandev.uk/api/labyrinth/resources/colours")
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                val unwrappedDrawable = AppCompatResources.getDrawable(
                                    applicationContext,
                                    R.drawable.ic_dot
                                )
                                val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
                                DrawableCompat.setTint(
                                    wrappedDrawable,
                                    Color.parseColor(
                                        response.getJSONObject(Mainlang.text as String)
                                            .getString("color")
                                    )
                                )
                                Mainlang.setCompoundDrawablesWithIntrinsicBounds(
                                    wrappedDrawable,
                                    null,
                                    null,
                                    null
                                )
                            }

                            override fun onError(error: ANError?) {
                                // handle error
                                Toast.makeText(applicationContext, error.toString(), LENGTH_LONG)
                                    .show()
                            }

                        })

                }

                override fun onError(error: ANError?) {
                    // handle error
                    Toast.makeText(applicationContext, "error", LENGTH_LONG).show()
                }

            })


        val avatar = findViewById<ImageView>(R.id.avatar)
        avatar.load(dataJson.getString("avatar_url")) {
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
        val token = profile.getData("token")

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


                    val pipeline = objec.getString("status")

                    val pipelineStatus = findViewById<ImageView>(R.id.pipeline)

                    val latestCommit = findViewById<View>(R.id.latest_commit)
                    latestCommit.setOnClickListener {
                        val i = Intent(applicationContext, IndividualCommit::class.java)
                        i.putExtra("projectId", this@ProjectAct.projectId.toInt())
                        i.putExtra("commitDataIn", objec.toString())
                        startActivity(i)
                    }


                    when (pipeline) {
                        "success" -> {
                            pipelineStatus.setImageDrawable(
                                ContextCompat.getDrawable(
                                    applicationContext,
                                    R.drawable.ic_success
                                )
                            )
                        }
                        "failed" -> {
                            pipelineStatus.setImageDrawable(
                                ContextCompat.getDrawable(
                                    applicationContext,
                                    R.drawable.ic_failed
                                )
                            )
                        }
                        "running" -> {
                            pipelineStatus.setImageDrawable(
                                ContextCompat.getDrawable(
                                    applicationContext,
                                    R.drawable.ic_running
                                )
                            )
                        }
                        "canceled" -> {
                            pipelineStatus.setImageDrawable(
                                ContextCompat.getDrawable(
                                    applicationContext,
                                    R.drawable.ic_canceled
                                )
                            )
                        }
                    }

                    AndroidNetworking.initialize(applicationContext)
                    AndroidNetworking.get("https://gitlab.com/api/v4/avatar?email=$em")
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject?) {

                                Picasso.get().load(
                                    (response
                                        ?: return).getString("avatar_url")
                                )
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
        slugTextView.text = "Id: " + dataJson.getString("id")
        if (dataJson.getString("description") != "") {
            descriptionTextView.text = dataJson.getString("description")
        } else {
            descriptionTextView.text = "No Description"
        }

        val infoListView = findViewById<NonScrollListView>(R.id.infoList)

        val infoList: MutableList<String> = mutableListOf()

        val issueNum = dataJson.getInt("open_issues_count")
        val defaultBranch = getBranch(dataJson)

        var commitsCount = ""
        var filesSize: Long
        var fileSizeStr = ""

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.get("https://gitlab.com/api/v4/projects/$projectId?statistics=true&access_token=$token")

            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response?.has("statistics") == true) {
                        commitsCount = (response
                            ?: return).getJSONObject("statistics").getInt("commit_count").toString()
                        filesSize =
                            response.getJSONObject("statistics").getInt("repository_size").toLong()
                        fileSizeStr =
                            Helpful().humanReadableByteCountSI(filesSize.toLong()).toString()
                    } else {
                        commitsCount = "Unknown"
                        fileSizeStr = "Unknown"
                    }

                    infoList.add("{ 'left' : 'Issues', 'right' : '$issueNum', 'icon' : 'branch' }")
                    infoList.add("{ 'left' : 'Branch', 'right' : '$defaultBranch', 'icon' : 'security' }")
                    infoList.add("{ 'left' : 'View Files', 'right' : '$fileSizeStr', 'icon' : 'file' }")
                    infoList.add("{ 'left' : 'Commits', 'right' : '$commitsCount', 'icon' : 'commit' }")

                    val infoListAdapter = InfoListAdapter(this@ProjectAct, infoList.toTypedArray())
                    infoListView.adapter = infoListAdapter

                    infoListView.onItemClickListener =
                        AdapterView.OnItemClickListener { parent, view, position, id ->
                            val selectedItem = parent.getItemAtPosition(position) as String
                            val obj = JSONObject(selectedItem)
                            when {
                                obj.getString("left") == "Issues" -> {
                                    val intent = Intent(applicationContext, IssuesList::class.java)
                                    val bundle = Bundle()
                                    bundle.putString("repo", data)
                                    intent.putExtras(bundle)
                                    startActivity(intent)
                                }
                                obj.getString("left") == "Commits" -> {
                                    val intent = Intent(applicationContext, Commits::class.java)
                                    val bundle = Bundle()
                                    bundle.putString("repo", data)
                                    intent.putExtras(bundle)
                                    startActivity(intent)
                                }
                                obj.getString("left") == "View Files" -> {
                                    val dataJs = JSONObject(data)
                                    val intent = Intent(applicationContext, FileViewer::class.java)
                                    val bundle = Bundle()
                                    bundle.putString("repoName", dataJs.getString("name"))
                                    bundle.putString("repoLogoUrl", dataJs.getString("avatar_url"))
                                    bundle.putString("repoId", dataJs.getString("id"))
                                    bundle.putString("path", "")
                                    bundle.putString("branch", defaultBranch)
                                    intent.putExtras(bundle)
                                    startActivity(intent)
                                }
                                obj.getString("left") == "Branch" -> {
                                    val dataJs = JSONObject(data)
                                    val intent =
                                        Intent(applicationContext, BranchSelector::class.java)
                                    val bundle = Bundle()
                                    bundle.putString("repoName", dataJs.getString("name"))
                                    bundle.putString("repoLogoUrl", dataJs.getString("avatar_url"))
                                    bundle.putString("repoId", dataJs.getString("id"))
                                    bundle.putString("branch", defaultBranch)
                                    intent.putExtras(bundle)
                                    startActivityForResult(intent, 0)
                                }
                            }
                        }

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

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            0 -> {
                updateBranch((data ?: return).getStringExtra("newBranch").toString())
            }
            // Other result codes
            else -> {
            }
        }
    }

    private fun updateBranch(branch: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(projectId + "_branch", branch)
            apply()
        }
        filldata()
    }

    private fun getBranch(dataJson: JSONObject): String {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        return when (val branch = sharedPref.getString(projectId + "_branch", "default")) {
            "default" -> dataJson.getString("default_branch")
            else -> branch.toString()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.project_menu, menu)
        return true
    }

    private fun toggleStar() {
        val token = profile.getData("token")
        AndroidNetworking.initialize(this)
        AndroidNetworking.post("https://gitlab.com/api/v4/projects/$projectId/star")
            .addHeaders("PRIVATE-TOKEN:", token)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?) {
                    //handle response
                    Toast.makeText(applicationContext, "Starred", LENGTH_LONG).show()
                }

                override fun onError(error: ANError?) {
                    // handle error
                    Toast.makeText(applicationContext, error.toString(), LENGTH_LONG).show()
                }

            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.star -> {
                toggleStar()
                super.onOptionsItemSelected(item)
            }
            R.id.open -> {
                val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
                builder.setToolbarColor(Color.parseColor("#0067f4"))
                val customTabsIntent: CustomTabsIntent = builder.build()
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


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


    private fun hideAll() {
        latestCommit?.isGone = true
        infoBar?.isGone = true
        progressBar?.isGone = false
    }

    fun showAll() {
        latestCommit?.isGone = false
        infoBar?.isGone = false
        progressBar?.isGone = true
    }
}