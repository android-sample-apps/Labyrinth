package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.imageLoader
import org.bandev.labyrinth.core.*
import org.bandev.labyrinth.core.obj.Group
import org.bandev.labyrinth.databinding.GroupsListActivityBinding
import org.bandev.labyrinth.recycleradapters.GroupRecyclerAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * [GroupsListActivity] is the activity for showing the
 * user's groups with a recyclerview
 * @author Jack Devey
 * @since v0.0.3
 * Notes:
 *  - Uses EventBus return logic (first gen)
 */
class GroupsListActivity : AppCompatActivity(), GroupRecyclerAdapter.ClickListener {

    private lateinit var binding: GroupsListActivityBinding
    private lateinit var connection: Connection
    private var globalVars = GlobalVars()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the view
        binding = GroupsListActivityBinding.inflate(layoutInflater)
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
        if (event is Notify.ReturnGroups) {
            showData(event.groupsList)
        }
    }

    /**
     * Show the data on the page
     * @param groupsList [MutableList]
     */
    private fun showData(groupsList: MutableList<Group>) {
        val recyclerAdapter = GroupRecyclerAdapter(
            groupsList, imageLoader,
            this@GroupsListActivity, this
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
     * Make a request to GitLab for the user's groups
     */
    private fun makeRequest() {
        binding.spinner.visibility = View.VISIBLE // Show the spinner
        binding.recyclerView.visibility = View.INVISIBLE // Hide the content
        connection.Groups().getAll()
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
     * On group pressed in RecyclerView
     * @param group [Group]
     */
    override fun onClick(group: Group) {
        val intent = Intent(this, GroupsActivity::class.java)
        intent.putExtra("id", group.id)
        startActivity(intent)
    }
}