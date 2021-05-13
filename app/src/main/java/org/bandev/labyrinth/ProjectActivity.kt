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
import coil.ImageLoader
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.google.android.material.snackbar.Snackbar
import com.maxkeppeler.sheets.options.Option
import com.maxkeppeler.sheets.options.OptionsSheet
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.Connection
import org.bandev.labyrinth.core.Central
import org.bandev.labyrinth.core.Notify
import org.bandev.labyrinth.core.obj.Commit
import org.bandev.labyrinth.core.obj.Project
import org.bandev.labyrinth.databinding.ProjectActBinding
import org.bandev.labyrinth.projects.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


class ProjectActivity : AppCompatActivity() {

    private var latestCommit: View? = null
    private var progressBar: ProgressBar? = null
    private var infoBar: View? = null
    private var projectId = ""
    private var id: Int = 0
    private lateinit var project: Project
    private lateinit var imageLoader: ImageLoader

    private lateinit var binding: ProjectActBinding

    private var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProjectActBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // New image loader
        imageLoader = ImageLoader.Builder(this)
            .availableMemoryPercentage(0.25)
            .crossfade(true)
            .build()

        startActivity(Intent(this, NewProjectActivity::class.java))

        latestCommit = findViewById(R.id.options_container)
        progressBar = findViewById(R.id.progressBar)
        infoBar = findViewById(R.id.contentView4)

        id = (intent.extras ?: return).getInt("id")

        binding.content.avatar.load(R.color.browser_actions_bg_grey) {
            transformations(RoundedCornersTransformation(20f))
        }

        profile.login(this, 0)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }


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
        menu?.findItem(R.id.more)?.icon =
            IconicsDrawable(this, Octicons.Icon.oct_kebab_horizontal).apply {
                colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                sizeDp = 22
            }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.more) {
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
        }
    }

    /**
     * Show the options bottom sheet on menu click
     * @author Jack Devey
     */

    private fun showOptions() {
        val starDrawable = IconicsDrawable(this, Octicons.Icon.oct_star_fill)
        val forkDrawable = IconicsDrawable(this, Octicons.Icon.oct_git_fork)
        OptionsSheet().show(this) {
            title("Project Options")
            with(
                Option(starDrawable, "Star"),
                Option(forkDrawable, "Fork"),
                Option(R.drawable.ic_internet, "Open")
            )
            displayButtons(false)
            onPositive { index: Int, _: Option ->
                when (index) {
                    0 -> project.star()
                    1 -> project.fork()
                    2 -> project.openInBrowser(this@ProjectActivity)
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
        if (project.id == 22240804) binding.content.labyrinth.visibility = View.VISIBLE
        if (project.description != "") binding.content.description.text = project.description
        with(binding.content) {
            projectName.text = project.name
            slug.text = project.namespace
            stars.text = project.stars.toString()
            forks.text = project.forks.toString()
        }
        Central().loadAvatar(
            project.avatar,
            project.name,
            binding.content.avatar,
            RoundedCornersTransformation(30f),
            imageLoader,
            200,
            this
        )
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
            "success" -> IconicsDrawable(this, Octicons.Icon.oct_check_circle)
            "failed" -> IconicsDrawable(this, Octicons.Icon.oct_issue_opened)
            "running" -> IconicsDrawable(this, Octicons.Icon.oct_sync)
            else -> IconicsDrawable(this, Octicons.Icon.oct_circle_slash)
        }
        icon.colorInt = when (commit.status) {
            "success" -> ContextCompat.getColor(applicationContext, R.color.open)
            "failed" -> ContextCompat.getColor(applicationContext, R.color.failed)
            "running" -> ContextCompat.getColor(applicationContext, R.color.closed)
            else -> ContextCompat.getColor(applicationContext, R.color.textColorPrimary)
        }
        icon.sizeDp = 24
        binding.content.latestCommit.pipeline.setImageDrawable(icon)
        Connection(this).Users().getAvatar(commit.authorEmail)
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
        showStats()
    }

    /**
     * Show the stats of the project
     * @param projectStats [ProjectStats]
     * @author Jack Devey
     */

    private fun showStats() {
        val issues = project.issues
        val branch = project.defaultBranch
        val infoList: MutableList<String> = mutableListOf()
        infoList.add("{ 'left' : 'Issues', 'right' : '$issues', 'icon' : 'issue' }")
        infoList.add("{ 'left' : 'Merge Requests', 'right' : '', 'icon' : 'merge' }")
        infoList.add("{ 'left' : 'Branch', 'right' : '$branch', 'icon' : 'branch' }")
        infoList.add("{ 'left' : 'View Files', 'right' : '', 'icon' : 'file' }")
        infoList.add("{ 'left' : 'Commits', 'right' : '', 'icon' : 'commit' }")
        val infoListAdapter = InfoListAdapter(this@ProjectActivity, infoList.toTypedArray())
        binding.content.sshListView.adapter = infoListAdapter
        binding.content.sshListView.divider = null
        binding.content.sshListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, id ->
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
                    "Merge Requests" -> {
                        val intent = Intent(applicationContext, MergeRequests::class.java)
                        val bundle = Bundle()
                        bundle.putInt("id", project.id)
                        intent.putExtras(bundle)
                        startActivity(intent)
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