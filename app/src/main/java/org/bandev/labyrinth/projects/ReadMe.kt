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

class ReadMe : AppCompatActivity() {

    private var projectId: Int = 0
    private var token: String = ""
    var projectPath: String = ""
    var webUrl: String = ""


    @SuppressLint("ResourceAsColor")
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

        filldata()

        val refresher = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        refresher.setColorSchemeColors(R.color.colorPrimary)
        refresher.setOnRefreshListener {
            filldata()
            refresher.isRefreshing = false
        }
    }

    private fun filldata() {
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking
                .get("https://gitlab.com/api/v4/projects/$projectId/repository/files/README.md?ref=master&token=$token")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        val dataBase64 = response?.getString("content")
                        val markdownView: MarkdownView =
                                findViewById<View>(R.id.markdown_view) as MarkdownView

                        val text = String(Base64.decode(dataBase64, Base64.NO_WRAP))

                        val title: TextView = findViewById(R.id.title)
                        title.text = response?.getString("file_path")

                        val jsonParam = JSONObject()
                        jsonParam.put("text", text)
                        jsonParam.put("gfm", false)
                        jsonParam.put("project", projectPath)
                        Toast.makeText(applicationContext, webUrl, LENGTH_LONG).show()

                        AndroidNetworking.initialize(applicationContext)
                        AndroidNetworking
                                .post("https://gitlab.com/api/v4/markdown")
                                .addHeaders("Content-Type", "application/json")
                                .addJSONObjectBody(jsonParam)
                                .build()
                                .getAsJSONObject(object : JSONObjectRequestListener {
                                    override fun onResponse(response: JSONObject?) {
                                        val out = response?.getString("html")
                                        markdownView.setMarkDownText(out)
                                    }

                                    override fun onError(error: ANError?) {
                                        Toast.makeText(applicationContext, error.toString(), LENGTH_LONG)
                                                .show()
                                    }
                                })
                    }

                    override fun onError(anError: ANError?) {
                        TODO("Not yet implemented")
                    }
                })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}