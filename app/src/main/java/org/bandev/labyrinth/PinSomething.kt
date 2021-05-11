package org.bandev.labyrinth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import coil.imageLoader
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.core.Animations
import org.bandev.labyrinth.databinding.ActivityPinNewBinding
import org.json.JSONArray

class PinSomething : AppCompatActivity() {

    private lateinit var binding: ActivityPinNewBinding
    var token: String = ""
    var projectId: String = ""

    private var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profile.login(this, 0)
        token = profile.getData("token")

        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon = backDrawable

        //Toolbar shadow animation
        Animations().toolbarShadowScroll(binding.scroll, binding.toolbar)

        fillData()

        with(binding.pullToRefresh) {
            setColorSchemeColors(ContextCompat.getColor(this@PinSomething, R.color.colorPrimary))
            setOnRefreshListener {
                fillData()
                binding.pullToRefresh.isRefreshing = false
            }
        }
    }

    fun itemClick(parent: AdapterView<*>, position: Int) {
        //Work out what was pressed and send the user on their way
        val selectedItem = parent.getItemAtPosition(position) as String
        val data = Intent()
        data.putExtra("newPin", selectedItem)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun fillData() {
        hideAll()
        val list: MutableList<String> = ArrayList()
        //Get a list of issues from GitLab#
        val context = this
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking
            .get("https://gitlab.com/api/v4/projects/?access_token=$token&membership=true")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?) {
                    for (i in 0 until (response ?: return).length()) {
                        list.add(response.getJSONObject(i).toString())
                    }

                    val recyclerAdapter =
                        GroupOrProjectListAdapter(context, list.toTypedArray(), imageLoader)
                    with(binding.listView) {
                        adapter = recyclerAdapter
                        divider = null
                        onItemClickListener =
                            AdapterView.OnItemClickListener { parent, view, position, id ->
                                itemClick(parent, position)
                            }
                    }

                    showAll()
                }

                override fun onError(anError: ANError?) {
                    Toast.makeText(context, "Error 1", LENGTH_SHORT).show()
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun hideAll() {
        with(binding) {
            listView.isGone = true
            progressBar.isGone = false
        }
    }

    fun showAll() {
        with(binding) {
            listView.isGone = false
            progressBar.isGone = true
        }
    }

}