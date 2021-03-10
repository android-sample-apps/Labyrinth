package org.bandev.labyrinth.projects

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.Notify
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.FileViewAdapter
import org.bandev.labyrinth.core.Animations
import org.bandev.labyrinth.core.Connection
import org.bandev.labyrinth.core.obj.Project
import org.bandev.labyrinth.databinding.FileViewerBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

/*

    FileViewer displays files in a listview layout,
    it makes a call to GitLab using
    https://gitlab.com/api/v4/projects/{id}/repository/tree?path={path}
    of which documentation can be found here
    https://docs.gitlab.com/ce/api/repositories.html#list-repository-tree.

    Writtern by Jack, 02/12/2020, added in initial release

*/

class FileViewer : AppCompatActivity() {

    lateinit var binding: FileViewerBinding
    lateinit var fileList: MutableList<String>
    private lateinit var rproject: Project
    var profile: Profile = Profile()
    private var path: String = ""
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize dependencies used in activity
        AndroidNetworking.initialize(applicationContext)

        //Set the layout file with view binding
        binding = FileViewerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Login user making profile.getData(key) active & set token variable
        profile.login(this, 0)
        token = profile.getData("token")

        //Get data from bundle passed with intent
        path = intent.extras?.getString("path").toString()

        //Configure Toolbar
        val toolbar: Toolbar = binding.toolbar
        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = backDrawable

        //Toolbar shadow animation
        Animations().toolbarShadowScroll(binding.scroll, toolbar)

        //Turn on edge to edge

        //Set title depending on folder
        binding.title.text = if (path == "") {
            "Root"
        } else {
            path
        }

        //Configure pull to refresh & make it run fillData()
        binding.pullToRefresh.setColorSchemeColors(
            ContextCompat.getColor(
                this,
                R.color.colorPrimary
            )
        )
        binding.pullToRefresh.setOnRefreshListener {
            newConn()
            binding.pullToRefresh.isRefreshing = false
        }
        newConn()
    }

    internal fun newConn() {
        val connection = Connection(this)
        connection.Project().get((intent.extras ?: return).getInt("id"))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyReceive(event: Notify) {
        when (event) {
            is Notify.ReturnProject -> fillData(event.project)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        //Close the instance when back arrow is pressed
        finish()
        return true
    }

    fun fillData(project: Project) {
        rproject = project
        //Hide all the content and show spinner
        hideAll()
        val repoId = project.id
        //Get JSONArray of files from GitLab
        AndroidNetworking
            .get("https://gitlab.com/api/v4/projects/$repoId/repository/tree")
            .addQueryParameter("access_token", token)
            .addQueryParameter("path", path)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(result: JSONArray) {
                    //Convert response to list
                    fileList = ArrayList()
                    for (i in 0 until result.length()) {
                        fileList.add(result.getJSONObject(i).toString())
                    }

                    //Create adapter, and configure listview
                    val adapter = FileViewAdapter(this@FileViewer, fileList.toTypedArray())
                    binding.listView.adapter = adapter
                    //Helpful().justifyListViewHeightBasedOnChildren(binding.listView)

                    //Set on click listener for listview, route to function itemClick(parent, position)
                    binding.listView.onItemClickListener =
                        AdapterView.OnItemClickListener { parent, _, position, _ ->
                            itemClick(parent, position)
                        }

                    //Show all the elements to user and remove spinner
                    showAll()
                }

                override fun onError(error: ANError) {
                    //Alert user that something went wrong, let them try again (fillData())
                    Snackbar.make(binding.root, R.string.project_fv_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.project_fv_error_retry) {
                            newConn()
                        }
                        .show()

                    //Show empty elements but remove spinner
                    showAll()
                }
            })
    }

    fun itemClick(parent: AdapterView<*>, position: Int) {
        //Work out what was pressed and send the user on their way
        val selectedItem = parent.getItemAtPosition(position) as String
        val intent = when (JSONObject(selectedItem).getString("type")) {
            "tree" -> Intent(applicationContext, FileViewer::class.java)
            else -> Intent(applicationContext, IndividualFileViewer::class.java)
        }
        val path = JSONObject(selectedItem).getString("path")
        val bundle = Bundle()
        bundle.putInt("id", rproject.id)
        bundle.putString("path", path)
        bundle.putString("branch", rproject.defaultBranch)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun hideAll() {
        //Hide all 'ugly' loading elements and just show spinner
        binding.listView.isGone = true
        binding.spinner.isGone = false
    }

    fun showAll() {
        //Show all elements now they have loaded, remove spinner
        binding.listView.isGone = false
        binding.spinner.isGone = true
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