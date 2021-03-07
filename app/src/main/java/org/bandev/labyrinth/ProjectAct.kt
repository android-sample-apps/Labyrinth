package org.bandev.labyrinth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.google.android.material.snackbar.Snackbar
import com.maxkeppeler.sheets.options.Option
import com.maxkeppeler.sheets.options.OptionsSheet
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.core.Connection
import org.bandev.labyrinth.core.Helpful
import org.bandev.labyrinth.core.obj.Commit
import org.bandev.labyrinth.core.obj.Project
import org.bandev.labyrinth.core.obj.ProjectStats
import org.bandev.labyrinth.databinding.ProjectActBinding
import org.bandev.labyrinth.projects.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


class ProjectAct : AppCompatActivity() {

    private var latestCommit: View? = null
    private var progressBar: ProgressBar? = null
    private var infoBar: View? = null
    private var projectId = ""
    private var id: Int = 0
    private lateinit var project: Project

    private lateinit var binding: ProjectActBinding

    private var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProjectActBinding.inflate(layoutInflater)
        setContentView(binding.root)

        latestCommit = findViewById(R.id.contentView3)
        progressBar = findViewById(R.id.progressBar)
        infoBar = findViewById(R.id.contentView4)

        id = (intent.extras ?: return).getInt("id")

        binding.content.avatar.load(R.color.browser_actions_bg_grey){
            transformations(RoundedCornersTransformation(20f))
        }

        profile.login(this, 0)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)


        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            showData(id)
            refresher.isRefreshing = false
        }
        showData(id)
    }

    private fun showData(id: Int) {
        val connection = Connection(this)
        connection.Project().get(id)
        hideAll()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            0 -> {
                updateBranch((data ?: return).getStringExtra("newBranch").toString())
            }
        }
    }

    private fun updateBranch(branch: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(projectId + "_branch", branch)
            apply()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.project_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.more){
            showOptions()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyReceive(event: Notify) {
        when (event) {
            is Notify.ReturnProject -> setData(event.project)
            is Notify.ReturnFork -> forked(event.forks, event.newProject)
            is Notify.ReturnStar -> starred(event.stars, event.positive)
            is Notify.ReturnCommit -> showLatestCommit(event.commit)
            is Notify.ReturnAvatar -> showCommitAvatar(event.url)
            is Notify.ReturnProjectStats -> showStats(event.projectStats)
        }
    }

    /**
     * Show the options bottom sheet on menu click
     * @author Jack Devey
     */

    private fun showOptions() {
        OptionsSheet().show(this) {
            title("Project Options")
            with(
                Option(R.drawable.ic_star_full, "Star"),
                Option(R.drawable.ic_forks, "Fork"),
                Option(R.drawable.ic_internet, "Open")
            )
            displayButtons(false)
            onPositive { index: Int, _: Option ->
                when (index) {
                    0 -> project.star()
                    1 -> project.fork()
                    2 -> project.openInBrowser(this@ProjectAct)
                }
            }
        }
    }

    /**
     * Update the stars count & alert user that the project has been starred/unstarred
     * @param stars [Int]
     * @param positive [Boolean]
     * @author Jack Devey
     */

    private fun starred(stars: Int, positive: Boolean) {
        binding.content.stars.text = stars.toString()
        val action = if (positive) "starred" else "unstarred"
        val text = "You just " + action + " " + project.name
        Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Update the forks count & alert user that the project has been forked
     * @param forks [Int]
     * @param newProject [Project]
     * @author Jack Devey
     */

    private fun forked(forks: Int, newProject: Project) {
        binding.content.forks.text = forks.toString()
        val text = project.namespace + " has been forked to " + newProject.namespace
        Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
            .setAction("View") {
                // TODO: 20/02/2021
            }.show()
    }

    /**
     * Display the data we know about the project on the users screen & then request the latest
     * commit data
     * @param returnProject [Project]
     * @author Jack Devey
     */

    private fun setData(returnProject: Project) {
        project = returnProject
        binding.content.projectName.text = project.name
        binding.content.slug.text = project.namespace
        binding.content.description.text = project.description
        binding.content.stars.text = project.stars.toString()
        binding.content.forks.text = project.forks.toString()
        binding.content.avatar.load(project.avatar) {
            crossfade(true)
            transformations(RoundedCornersTransformation(30f))
        }
        project.getLastCommit()
    }

    /**
     * Show the latest commit data for the project & request it's avatar
     * @param commit [Commit]
     * @author Jack Devey
     */

    private fun showLatestCommit(commit: Commit) {
        binding.content.latestCommit.name.text = commit.title
        binding.content.latestCommit.visibility.text = commit.shortID
        val icon = when (commit.status) {
            "success" -> R.drawable.ic_success
            "failed" -> R.drawable.ic_failed
            "running" -> R.drawable.ic_running
            else -> R.drawable.ic_canceled
        }
        binding.content.latestCommit.pipeline.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                icon
            )
        )
        Connection(this).Users().getAvatar(commit.authorEmail)
        Connection(this).Project().getStats(project.id)
    }

    /**
     * When an avatar is recieved after being requested in showLatestCommit()
     * @param url [String]
     * @author Jack Devey
     */

    private fun showCommitAvatar(url: String) {
        binding.content.latestCommit.avatarList.load(url) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
    }

    /**
     * Show the stats of the project
     * @param projectStats [ProjectStats]
     * @author Jack Devey
     */

    private fun showStats(projectStats: ProjectStats) {
        val commits = projectStats.commits
        val repoSize = projectStats.repositorySize
        val repoSizeStr = Helpful().humanReadableByteCountSI(repoSize.toLong()).toString()
        val issues = project.issues
        val branch = project.defaultBranch
        val infoList: MutableList<String> = mutableListOf()
        infoList.add("{ 'left' : 'Issues', 'right' : '$issues', 'icon' : 'issue' }")
        infoList.add("{ 'left' : 'Branch', 'right' : '$branch', 'icon' : 'branch' }")
        infoList.add("{ 'left' : 'View Files', 'right' : '$repoSizeStr', 'icon' : 'file' }")
        infoList.add("{ 'left' : 'Commits', 'right' : '$commits', 'icon' : 'commit' }")
        val infoListAdapter = InfoListAdapter(this@ProjectAct, infoList.toTypedArray())
        binding.content.infoList.adapter = infoListAdapter
        binding.content.infoList.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                val obj = JSONObject(selectedItem)
                when (obj.getString("left")) {
                    "Issues" -> {
                        val intent = Intent(applicationContext, Issues::class.java)
                        val bundle = Bundle()
                        bundle.putInt("id", project.id)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                    "Commits" -> {
                        val intent = Intent(applicationContext, Commits::class.java)
                        val bundle = Bundle()
                        bundle.putInt("id", project.id)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                    "View Files" -> {
                        val intent = Intent(applicationContext, FileViewer::class.java)
                        val bundle = Bundle()
                        bundle.putInt("id", project.id)
                        bundle.putString("path", "")
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                    "Branch" -> {
                        val intent = Intent(applicationContext, BranchSelector::class.java)
                        val bundle = Bundle()
                        bundle.putInt("id", project.id)
                        bundle.putString("branch", project.defaultBranch)
                        intent.putExtras(bundle)
                        startActivityForResult(intent, 0)
                    }
                }
            }
        //This is the last thing to load so
        showAll()
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}