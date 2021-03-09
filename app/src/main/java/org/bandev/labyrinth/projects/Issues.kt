package org.bandev.labyrinth.projects

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.google.android.material.tabs.TabLayoutMediator
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.adapters.IssueAdapter
import org.bandev.labyrinth.databinding.ProjectsIssuesBinding
import org.bandev.labyrinth.databinding.ProjectsIssuesFragmentBinding
import org.json.JSONArray

class Issues : AppCompatActivity() {

    lateinit var binding: ProjectsIssuesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProjectsIssuesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_left)

        binding.pager.adapter = FragmentAdapter(
            supportFragmentManager, lifecycle, (intent.extras
                ?: return).getInt("id")
        )
        binding.pager.setCurrentItem(0, false)

        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = when (position) {
                0 -> "Open"
                else -> "Closed"
            }
        }.attach()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_issues, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                intent.extras?.let { newIssue(it.getInt("id")) }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun newIssue(id: Int) {
        val intent = Intent(this, NewIssue::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    class FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, val id: Int) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> IssuesFragment.new(position, 0, id)
                else -> IssuesFragment.new(position, 1, id)
            }
        }
    }

    class IssuesFragment(val type: Int, val projectId: Int) : Fragment() {

        private var _binding: ProjectsIssuesFragmentBinding? = null
        private var profile: Profile = Profile()
        internal val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            setHasOptionsMenu(true)
            _binding = ProjectsIssuesFragmentBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

            binding.pull.setColorSchemeColors(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            binding.pull.setOnRefreshListener {
                getData()
                binding.pull.isRefreshing = false
            }
            getData()
        }

        fun getData() {
            binding.error.visibility = View.GONE
            binding.scroll.visibility = View.GONE
            binding.spinner.visibility = View.VISIBLE
            profile.login(requireContext(), 0)
            val list: MutableList<String> = mutableListOf()
            val token = profile.getData("token")
            val state = when (type) {
                0 -> "opened"
                else -> "closed"
            }
            AndroidNetworking.initialize(context)
            AndroidNetworking
                .get("https://gitlab.com/api/v4/projects/$projectId/issues?access_token=$token&state=$state")
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray?) {

                        for (i in 0 until (response ?: return).length()) {
                            list.add(response.getJSONObject(i).toString())
                        }

                        if (list.size == 0) {
                            error(state, 1)
                        } else binding.error.visibility = View.GONE

                        binding.listview.adapter =
                            IssueAdapter(requireActivity(), list.toTypedArray())
                        binding.listview.divider = null

                        binding.listview.onItemClickListener =
                            AdapterView.OnItemClickListener { parent, _, position, _ ->
                                val selectedItem = parent.getItemAtPosition(position) as String
                                val intent = Intent(context, IndividualIssue::class.java)
                                val bundle = Bundle()
                                bundle.putString("issueData", selectedItem)
                                intent.putExtras(bundle)
                                startActivity(intent)
                            }

                        binding.spinner.visibility = View.INVISIBLE
                        binding.scroll.visibility = View.VISIBLE
                    }

                    override fun onError(anError: ANError?) {
                        error(state, 0)
                    }
                })
        }

        fun error(state: String, type: Int) {
            binding.error.visibility = View.VISIBLE
            binding.spinner.visibility = View.INVISIBLE
            when (type) {
                1 -> {
                    binding.icon.setImageResource(R.drawable.ic_done)
                    binding.title.text = "You really showed those bugs"
                    binding.description.text = "There are no $state issues on this repository!"
                }
                else -> {
                    binding.icon.setImageResource(R.drawable.ic_error)
                    binding.title.text = "Something went wrong"
                    binding.description.text = "Try again or check your internet connection"
                }
            }
        }

        companion object {
            fun new(position: Int, type: Int, id: Int): IssuesFragment {
                val instance = IssuesFragment(type, id)
                val args = Bundle()
                args.putInt("position", position)
                instance.arguments = args
                return instance
            }
        }

    }
}