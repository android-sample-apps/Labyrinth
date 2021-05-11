package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.imageLoader
import coil.load
import coil.transform.RoundedCornersTransformation
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.*
import org.bandev.labyrinth.core.obj.Group
import org.bandev.labyrinth.core.obj.Member
import org.bandev.labyrinth.databinding.GroupActBinding
import org.bandev.labyrinth.recycleradapters.MemberRecyclerAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class GroupsActivity : AppCompatActivity(), MemberRecyclerAdapter.ClickListener {
    private lateinit var binding: GroupActBinding
    private lateinit var connection: Connection.Groups
    private var id: Int = 0
    private var globalVars = GlobalVars()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = GroupActBinding.inflate(layoutInflater)
        id = (intent.extras ?: return).getInt("id")
        connection = Connection(this).Groups()
        setContentView(binding.root)

        Animations().toolbarShadowScroll(binding.scrolly, binding.toolbar)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.navigationIcon = globalVars.getBackDrawable(this)

        sendRequest()

        binding.pullToRefresh.setColorSchemeColors(
            ContextCompat.getColor(
                this,
                R.color.colorPrimary
            )
        )
        binding.pullToRefresh.setOnRefreshListener {
            sendRequest()
        }
    }

    private fun sendRequest() {
        connection.get(id)
        connection.getMembers(id)
        binding.pullToRefresh.isRefreshing = false
        binding.membersContainer.visibility = View.GONE
        binding.optionsContainer.visibility = View.GONE
        binding.top.visibility = View.GONE
        binding.loader.visibility = View.VISIBLE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyReceive(event: Notify) {
        when (event) {
            is Notify.ReturnGroup -> showData(event.group)
            is Notify.ReturnMembers -> showMembers(event.membersList)
        }
    }

    private fun showData(group: Group) {
        binding.projectName.text = group.name
        binding.slug.text = group.id.toString()

        if (group.description != "")
            binding.description.text = group.description
        else
            binding.description.text = getString(R.string.no_description)

        binding.avatar.load(group.avatar) {
            crossfade(true)
            transformations(RoundedCornersTransformation(20f))
        }

        if (group.id == 8650010) binding.contributor.visibility = View.VISIBLE

        val infoList = mutableListOf<String>()
        infoList.add("{ 'left' : 'Projects', 'right' : '', 'icon' : 'repo' }") //Id: 2

        binding.options.adapter = InfoListAdapter(this, infoList.toTypedArray())
        binding.options.divider = null

        binding.options.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val intent = Intent(applicationContext, ProjectsListActivity::class.java)
                intent.putExtra("type", Type.PROJECTS_FROM_GROUP)
                intent.putExtra("id", group.id)
                startActivity(intent)
            }

        binding.optionsContainer.visibility = View.VISIBLE
        binding.top.visibility = View.VISIBLE
        binding.loader.visibility = View.GONE
    }

    private fun showMembers(membersList: MutableList<Member>) {
        val recyclerAdapter = MemberRecyclerAdapter(
            membersList, imageLoader,
            this@GroupsActivity, this
        )
        with(binding.membersRecyclerView) {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
        binding.membersContainer.visibility = View.VISIBLE
        binding.loader.visibility = View.GONE
    }

    override fun onClick(member: Member) {
        startActivity(
            Intent(this, OtherProfile::class.java)
                .putExtra("id", member.id)
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
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