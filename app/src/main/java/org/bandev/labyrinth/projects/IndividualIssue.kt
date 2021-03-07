package org.bandev.labyrinth.projects

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import coil.transform.CircleCropTransformation
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import io.noties.markwon.Markwon
import org.bandev.labyrinth.OtherProfile
import org.bandev.labyrinth.OthersProfileAct
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.IssueNotesAdapter
import org.bandev.labyrinth.core.Animations
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.databinding.IndividualIssueBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class IndividualIssue : AppCompatActivity() {

    private lateinit var binding: IndividualIssueBinding
    private lateinit var issueData: JSONObject

    var token: String = ""
    var projectId: String = ""
    var listView: ListView? = null
    var listView2: ListView? = null
    private var progressBar: ProgressBar? = null

    private var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setup view binding
        binding = IndividualIssueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Login user making profile.getData(key) active & set token variable
        profile.login(this, 0)
        token = profile.getData("token")

        //Set issueData JSON depending on the data passed in to the activity
        issueData = JSONObject((intent.extras ?: return).getString("issueData").toString())

        //Set title, avatar, username etc
        binding.content.avatar.load(issueData.getJSONObject("author").getString("avatar_url")) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
        binding.content.creator.text = issueData.getJSONObject("author").getString("name")
        binding.title.text = "Issue #" + issueData.getInt("iid").toString()

        binding.content.creator.setOnClickListener{
            toPoster()
        }
        binding.content.avatar.setOnClickListener{
            toPoster()
        }

        //Set description in markdown renderor
        val markwon: Markwon = Markwon.create(this)
        markwon.setMarkdown(binding.content.description, issueData.getString("description"))

        //Work out and set likes
        val diff = issueData.getInt("upvotes") - issueData.getInt("downvotes")
        binding.content.likes.text = (if (diff >= 0) {
            "+$diff"
        } else {
            diff
        }).toString()

        //Work out time of publishing
        binding.content.time.text = getDateTime(issueData.getString("created_at"))

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        Compatibility().edgeToEdge(window, View(this), toolbar, resources)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        //Toolbar shadow animation
        Animations().toolbarShadowScroll(binding.scroll, toolbar)


        fillData()

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            fillData()
            refresher.isRefreshing = false
        }
    }

    private fun getDateTime(s: String): String {
        val arr = s.split("-")
        val arr2 = arr[2].split("T")
        return arr2[0] + "/" + arr[1] + "/" + arr[0]
    }

    private fun fillData() {
        hideAll()
        val list: MutableList<String> = ArrayList()
        //Get a list of issues from GitLab#
        val projectId2 = issueData.getInt("project_id").toString()
        val iid = issueData.getInt("iid").toString()

        AndroidNetworking.get("https://gitlab.com/api/v4/projects/$projectId2/issues/$iid/notes?sort=asc")
                .addQueryParameter("access_token", token)
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray) {
                        // do anything with response

                        for (i in 0 until response.length()) {
                            list.add(response.getJSONObject(i).toString())
                        }

                        val adapter2 = IssueNotesAdapter(this@IndividualIssue, list.toTypedArray())
                        binding.content.listView.adapter = adapter2
                        binding.content.listView.divider = null

                        showAll()

                    }

                    override fun onError(error: ANError) {
                        // handle error
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
        binding.content.listView.isGone = true
        progressBar?.isGone = false
    }

    fun showAll() {
        binding.content.listView.isGone = false
        progressBar?.isGone = true
    }

    private fun toPoster(){
        val i = Intent(this, OtherProfile::class.java)
        i.putExtra("id", issueData.getJSONObject("author").getInt("id"))
        startActivity(i)
    }

}