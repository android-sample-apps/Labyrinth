package org.bandev.labyrinth.projects

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.mukesh.MarkdownView
import org.bandev.labyrinth.R
import org.bandev.labyrinth.core.Api
import org.json.JSONObject


class reduceStorageSize : AppCompatActivity() {


    var projectId: Int = 0
    var token: String = ""
    var projectPath = ""
    var webUrl = ""


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.projects_readme_act)

        projectId = intent.getIntExtra("projectId", 0)
        projectPath = intent.getStringExtra("projectPath")
        webUrl = intent.getStringExtra("webUrl")
        token = Api().getUserToken(this)


        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)


        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(R.color.colorPrimary)
        refresher.setOnRefreshListener {
            refresher.isRefreshing = false
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}