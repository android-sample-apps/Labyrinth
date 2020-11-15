package org.bandev.labyrinth.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.bandev.labyrinth.R
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.ProjectAct

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

        val listViewProjects = root.findViewById<ListView>(R.id.projects)

        var i2 = 0
        val list2: MutableList<String?> = mutableListOf()
        while (i2 != projectLists.getInt("numProjects", 0)) {
            list2.add(projectLists.getString(i2.toString(), "null"))
            i2++
        }

        val adapter2 = GroupOrProjectListAdapter(context as Activity, list2.toTypedArray())
        listViewProjects.adapter = adapter2
        listViewProjects.divider = null
        justifyListViewHeightBasedOnChildren(listViewProjects)

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

    private fun justifyListViewHeightBasedOnChildren(listView: ListView) {
        val adapter = listView.adapter ?: return
        val vg: ViewGroup = listView
        var totalHeight = 0
        for (i in 0 until adapter.count) {
            val listItem: View = adapter.getView(i, null, vg)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight + 10
        }
        val par = listView.layoutParams
        par.height = totalHeight + listView.dividerHeight * (adapter.count - 1)
        listView.layoutParams = par
        listView.requestLayout()
    }
}