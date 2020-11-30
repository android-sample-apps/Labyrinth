package org.bandev.labyrinth

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.core.Api
import org.bandev.labyrinth.core.User
import org.json.JSONObject


class OthersProfileAct : AppCompatActivity() {

    val userData: HashMap<String, String> = HashMap()

    val profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.othersprofile_act)

        profile.login(this, 0)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        fillCommonData(intent.extras?.getString("data").toString())

        getData(intent.extras?.getString("data").toString())


        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        refresher.setOnRefreshListener {
            var token = profile.getData("token")
            Api().getUserGroups(this, token)
            Api().getUserProjects(this, token)
            filldata()
            refresher.isRefreshing = false
        }
    }

    fun fillCommonData(data: String) {
        val dataJson = JSONObject(data)
        val avatar = findViewById<ImageView>(R.id.avatar)
        val usernameTextView: TextView = findViewById(R.id.usernmame)
        val emailTextView: TextView = findViewById(R.id.email)

        Picasso.get().load(dataJson.getString("avatar_url"))
                .transform(RoundedTransform(90, 0))
                .into(avatar)

        usernameTextView.text = dataJson.getString("username")

        if (dataJson.has("email")) {
            emailTextView.text = dataJson.getString("email")
        } else {
            emailTextView.text = "No public email"
        }
    }

    fun getData(data: String) {
        val id = JSONObject(data).getInt("id")
        val token = User().getToken(applicationContext)


        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.get("https://gitlab.com/api/v4/users/$id?access_token=$token")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        userData.put("username", response.getString("username"))
                        userData.put("avatar_url", response.getString("avatar_url"))
                        userData.put("public_email", response.getString("public_email"))
                        userData.put("bio", response.getString("bio"))
                        userData.put("location", response.getString("location"))


                        filldata()
                    }

                    override fun onError(error: ANError?) {

                    }
                })
    }

    private fun filldata() {


        val baseProfile: View = findViewById(R.id.contentView)


        if (userData.get("bio") == "" && userData.get("location") == "") {
            //Hide Extended Profile


            baseProfile.background = ContextCompat.getDrawable(this, R.drawable.toolbar_line)

            val param = baseProfile.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0, 0, 0, 20)
            baseProfile.layoutParams = param

        } else {

        }

        val userGroups = getSharedPreferences("User-Groups", 0)

        val listView = findViewById<ListView>(R.id.groupsList)

        var i = 0
        val list: MutableList<String?> = mutableListOf()
        while (i != userGroups.getInt("numGroups", 0)) {
            list.add(userGroups.getString(i.toString(), "null"))
            i++
        }

        val adapter = GroupOrProjectListAdapter(this, list.toTypedArray())
        listView.adapter = adapter
        listView.divider = null
        justifyListViewHeightBasedOnChildren(listView)

        listView.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    val selectedItem = parent.getItemAtPosition(position) as String
                    val intent = Intent(this, GroupsAct::class.java)
                    val bundle = Bundle()
                    bundle.putString("data", selectedItem)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }

        val projectLists = getSharedPreferences("User-Projects", 0)

        val listViewProjects = findViewById<ListView>(R.id.projectsList)

        var i2 = 0
        val list2: MutableList<String?> = mutableListOf()
        while (i2 != projectLists.getInt("numProjects", 0)) {
            list2.add(projectLists.getString(i2.toString(), "null"))
            i2++
        }

        val adapter2 = GroupOrProjectListAdapter(this, list2.toTypedArray())
        listViewProjects.adapter = adapter2
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
        item.setVisible(false)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open -> {

                //FIX THIS PLS

                val url = "https://gitlab.com"
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