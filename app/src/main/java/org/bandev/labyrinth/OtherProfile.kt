package org.bandev.labyrinth

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.Connection
import org.bandev.labyrinth.core.obj.User
import org.bandev.labyrinth.databinding.OtherProfileActBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        connection.get(id)
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
        binding.content.description.text = user.bio
        binding.content.location.text = user.location
        binding.content.twitter.text = user.twitter
        binding.content.avatar.load(user.avatar) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }

        val infoList = mutableListOf<String>()
        infoList.add("{ 'left' : 'Groups', 'right' : '>', 'icon' : 'groups' }") //Id: 1
        infoList.add("{ 'left' : 'Projects', 'right' : '>', 'icon' : 'repo' }") //Id: 2

        binding.content.options.adapter = InfoListAdapter(this, infoList.toTypedArray())
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