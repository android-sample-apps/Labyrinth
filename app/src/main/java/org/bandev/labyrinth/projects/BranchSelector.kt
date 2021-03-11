package org.bandev.labyrinth.projects

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.BranchSelectorAdapter
import org.bandev.labyrinth.core.Animations
import org.bandev.labyrinth.databinding.BranchSelectorBinding
import org.json.JSONArray
import org.json.JSONObject


/*

    BranchSelector allows the user to choose the branch they want to see,
    it makes a call to GitLab using
    https://gitlab.com/api/v4/projects/{id}/repository/branches
    of which documentation can be found here
    https://docs.gitlab.com/ee/api/branches.html#list-repository-branches.

    Writtern by Jack, 03/12/2020, added in initial release

*/

class BranchSelector : AppCompatActivity() {

    lateinit var binding: BranchSelectorBinding
    lateinit var branchList: MutableList<String>

    var profile: Profile = Profile()
    private var token: String = ""
    private var repoId: Int = 0
    private var branch: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize dependencies used in activity
        AndroidNetworking.initialize(applicationContext)

        //Set the layout file with view binding
        binding = BranchSelectorBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Login user making profile.getData(key) active & set token variable
        profile.login(this, 0)
        token = profile.getData("token")

        //Get data from bundle passed with intent
        repoId = (intent.extras?.getInt("id") ?: return)
        branch = (intent.extras?.getString("branch") ?: return)

        //Configure Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon =
            IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
                colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                sizeDp = 16
            }

        //Toolbar shadow animation
        Animations().toolbarShadowScroll(binding.scroll, binding.toolbar)

        //Turn on edge to edge

        //Set title depending on folder
        binding.title.text = "Select branch"

        //Configure pull to refresh & make it run fillData()
        binding.pullToRefresh.setColorSchemeColors(
            ContextCompat.getColor(
                this,
                R.color.colorPrimary
            )
        )
        binding.pullToRefresh.setOnRefreshListener {
            fillData()
            binding.pullToRefresh.isRefreshing = false
        }

        //Fill the activity with some data
        fillData()
    }

    override fun onSupportNavigateUp(): Boolean {
        //Close the instance when back arrow is pressed
        finish()
        return true
    }

    fun fillData() {
        //Hide all the content and show spinner
        hideAll()

        //Get JSONArray of files from GitLab
        AndroidNetworking
            .get("https://gitlab.com/api/v4/projects/{id}/repository/branches")
            .addQueryParameter("access_token", token)
            .addPathParameter("id", repoId.toString())
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(result: JSONArray) {
                    //Convert response to list
                    branchList = ArrayList()
                    for (i in 0 until result.length()) {
                        branchList.add(result.getJSONObject(i).toString())
                    }

                    //Create adapter, and configure listview
                    val branchSelectorAdapter = BranchSelectorAdapter(
                        this@BranchSelector,
                        branchList.toTypedArray(),
                        branch
                    )
                    with(binding.listView) {
                        adapter = branchSelectorAdapter
                        divider = null
                        //Helpful().justifyListViewHeightBasedOnChildren(binding.listView)

                        //Set on click listener for listview, route to function itemClick(parent, position)
                        onItemClickListener =
                            AdapterView.OnItemClickListener { parent, _, position, _ ->
                                itemClick(parent, position)
                            }
                    }

                    //Show all the elements to user and remove spinner
                    showAll()
                }

                override fun onError(error: ANError) {
                    //Alert user that something went wrong, let them try again (fillData())
                    Snackbar.make(binding.root, R.string.project_fv_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.project_fv_error_retry) {
                            fillData()
                        }.show()

                    //Show empty elements but remove spinner
                    showAll()
                }
            })
    }

    fun itemClick(parent: AdapterView<*>, position: Int) {
        //Work out what was pressed and send the user on their way
        val selectedItem = parent.getItemAtPosition(position) as String
        val data = Intent()
        data.putExtra("newBranch", JSONObject(selectedItem).getString("name"))
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun hideAll() {
        //Hide all 'ugly' loading elements and just show spinner
        with(binding) {
            listView.isGone = true
            spinner.isGone = false
        }
    }

    fun showAll() {
        //Show all elements now they have loaded, remove spinner
        with(binding) {
            listView.isGone = false
            spinner.isGone = true
        }
    }

}