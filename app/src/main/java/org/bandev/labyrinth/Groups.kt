package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.RoundedCornersTransformation
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.Connection
import org.bandev.labyrinth.core.Notify
import org.bandev.labyrinth.core.Type
import org.bandev.labyrinth.core.obj.Group
import org.bandev.labyrinth.databinding.GroupActBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class Groups : AppCompatActivity() {
    private lateinit var binding: GroupActBinding
    private lateinit var connection: Connection.Groups
    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = GroupActBinding.inflate(layoutInflater)
        id = (intent.extras ?: return).getInt("id")
        connection = Connection(this).Groups()
        setContentView(binding.root)

        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.navigationIcon = backDrawable

        connection.get(id)

        binding.pullToRefresh.setColorSchemeColors(
            ContextCompat.getColor(
                this,
                R.color.colorPrimary
            )
        )
        binding.pullToRefresh.setOnRefreshListener {
            connection.get(id)
            binding.pullToRefresh.isRefreshing = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyReceive(event: Notify) {
        when (event) {
            is Notify.ReturnGroup -> showData(event.group)
        }
    }

    private fun showData(group: Group) {
        binding.content.projectName.text = group.name
        binding.content.slug.text = group.id.toString()

        if (group.description != "")
            binding.content.description.text = group.description
        else
            binding.content.description.text = "No description"

        binding.content.avatar.load(group.avatar) {
            crossfade(true)
            transformations(RoundedCornersTransformation(20f))
        }

        if (group.id == 8650010) binding.content.contributor.visibility = View.VISIBLE

        val infoList = mutableListOf<String>()
        infoList.add("{ 'left' : 'Projects', 'right' : '', 'icon' : 'repo' }") //Id: 2

        binding.content.options.adapter = InfoListAdapter(this, infoList.toTypedArray())
        binding.content.options.divider = null

        binding.content.options.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val intent = Intent(applicationContext, ProjectsListActivity::class.java)
                intent.putExtra("type", Type.PROJECTS_FROM_GROUP)
                intent.putExtra("id", group.id)
                startActivity(intent)
            }
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