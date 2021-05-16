package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.imageLoader
import org.bandev.labyrinth.activities.ProjectActivity
import org.bandev.labyrinth.core.*
import org.bandev.labyrinth.core.obj.Project
import org.bandev.labyrinth.databinding.ProjectsListActivityBinding
import org.bandev.labyrinth.recycleradapters.ProjectRecyclerAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * [ProjectsListActivity] is the activity for showing the
 * user's projects with a recyclerview
 * @author Jack Devey
 * @since v0.0.3
 * Notes:
 *  - First activity to be migrated to a RecyclerView
 *  - Uses EventBus return logic (first gen)
 */
class ProjectsListActivity : AppCompatActivity(), ProjectRecyclerAdapter.ClickListener {

    private lateinit var binding: ProjectsListActivityBinding
    private lateinit var connection: Connection
    private var globalVars = GlobalVars()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the view
        binding = ProjectsListActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Central().fitSystemBars(binding.root, window, binding.toolbar)

        // Setup the connection class
        connection = Connection(this)

        // Make a request
        makeRequest()

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon = globalVars.getBackDrawable(this)

        // Setup toolbar scroll animation
        Animations().toolbarShadowScroll(binding.recyclerView, binding.toolbar)

        // Setup the refresher layout
        globalVars.setupPullToRefresh(binding.swipeRefresh, this)
        binding.swipeRefresh.setOnRefreshListener {
            makeRequest()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    /**
     * When data is recieved from eventBus, i.e. the
     * data returned from the connection class
     * @param event [Notify]
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyReceive(event: Notify) {
        if (event is Notify.ReturnProjects) {
            showData(event.projectsList)
        }
    }

    /**
     * Show the data on the page
     * @param projectsList [MutableList]
     */
    private fun showData(projectsList: MutableList<Project>) {
        val recyclerAdapter = ProjectRecyclerAdapter(
            projectsList, imageLoader,
            this@ProjectsListActivity, this
        )
        with(binding.recyclerView) {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
        binding.spinner.visibility = View.INVISIBLE // Hide the spinner
        binding.recyclerView.visibility = View.VISIBLE // Show the content
    }

    /**
     * Make a request to GitLab for the user's projects
     */
    private fun makeRequest() {
        binding.spinner.visibility = View.VISIBLE // Show the spinner
        binding.recyclerView.visibility = View.INVISIBLE // Hide the content
        val type = intent.extras?.getInt("type") ?: return // The type
        val id = intent.extras?.getInt("id") ?: return // The user Id
        connection.Project().getCustom(
            when (type) {
                Type.PROJECTS_FROM_OTHER -> "users/$id/projects"
                Type.PROJECTS_FROM_GROUP -> "groups/$id/projects"
                else -> "projects?membership=true"
            }
        )
    }

    /**
     * Register the activity for eventbus
     */
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    /**
     * Unregister the activity for eventbus
     */
    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    /**
     * Close the activity
     */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    /**
     * On project pressed in RecyclerView
     * @param project [Project]
     */
    override fun onClick(project: Project) {
        val intent = Intent(this, ProjectActivity::class.java)
        intent.putExtra("fullPath", project.namespace)
        startActivity(intent)
    }
}