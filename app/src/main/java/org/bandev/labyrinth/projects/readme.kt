package org.bandev.labyrinth.projects

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.mukesh.MarkdownView
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.CircleTransform
import org.bandev.labyrinth.R
import org.bandev.labyrinth.RoundedTransform
import org.bandev.labyrinth.core.Api
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets.UTF_8


class readme : AppCompatActivity() {

    var projectId: Int = 0
    var token: String = ""


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.projects_readme_act)

        projectId = intent.getIntExtra("projectId", 0)
        token = Api().getUserToken(this)



        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        filldata()

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(R.color.colorPrimary)
        refresher.setOnRefreshListener {
            filldata()
            refresher.isRefreshing = false
        }

    }

    fun filldata(){
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking
                .get("https://gitlab.com/api/v4/projects/$projectId/repository/files/README.md?ref=master&token=$token")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        val dataBase64 = response?.getString("content")
                        val markdownView: MarkdownView = findViewById<View>(R.id.markdown_view) as MarkdownView
                        markdownView.setMarkDownText(String(Base64.decode(dataBase64, Base64.NO_WRAP), UTF_8))

                        val title: TextView = findViewById(R.id.title)
                        title.text = response?.getString("file_path")
                    }

                    override fun onError(error: ANError?) {
                        Toast.makeText(applicationContext, "Error retrieving readme!", LENGTH_LONG).show()
                    }

                })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}