package org.bandev.labyrinth

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import coil.transform.RoundedCornersTransformation
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.databinding.OthersprofileActBinding
import org.json.JSONArray
import org.json.JSONObject

class OthersProfileAct : AppCompatActivity() {

    val userData: HashMap<String, String> = HashMap()
    private val profile: Profile = Profile()
    private lateinit var binding: OthersprofileActBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setup view binding
        binding = OthersprofileActBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Logs the user into our profile API
        profile.login(this, 0)

        //Sets up toolbar & sets navigation icons
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        //Fills the data we already know from the activity
        getData(intent.extras?.getString("data").toString())


        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            val token = profile.getData("token")
            Api().getUserGroups(this, token)
            Api().getUserProjects(this, token)
            filldata()
            refresher.isRefreshing = false
        }
    }

    private fun getData(data: String) {
        val id = JSONObject(data).getInt("id")

        //Contact GitLab then set the userData hashmap with our data
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.get("https://gitlab.com/api/v4/users/$id")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        userData["username"] = response.getString("username")
                        userData["avatar_url"] = response.getString("avatar_url")
                        userData["email"] = response.getString("public_email")
                        userData["bio"] = response.getString("bio")
                        userData["location"] = response.getString("location")
                        userData["id"] = response.getInt("id").toString()
                        filldata()
                    }

                    override fun onError(error: ANError?) {
                        Toast.makeText(
                                applicationContext,
                                "Error while fetching profile from GitLab. Try Again Later",
                                LENGTH_SHORT).show()
                    }
                })
    }

    internal fun filldata() {

        binding.content.avatar.load(userData["avatar_url"]) {
            crossfade(true)
            transformations(RoundedCornersTransformation(20f))
        }

        //Set their username
        binding.content.username.text =  userData["username"]

        //Check if they have an email
        if (userData["email"] != "") {
            //Yep, show it
            binding.content.email.text = userData["email"]
        } else {
            //Nope show something else
            binding.content.email.text = "No public email"
        }

        //Check if they have a bio
        if (userData["bio"] != "") {
            //Yep, show it
            binding.content.description.text = userData["bio"]
        } else {
            //Nope show something else
            binding.content.description.text = "No description"
        }

        //Check if they have a location
        if (userData["location"] != "") {
            //Yep, show it
            binding.content.location.text = userData["location"]
        } else {
            //Nope show something else
            binding.content.location.text = "Worldwide"
        }

        val id = userData["id"]

        //Contact GitLab then set the userData hashmap with our data
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.get("https://gitlab.com/api/v4/users/$id/projects")
                .addQueryParameter("access_token", profile.getData("token"))
                .addQueryParameter("statistics", "true")
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray) {
                        val list: MutableList<String> = ArrayList()
                        var index = 0
                        while (index != response.length()) {
                            list.add(response[index].toString())
                            index++
                        }

                        binding.content.projectsList.adapter = GroupOrProjectListAdapter(
                                this@OthersProfileAct,
                                list.toTypedArray())

                        binding.content.projectsList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                            val selectedItem = parent.getItemAtPosition(position) as String
                            val intent = Intent(applicationContext, ProjectAct::class.java)
                            val bundle = Bundle()
                            bundle.putString("data", selectedItem)
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }

                    }

                    override fun onError(error: ANError?) {
                        Toast.makeText(
                                applicationContext,
                                "Error while fetching profile from GitLab. Try Again Later",
                                LENGTH_SHORT).show()
                    }
                })


        val projectLists = getSharedPreferences("User-Projects", 0)

        val listViewProjects = findViewById<ListView>(R.id.projectsList)

        var i2 = 0
        val list2: MutableList<String?> = mutableListOf()
        while (i2 != projectLists.getInt("numProjects", 0)) {
            list2.add(projectLists.getString(i2.toString(), "null"))
            i2++
        }

        //val adapter2 = GroupOrProjectListAdapter(this, list2.toTypedArray())
        //listViewProjects.adapter = adapter2
        listViewProjects.divider = null
        justifyListViewHeightBasedOnChildren(listViewProjects)

        listViewProjects.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    val selectedItem = parent.getItemAtPosition(position) as String
                    val intent = Intent(this, ProjectAct::class.java)
                    val bundle = Bundle()
                    bundle.putString("data", selectedItem)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        val item = menu!!.findItem(R.id.settings)
        item.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open -> {
                val url = "https://gitlab.com/" + userData["username"]
                val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
                builder.setToolbarColor(Color.parseColor("#0067f4"))
                val customTabsIntent: CustomTabsIntent = builder.build()
                customTabsIntent.launchUrl(this, Uri.parse(url))
                super.onOptionsItemSelected(item)
            }
            R.id.settings -> {
                val i = Intent(this, SettingsAct::class.java)
                startActivity(i)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun justifyListViewHeightBasedOnChildren(listView: ListView) {
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}