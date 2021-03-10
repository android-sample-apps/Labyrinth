package org.bandev.labyrinth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import org.bandev.labyrinth.widgets.NonScrollListView
import org.json.JSONArray
import org.json.JSONObject

class PinSomething : AppCompatActivity() {

    var issueArryIn: JSONArray? = null
    private var repoObjIn: JSONObject? = null
    var token: String = ""
    var projectId: String = ""
    var listView: NonScrollListView? = null
    private var listView2: ListView? = null
    private var progressBar: ProgressBar? = null

    private var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pin_new_act)

        profile.login(this, 0)
        token = profile.getData("token")
        listView = findViewById(R.id.listView)
        progressBar = findViewById(R.id.progressBar2)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = backDrawable

        //Toolbar shadow animation
        val scroll = findViewById<ScrollView>(R.id.scroll)
        Animations().toolbarShadowScroll(scroll, toolbar)

        fillData()

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            fillData()
            refresher.isRefreshing = false
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
        var responseArray: JSONArray? = null
        //Get a list of issues from GitLab#
        var done = false
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

                    val adapter = GroupOrProjectListAdapter(context, list.toTypedArray())
                    (listView ?: return).adapter = adapter


                    (listView ?: return).onItemClickListener =
                        AdapterView.OnItemClickListener { parent, view, position, id ->
                            itemClick(parent, position)
                        }
                    done = true

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

    internal fun justifyListViewHeightBasedOnChildren(listView: ListView) {
        val adapter = listView.adapter ?: return
        val vg: ViewGroup = listView
        var totalHeight = 0
        for (i in 0 until adapter.count) {
            val listItem: View = adapter.getView(i, null, vg)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        val par = listView.layoutParams
        par.height = totalHeight + listView.dividerHeight * (adapter.count - 1)
        listView.layoutParams = par
        listView.requestLayout()
    }

    private fun hideAll() {
        listView?.isGone = true
        listView2?.isGone = true
        progressBar?.isGone = false
    }

    fun showAll() {
        listView?.isGone = false
        listView2?.isGone = false
        progressBar?.isGone = true
    }

}