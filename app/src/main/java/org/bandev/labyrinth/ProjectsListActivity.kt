package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.imageLoader
import org.bandev.labyrinth.core.Connection
import org.bandev.labyrinth.core.GlobalVars
import org.bandev.labyrinth.core.Notify
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

        // Setup the connection class
        connection = Connection(this)

        // Make a request
        makeRequest()

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon = globalVars.getBackDrawable(this)

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
    }

    /**
     * Make a request to GitLab for the user's projects
     */
    private fun makeRequest() {
        binding.spinner.visibility = View.VISIBLE // Show the spinner
        connection.Project().getAll()
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
        val intent = Intent(this, ProjectAct::class.java)
        intent.putExtra("id", project.id)
        startActivity(intent)
    }
}