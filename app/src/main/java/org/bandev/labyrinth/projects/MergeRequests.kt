package org.bandev.labyrinth.projects

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.bandev.labyrinth.adapters.MergeRequestAdapter
import org.bandev.labyrinth.core.obj.MergeRequest
import org.bandev.labyrinth.databinding.ProjectsIssuesFragmentBinding
import org.bandev.labyrinth.databinding.ProjectsMergeRequestsBinding
import org.json.JSONArray

class MergeRequests : AppCompatActivity() {

    lateinit var binding: ProjectsMergeRequestsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProjectsMergeRequestsBinding.inflate(layoutInflater)
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

    class FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, val id: Int) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> MergeRequestFragment.new(position, 0, id)
                else -> MergeRequestFragment.new(position, 1, id)
            }
        }
    }

    class MergeRequestFragment(val type: Int, val projectId: Int) : Fragment() {

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
            val list: MutableList<MergeRequest> = mutableListOf()
            val token = profile.getData("token")
            val state = when (type) {
                0 -> "opened"
                else -> "closed"
            }
            AndroidNetworking.initialize(context)
            AndroidNetworking
                .get("https://gitlab.com/api/v4/projects/$projectId/merge_requests?access_token=$token&state=$state")
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray) {

                        for (i in 0 until response.length()) list.add(
                            MergeRequest(response.getJSONObject(i))
                        )

                        if (list.size == 0) {
                            error(state, 1)
                        } else binding.error.visibility = View.GONE

                        binding.listview.adapter = MergeRequestAdapter(requireActivity(), list)
                        binding.listview.divider = null

                        binding.listview.onItemClickListener =
                            AdapterView.OnItemClickListener { parent, _, position, _ ->
                                val selectedItem =
                                    parent.getItemAtPosition(position) as MergeRequest
                                val intent = Intent(context, IndividualMR::class.java)
                                intent.putExtra("mr", selectedItem.json.toString())
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
                    binding.icon.setImageResource(R.drawable.ic_mr_24)
                    binding.title.text = "It's lonely here"
                    binding.description.text = "There are no $state merge requests"
                }
                else -> {
                    binding.icon.setImageResource(R.drawable.ic_error)
                    binding.title.text = "Something went wrong"
                    binding.description.text = "Try again or check your internet connection"
                }
            }
        }

        companion object {
            fun new(position: Int, type: Int, id: Int): MergeRequestFragment {
                val instance = MergeRequestFragment(type, id)
                val args = Bundle()
                args.putInt("position", position)
                instance.arguments = args
                return instance
            }
        }

    }
}