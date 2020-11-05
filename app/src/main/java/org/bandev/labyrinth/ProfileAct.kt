package org.bandev.labyrinth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter


class ProfileAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_act)

        val toolbar:androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.back)

        val pref = getSharedPreferences("User", 0)

        val avatar = findViewById<ImageView>(R.id.avatar)
        Picasso.get().load(pref?.getString("avatarUrl", "null")).transform(RoundedTransform(30, 0)).into(avatar)

        val usernameTextView: TextView = findViewById(R.id.usernmame)
        val emailTextView: TextView = findViewById(R.id.email)
        val descriptionTextView: TextView = findViewById(R.id.description)
        val locationTextView: TextView = findViewById(R.id.location)

        usernameTextView.text = pref.getString("username", "null")
        emailTextView.text = pref.getString("email", "null")
        descriptionTextView.text = pref.getString("bio", "null")
        locationTextView.text = pref.getString("location", "null")

        val userGroups = getSharedPreferences("User-Groups", 0)

        val listView = findViewById<ListView>(R.id.groupsList)

        var i = 0
        val list: MutableList<String?> = mutableListOf()
        while(i != userGroups.getInt("numGroups", 0)){
            if(i == 3){
                break
            }
            list.add(userGroups.getString(i.toString(), "null"))
            i++
        }

        val adapter = GroupOrProjectListAdapter(this, list.toTypedArray())
        listView.adapter = adapter
        listView.divider = null
        justifyListViewHeightBasedOnChildren(listView)


        val projectLists = getSharedPreferences("User-Projects", 0)

        val listViewProjects = findViewById<ListView>(R.id.projectsList)

        var i2 = 0
        val list2: MutableList<String?> = mutableListOf()
        while(i2 != projectLists.getInt("numProjects", 0)){
            list2.add(projectLists.getString(i2.toString(), "null"))
            i2++
        }

        val adapter2 = GroupOrProjectListAdapter(this, list2.toTypedArray())
        listViewProjects.adapter = adapter2
        listViewProjects.divider = null
        justifyListViewHeightBasedOnChildren(listViewProjects)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open -> {
                val pref = getSharedPreferences("User", 0)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(pref.getString("webUrl", "https://gitlab.com"))
                startActivity(i)
                super.onOptionsItemSelected(item)
            }
            R.id.settings -> {

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