package org.bandev.labyrinth.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.bandev.labyrinth.ProjectAct
import org.bandev.labyrinth.R
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.widgets.NonScrollListView

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val projectLists = requireContext().getSharedPreferences("User-Projects", 0)

        val listViewProjects = root.findViewById<NonScrollListView>(R.id.projects)

        var i2 = 0
        val list2: MutableList<String?> = mutableListOf()
        while (i2 != projectLists.getInt("numProjects", 0)) {
            list2.add(projectLists.getString(i2.toString(), "null"))
            i2++
        }

        val adapter2 = GroupOrProjectListAdapter(context as Activity, list2.toTypedArray())
        listViewProjects.adapter = adapter2
        listViewProjects.divider = null

        listViewProjects.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            val intent = Intent(context, ProjectAct::class.java)
            val bundle = Bundle()
            bundle.putString("data", selectedItem)
            intent.putExtras(bundle)
            startActivity(intent)
        }
        return root
    }

}