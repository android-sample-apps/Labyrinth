package org.bandev.labyrinth

import android.content.Intent
import android.content.res.TypedArray
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.imageLoader
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import okhttp3.*
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.*
import org.bandev.labyrinth.core.obj.Info
import org.bandev.labyrinth.core.obj.ProjectQL
import org.bandev.labyrinth.databinding.ProjectActivityBinding
import org.bandev.labyrinth.projects.*
import org.bandev.labyrinth.recycleradapters.ProjectRecyclerAdapter
import org.json.JSONObject
import java.io.IOException

class ProjectActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private lateinit var binding: ProjectActivityBinding
    private lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fullPath = (intent.extras ?: return).getString("fullPath")

        // Set up binding and set content view
        binding = ProjectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the handler
        handler = Handler(Looper.getMainLooper())

        // Setup the imageloader
        imageLoader = ImageLoader.Builder(this)
            .availableMemoryPercentage(0.25)
            .crossfade(true)
            .build()

        // Use the profile class to get the token
        val profile = Profile()
        profile.login(this, 0)
        val token = profile.getData("token")

        // Setup toolbar & navigation icon
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.SetNavigateIcon(this)

        // Make sure the content is not visible when loading
        binding.refresher.visibility = View.INVISIBLE

        // Get a request using QueryCreator
        val req = GraphQLQueryCreator(token).getProject(fullPath ?: return)
        // Send a request to GitLab
        connect(req)

        // Setup refresh listener
        binding.refresher.setOnRefreshListener {
            connect(req)
            binding.refresher.isRefreshing = false
        }
    }

    fun showData(project: ProjectQL) {
        // Set data about the project
        binding.projectName.text = project.name
        binding.projectPath.text = project.fullPath
        binding.projectDescription.text =
            if (project.description != "") project.description
            else getString(R.string.no_description)

        if (project.hasCommits) {
            binding.projectChips.add(
                "commits",
                project.commits.toString()
            ) {
                showInfoSheet("commits", it)
            }
        }

        if (project.repoSize != 0.0) {
            binding.projectChips.add(
                "size",
                project.repoSize.nicefyBytes()
            ) {
                showInfoSheet("size", it)
            }
        }

        if (project.releases[0] != "null") {
            binding.projectChips.add(
                "tags",
                project.releases[0]
            ) {
                showInfoSheet("tags", it)
            }
        }

        binding.projectChips.add(
            "forks",
            project.forksCount.toString()
        ) {
            showInfoSheet("forks", it)
        }

        binding.projectChips.add(
            "stars",
            project.starCount.toString()
        ) {
            showInfoSheet("stars", it)
        }

        // Load the projects avatar
        Central().loadAvatar(
            project.avatarUrl,
            project.name,
            binding.projectAvatar,
            RoundedCornersTransformation(30f),
            imageLoader,
            200,
            this
        )

        if (project.hasCommits) {
            // Set the commit data
            binding.commitTitle.text = project.lastCommit.title
            binding.commitId.text = project.lastCommit.shortId

            // Load the commit author avatar
            Central().loadAvatar(
                project.lastCommit.author.avatarUrl,
                project.lastCommit.author.name,
                binding.commitAvatar,
                CircleCropTransformation(),
                imageLoader,
                100,
                this
            )

            // Decide on a pipeline icon depending on the status
            val pipelineIcon = getPipelineIcon(project.lastCommit.pipelines[0].status, this)
            binding.commitPipeline.setImageDrawable(pipelineIcon)
        }

        // Show the content and hide the spinner
        binding.refresher.visibility = View.VISIBLE
        binding.spinner.visibility = View.GONE
    }

    fun connect(req: Request) {
        OkHttpClient().newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                handler.post {
                    showData(ProjectQL(JSONObject((response.body ?: return@post).string())))
                }
            }
        })
    }

    // Close the activity on back pressed
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showInfoSheet(type: String, chip: Chip) {
        val sheet = ChipInfoSheet()
        sheet.show(supportFragmentManager, "CustomInfoSheet")

        sheet.icon = when (type) {
            "stars" -> R.drawable.ic_star_full
            "forks" -> R.drawable.ic_forks
            "tags" -> R.drawable.ic_tag
            "size" -> R.drawable.ic_code
            else -> R.drawable.ic_commit
        }

        sheet.statistic = chip.text.toString()

        sheet.description = getString(
            when (type) {
                "stars" -> R.string.stars_awarded
                "forks" -> R.string.forked_projects
                "tags" -> R.string.latest_tag
                "size" -> R.string.repository_size
                else -> R.string.commit_ammount
            }
        )
    }
}