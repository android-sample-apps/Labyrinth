package org.bandev.labyrinth.projects

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.databinding.ProjectsNewIssueBinding
import org.json.JSONObject

class NewIssue : AppCompatActivity() {

    private lateinit var binding: ProjectsNewIssueBinding
    private var profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProjectsNewIssueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_left)

        profile.login(this, 0)
        binding.inTitle.performClick()
    }

    private fun send() {
        val id = intent.extras?.getInt("id")
        val title = binding.inTitle.text.toString()
        val description = binding.inDescription.text.toString()
        val token = profile.getData("token")
        var safe = true

        if (title == "") {
            binding.boxTitle.error = "Cant be empty!"
            safe = false
        }

        if (safe) {
            AndroidNetworking.initialize(this)
            AndroidNetworking.post("https://gitlab.com/api/v4/projects/$id/issues")
                .addHeaders("PRIVATE-TOKEN", token)
                .addQueryParameter("title", title)
                .addQueryParameter("description", description)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                    }

                    override fun onError(error: ANError) {
                        Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
                })

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_new_issue, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.post -> {
                send()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}