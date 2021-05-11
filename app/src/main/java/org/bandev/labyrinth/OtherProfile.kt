package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.Connection
import org.bandev.labyrinth.core.Notify
import org.bandev.labyrinth.core.Type
import org.bandev.labyrinth.core.obj.User
import org.bandev.labyrinth.databinding.OtherProfileActBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class OtherProfile : AppCompatActivity() {
    private lateinit var binding: OtherProfileActBinding
    private lateinit var connection: Connection.Users
    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = OtherProfileActBinding.inflate(layoutInflater)
        id = (intent.extras ?: return).getInt("id")
        connection = Connection(this).Users()
        setContentView(binding.root)

        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.navigationIcon = backDrawable

        connection.get(id)

        with(binding.pullToRefresh) {
            setColorSchemeColors(ContextCompat.getColor(this@OtherProfile, R.color.colorPrimary))
            setOnRefreshListener {
                connection.get(id)
                binding.pullToRefresh.isRefreshing = false
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyReceive(event: Notify) {
        when (event) {
            is Notify.ReturnUser -> showData(event.user)
        }
    }

    private fun showData(user: User) {
        binding.content.projectName.text = user.name
        binding.content.slug.text = user.username

        if (user.bio != "")
            binding.content.description.text = user.bio
        else
            binding.content.description.text = "No description"

        if (user.location != "")
            binding.content.location.text = user.location
        else
            binding.content.location.visibility = View.GONE

        if (resources.getStringArray(R.array.contributors_id)
                .contains<String>(id.toString())
        ) binding.content.contributor.visibility = View.VISIBLE
        binding.content.avatar.load(user.avatar) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }

        val infoList = mutableListOf<String>()
        infoList.add("{ 'left' : 'Projects', 'right' : '', 'icon' : 'repo' }") // Id: 2

        with(binding.content.options) {
            adapter = InfoListAdapter(this@OtherProfile, infoList.toTypedArray())
            divider = null
            onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->
                    val intent = Intent(applicationContext, ProjectsListActivity::class.java)
                    intent.putExtra("type", Type.PROJECTS_FROM_OTHER)
                    intent.putExtra("id", user.id)
                    startActivity(intent)
                }
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