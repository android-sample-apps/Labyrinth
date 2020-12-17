package org.bandev.labyrinth.projects

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import coil.load
import coil.transform.CircleCropTransformation
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.bandev.labyrinth.OthersProfileAct
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.CommitDiffAdapter
import org.bandev.labyrinth.core.Animations
import org.bandev.labyrinth.databinding.IndividualCommitBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class IndividualCommit : AppCompatActivity() {

    private var profile: Profile = Profile()
    private lateinit var binding: IndividualCommitBinding
    private var commitFullId: String = ""
    private var commitShortId: String = ""
    private var token: String = ""
    private var projectId: Int = 0
    lateinit var commitDataIn: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setup view binding
        binding = IndividualCommitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Login the user and set token variable
        profile.login(this, 0)
        token = profile.getData("token")

        //Set important variables from the brief data we got from gitlab
        commitDataIn = JSONObject(intent.extras?.getString("commitDataIn").toString())
        commitFullId = commitDataIn.getString("id")
        commitShortId = commitDataIn.getString("short_id")
        projectId = (intent.extras?.getInt("projectId") ?: return).toInt()

        //Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
        binding.title.text = commitShortId
        Animations().toolbarShadowScroll(binding.scroll, binding.toolbar  )

        //Setup pull to refresh on the activity
        binding.pullToRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        binding.pullToRefresh.setOnRefreshListener {
            fillData()
            binding.pullToRefresh.isRefreshing = false
        }

        //fillData() fills the page with data
        fillData()
    }

    private fun fillData() {
        //Hide all elements to make loading look smooth
        hideAll()
        //Make a list and begin filling data on response from GitLab
        val list: MutableList<String> = ArrayList()
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking
                .get("https://gitlab.com/api/v4/projects/{id}/repository/commits/{sha}/diff")
                .addPathParameter("id", projectId.toString())
                .addPathParameter("sha", commitFullId)
                .addQueryParameter("access_token", token)
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray?) {
                        for (i in 0 until (response ?: return).length()) {
                            list.add(response.getJSONObject(i).toString())
                        }

                        val adapter2 = CommitDiffAdapter(this@IndividualCommit, list.toTypedArray())
                        binding.content.listView.adapter = adapter2
                        binding.content.listView.divider = null

                        setProfilePicture(commitDataIn.getString("author_email"))
                    }

                    override fun onError(anError: ANError?) {
                        Toast.makeText(applicationContext, "Error 1", LENGTH_SHORT).show()
                    }
                })
    }

    fun setProfilePicture(email: String){
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking
                .get("https://gitlab.com/api/v4/avatar")
                .addQueryParameter("access_token", token)
                .addQueryParameter("email", email)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        //Response is successful, set the data we know
                        binding.content.avatar.load(response?.getString("avatar_url")) {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                            //Know set known commit data
                            setKnownData()
                        }

                    }

                    override fun onError(anError: ANError?) {
                        Toast.makeText(applicationContext, "Error getting profile picture", LENGTH_SHORT).show()
                        setKnownData()
                    }
                })
    }

    fun setKnownData() {
        binding.content.creator.text = commitDataIn.getString("author_name")
        binding.content.description.text = commitDataIn.getString("title")
        binding.content.time.text = getDateTime(commitDataIn.getString("created_at"))
        setMoreData()
    }

    private fun setMoreData() {
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking
                .get("https://gitlab.com/api/v4/projects/{id}/repository/commits/{sha}")
                .addPathParameter("id", projectId.toString())
                .addPathParameter("sha", commitFullId)
                .addQueryParameter("access_token", token)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        val pipeline = response.getString("status")
                        if(pipeline != "null"){
                            val icon = when (pipeline) {
                                "success" -> {
                                    R.drawable.ic_success
                                }
                                "failed" -> {
                                    R.drawable.ic_failed
                                }
                                "running" -> {
                                    R.drawable.ic_running
                                }
                                else -> {
                                    R.drawable.ic_canceled
                                }
                            }
                            with(binding.content.pipeline){
                                text = response.getString("status").capitalize(Locale.ROOT)
                                setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
                            }
                        }else{
                            binding.content.pipeline.isGone = true
                        }

                        showAll()
                    }

                    override fun onError(anError: ANError?) {
                        Toast.makeText(applicationContext, "Error getting more commit detail", LENGTH_SHORT).show()
                    }
                })
    }

    private fun getDateTime(s: String): String {
        val arr = s.split("-")
        val arr2 = arr[2].split("T")
        return arr2[0] + "/" + arr[1] + "/" + arr[0]
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_commit, menu)

        return true
    }

    private fun hideAll() {
        binding.content.toggle.isGone = true
        binding.progressBar.isGone = false
    }

    fun showAll() {
        binding.content.toggle.isGone = false
        binding.progressBar.isGone = true
    }

}