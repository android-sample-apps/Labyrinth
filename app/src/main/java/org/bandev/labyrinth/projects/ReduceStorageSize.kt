package org.bandev.labyrinth.projects

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.bandev.labyrinth.R
import org.bandev.labyrinth.core.Api

class ReduceStorageSize : AppCompatActivity() {

    private var projectId: Int = 0
    private var token: String = ""
    private var projectPath = ""
    private var webUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.projects_readme_act)

        projectId = intent.getIntExtra("projectId", 0)
        projectPath = intent.getStringExtra("projectPath").toString()
        webUrl = intent.getStringExtra("webUrl").toString()
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