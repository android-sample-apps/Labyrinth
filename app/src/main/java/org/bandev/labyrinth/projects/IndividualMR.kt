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
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import io.noties.markwon.Markwon
import org.bandev.labyrinth.OtherProfile
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.IssueNotesAdapter
import org.bandev.labyrinth.core.Animations
import org.bandev.labyrinth.core.obj.MergeRequest
import org.bandev.labyrinth.databinding.IndividualMrBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class IndividualMR : AppCompatActivity() {

    private lateinit var binding: IndividualMrBinding
    private lateinit var mr: MergeRequest

    var listView: ListView? = null

    private var profile: Profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setup view binding
        binding = IndividualMrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Login user making profile.getData(key) active & set token variable
        profile.login(this, 0)

        val data = JSONObject(intent.extras?.getString("mr").toString())
        mr = MergeRequest(data)

        //Set title, avatar, username etc
        binding.content.avatar.load(mr.author.avatarUrl) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
        binding.content.creator.text = mr.author.name
        binding.title.text = "Merge Request !" + mr.iid

        binding.content.creator.setOnClickListener {
            toPoster()
        }
        binding.content.avatar.setOnClickListener {
            toPoster()
        }

        //Set description in markdown renderor
        val markwon: Markwon = Markwon.create(this)
        markwon.setMarkdown(binding.content.description, mr.descripton)

        //Work out and set likes
        val diff = mr.upVotes - mr.downVotes
        binding.content.likes.text = (if (diff >= 0) {
            "+$diff"
        } else {
            diff
        }).toString()

        //Work out time of publishing
        binding.content.time.text = getDateTime(mr.createdAt)

        //Setup toolbar
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

        AndroidNetworking.get("https://gitlab.com/api/v4/projects/{id}/merge_requests/{iid}/notes?sort=asc")
            .addQueryParameter("access_token", profile.getData("token"))
            .addPathParameter("id", mr.projectId.toString())
            .addPathParameter("iid", mr.iid.toString())
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    // do anything with response

                    for (i in 0 until response.length()) {
                        list.add(response.getJSONObject(i).toString())
                    }

                    val adapter2 = IssueNotesAdapter(this@IndividualMR, list.toTypedArray())
                    binding.content.listView.adapter = adapter2
                    binding.content.listView.divider = null

                    showAll()

                }

                override fun onError(error: ANError) {
                    binding.progressBar.isGone = true
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
        binding.progressBar.isGone = false
    }

    fun showAll() {
        binding.content.listView.isGone = false
        binding.progressBar.isGone = true
    }

    private fun toPoster() {
        val i = Intent(this, OtherProfile::class.java)
        i.putExtra("id", mr.author.id)
        startActivity(i)
    }

}