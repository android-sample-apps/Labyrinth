package org.bandev.labyrinth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.adapters.groupOrProjectListAdapter
import org.bandev.labyrinth.core.api
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text


class projectAct : AppCompatActivity() {

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.project_act)

        filldata()

        var toolbar:androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.back)


        var refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(R.color.colorPrimary)
        refresher.setOnRefreshListener {
            val pref = getSharedPreferences("Settings", 0)
            val token = pref.getString("token", "null").toString()
            api().getUserGroups(this, token)
            api().getUserProjects(this, token)
            filldata()
            refresher.isRefreshing = false
        }

    }

    fun filldata(){
        var data = intent.getExtras()?.getString("data")

        var data_json = JSONObject(data)

        var nameTextView: TextView = findViewById(R.id.name_1)
        var slugTextView: TextView = findViewById(R.id.slug)
        var descriptionTextView: TextView = findViewById(R.id.description)
        var starsTextView: TextView = findViewById(R.id.stars)
        var forksTextView: TextView = findViewById(R.id.forks)

        var avatar = findViewById<ImageView>(R.id.avatar)
        if(data_json.getString("visibility") == "public"){
            Picasso.get().load(data_json.getString("avatar_url")).transform(RoundedTransform(90, 0)).into(avatar)
        }else{
            Picasso.get().load("file:///android_asset/lock.png").transform(RoundedTransform(90, 0)).into(avatar)
        }

        if(data_json.getString("star_count") != "1"){
            starsTextView.text = data_json.getString("star_count") + " Stars"
        }else{
            starsTextView.text = data_json.getString("star_count") + " Star"
        }

        if(data_json.getString("forks_count") != "1"){
            forksTextView.text = data_json.getString("forks_count") + " Forks"
        }else{
            forksTextView.text = data_json.getString("forks_count") + " Fork"
        }

        val projectId = data_json.getString("id")
        var pref = getSharedPreferences("User", 0)
        val token = pref.getString("token", "no")

        AndroidNetworking.initialize(this)
        AndroidNetworking.get("https://gitlab.com/api/v4/projects/$projectId/repository/commits?access_token=$token&order=topo")
                .build()
                .getAsJSONArray(object: JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray?){
                        var string = response?.get(0)?.toString()
                        var objec = JSONObject(string)

                        var commit_title = findViewById<TextView>(R.id.name)

                         commit_title.text = objec.getString("title")

                        var commit_title2 = findViewById<TextView>(R.id.visibility)

                        commit_title2.text = objec.getString("short_id")

                        val avatar = findViewById<ImageView>(R.id.avatar_list)

                        val em = objec.getString("committer_email")

                        AndroidNetworking.initialize(applicationContext)
                        AndroidNetworking.get("https://gitlab.com/api/v4/avatar?email=$em")
                                .build()
                                .getAsJSONObject(object: JSONObjectRequestListener {
                                    override fun onResponse(response: JSONObject?){

                                        Picasso.get().load(response!!.getString("avatar_url")).transform(CircleTransform()).into(avatar)


                                    }

                                    override fun onError(error: ANError?) {
                                        // handle error
                                        Toast.makeText(applicationContext, "Error retrieving committer avatar!", LENGTH_LONG).show()
                                    }

                                })


                        Log.d("em", objec.getString("committer_email"))
                    }

                    override fun onError(error: ANError?) {
                        // handle error
                        Toast.makeText(applicationContext, "error", LENGTH_LONG).show()
                    }

                })




        nameTextView.text = data_json.getString("name")
        slugTextView.text = data_json.getString("name_with_namespace")
        if(data_json.getString("description") != ""){
            descriptionTextView.text = data_json.getString("description")
        }else{
            descriptionTextView.isGone = true
        }




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.project_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open -> {
                var pref = getSharedPreferences("User", 0)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(pref.getString("webUrl", "https://gitlab.com"))
                startActivity(i)
                super.onOptionsItemSelected(item)
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
            totalHeight += listItem.getMeasuredHeight()
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