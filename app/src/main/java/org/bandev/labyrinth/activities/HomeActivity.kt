package org.bandev.labyrinth.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.transform.CircleCropTransformation
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.core.Central
import org.bandev.labyrinth.databinding.HomeActivityBinding
import coil.imageLoader
import org.bandev.labyrinth.GroupsListActivity
import org.bandev.labyrinth.ProfileActivity
import org.bandev.labyrinth.ProjectsListActivity
import org.bandev.labyrinth.R
import org.bandev.labyrinth.apis.Explore
import org.bandev.labyrinth.core.Type
import org.bandev.labyrinth.recycleradapters.ProjectOnExploreAdapter

class HomeActivity : AppCompatActivity(), ProjectOnExploreAdapter.ClickListener {

    private lateinit var binding: HomeActivityBinding
    private lateinit var handler: Handler
    private lateinit var profile: Profile
    val explore: Explore = Explore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profile = Profile().login(this, 0)

        handler = Handler(Looper.getMainLooper())

        binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Central().loadAvatar(
            profile.getData("avatarUrl"),
            "Avatar",
            binding.profileAvatar,
            CircleCropTransformation(),
            imageLoader,
            200,
            this
        )

        binding.profileAvatar.setOnClickListener { openProfileActivity() }
        binding.optionProjects.setOnClickListener { openProjectsActivity() }
        binding.optionGroups.setOnClickListener { openGroupsActivity() }

        getExploreData()

        binding.swipeRefresh.setOnRefreshListener {
            getExploreData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun getExploreData() {
        explore.getProjects(10, profile.getData("token")) {
            handler.post { newExploreData(it) }
        }
    }

    private fun newExploreData(data: MutableList<Explore.ProjectOnExplore>) {
        with(binding.exploreRecycler) {
            adapter = ProjectOnExploreAdapter(data, imageLoader, this@HomeActivity, context)
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        binding.exploreTitle.text = getString(R.string.explore_title, explore.field)
    }

    override fun onClick(project: Explore.ProjectOnExplore) {
        val i = Intent(this, ProjectActivity::class.java)
            .putExtra("fullPath", project.fullPath)
        startActivity(i)
    }

    private fun openProfileActivity() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    private fun openProjectsActivity() {
        startActivity(
            Intent(this, ProjectsListActivity::class.java)
                .putExtra("type", Type.PROJECTS_FROM_USER)
                .putExtra("id", profile.getData("id").toInt())
        )
    }

    private fun openGroupsActivity() {
        startActivity(Intent(this, GroupsListActivity::class.java))
    }
}