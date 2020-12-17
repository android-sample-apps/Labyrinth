package org.bandev.labyrinth.ui.notifications

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.bandev.labyrinth.R
import org.bandev.labyrinth.adapters.TodoAdapter
import org.bandev.labyrinth.widgets.NonScrollListView

class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)

        val projectLists = requireContext().getSharedPreferences("User-Todos", 0)

        val listViewProjects = root.findViewById<NonScrollListView>(R.id.projects)

        var i2 = 0
        val list2: MutableList<String?> = mutableListOf()
        while (i2 != projectLists.getInt("numTodos", 0)) {
            list2.add(projectLists.getString(i2.toString(), "null"))
            i2++
        }

        val adapter2 = TodoAdapter(context as Activity, list2.toTypedArray())
        listViewProjects.adapter = adapter2
        if (projectLists.getInt("numTodos", 0) != 0) {
            listViewProjects.divider = null
        }

        listViewProjects.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String


            }




        return root
    }

}