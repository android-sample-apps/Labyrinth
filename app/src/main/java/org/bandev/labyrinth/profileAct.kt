package org.bandev.labyrinth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.adapters.groupOrProjectListAdapter
import org.bandev.labyrinth.core.api


class profileAct : AppCompatActivity() {

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_act)

        var toolbar:androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.back)

        filldata()

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
        var pref = getSharedPreferences("User", 0)

        var avatar = findViewById<ImageView>(R.id.avatar)
        Picasso.get().load(pref?.getString("avatarUrl", "null")).transform(RoundedTransform(30, 0)).into(avatar)

        var usernameTextView: TextView = findViewById(R.id.usernmame)
        var emailTextView: TextView = findViewById(R.id.email)
        var descriptionTextView: TextView = findViewById(R.id.description)
        var locationTextView: TextView = findViewById(R.id.location)

        usernameTextView.text = pref.getString("username", "null")
        emailTextView.text = pref.getString("email", "null")
        if(pref.getString("bio", "null") == ""){
            descriptionTextView.isGone = true
        }else{
            descriptionTextView.text = pref.getString("bio", "null")
        }
        if(pref.getString("location", "null") == ""){
            locationTextView.isGone = true
        }else{
            locationTextView.text = pref.getString("location", "null")
        }

        var userGroups = getSharedPreferences("User-Groups", 0)

        var listView = findViewById<ListView>(R.id.groupsList)

        var i = 0
        var list: MutableList<String?> = mutableListOf()
        while(i != userGroups.getInt("numGroups", 0)){
            if(i == 3){
                break;
            }
            list.add(userGroups.getString(i.toString(), "null"))
            i++;
        }

        val adapter = groupOrProjectListAdapter(this, list.toTypedArray())
        listView.adapter = adapter
        listView.divider = null
        justifyListViewHeightBasedOnChildren(listView);


        var projectLists = getSharedPreferences("User-Projects", 0)

        var listViewProjects = findViewById<ListView>(R.id.projectsList)

        var i2 = 0
        var list2: MutableList<String?> = mutableListOf()
        while(i2 != projectLists.getInt("numProjects", 0)){
            list2.add(projectLists.getString(i2.toString(), "null"))
            i2++;
        }

        val adapter2 = groupOrProjectListAdapter(this, list2.toTypedArray())
        listViewProjects.adapter = adapter2
        listViewProjects.divider = null
        justifyListViewHeightBasedOnChildren(listViewProjects);

        listViewProjects.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            var intent = Intent(this, projectAct::class.java)
            var bundle = Bundle()
            bundle.putString("data", selectedItem)
            intent.putExtras(bundle);
            startActivity(intent)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
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
            R.id.settings -> {
                val i = Intent(this, settingsAct::class.java)
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