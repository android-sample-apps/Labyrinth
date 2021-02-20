package org.bandev.labyrinth.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.Fragment
import com.maxkeppeler.sheets.options.DisplayMode
import com.maxkeppeler.sheets.options.Option
import com.maxkeppeler.sheets.options.OptionsSheet
import org.bandev.labyrinth.PinSomething
import org.bandev.labyrinth.ProjectAct
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.account.activities.ProfileGroupsAct
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.Pins
import org.bandev.labyrinth.databinding.FragmentHomeBinding
import org.json.JSONObject

class HomeFragment : Fragment() {

    //Get the profile class setup
    var profile: Profile = Profile()

    //Setup view binding on this fragment [1/2]
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        //Setup binding on this fragment [2/2]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        //Login the user getting access to user data
        profile.login(requireContext(), 0)

        //Run code for top part
        top()

        //Run code for bottom part
        bottom()

        return binding.root
    }

    //All of the code that manages the 'top' part of the fragment
    private fun top() {
        //Setup top as the 'top' content only
        val top = binding.top

        //Create MutableList infoList, and fill each element with json data
        val infoList = mutableListOf<String>()
        infoList.add("{ 'left' : 'Issues', 'right' : '>', 'icon' : 'issue' }")  //Id: 0
        infoList.add("{ 'left' : 'Groups', 'right' : '>', 'icon' : 'groups' }") //Id: 1
        infoList.add("{ 'left' : 'Projects', 'right' : '>', 'icon' : 'repo' }") //Id: 2

        //Cast infoList to Array and send to InfoListAdapter to generate element for each item.
        top.infoListView.adapter = InfoListAdapter(requireActivity(), infoList.toTypedArray())

        //Handle clicks on infoList
        top.infoListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            //Find where to send the user based off the position of the click
            when (position) {
                1 -> {
                    //Send user to see their groups
                    val intent = Intent(context, ProfileGroupsAct::class.java)
                    intent.putExtra("type", 0)
                    startActivity(intent)
                }
                2 -> {
                    //Send user to see their repos
                    val intent = Intent(context, ProfileGroupsAct::class.java)
                    intent.putExtra("type", 1)
                    startActivity(intent)
                }
            }
        }
    }

    //All of the code that manages the 'bottom' part of the fragment
    private fun bottom() {
        //Setup bottom as the 'bottom' content only
        val bottom = binding.bottom

        //When add button is pressed, send user to activity showing what can be pinned
        bottom.add.setOnClickListener {
            val intent = Intent(context, PinSomething::class.java)
            startActivityForResult(intent, 0)
        }

        val pins = Pins(requireContext())
        bottom.infoListView.adapter = GroupOrProjectListAdapter(context as Activity, pins.data.toTypedArray())

        bottom.infoListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            val intent = Intent(context, ProjectAct::class.java)
            val bundle = Bundle()
            bundle.putInt("id", JSONObject(selectedItem).getInt("id"))
            intent.putExtras(bundle)
            startActivity(intent)
        }

        bottom.infoListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            showBottom(selectedItem)
            true
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            0 -> {
                val pins2 = Pins(requireContext())
                if(!pins2.exists(data!!.getStringExtra("newPin").toString())){
                    pins2.add(data!!.getStringExtra("newPin").toString())
                    pins2.save()
                    bottom()
                }else{
                    Toast.makeText(context, "Already is pinned", LENGTH_SHORT).show()
                }

            }
            // Other result codes
            else -> {
            }
        }
    }

    fun showBottom(data: String) {
        val datajs = JSONObject(data)
        OptionsSheet().show(requireContext()) {
            title(datajs.getString("name"))
            displayMode(DisplayMode.LIST)
            with(
                Option("Issues"),
                Option("View Files"),
                Option("Commits")
            )
            onPositive { index: Int, option: Option ->
                // Handle selected option
            }
        }
    }

}