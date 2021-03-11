package org.bandev.labyrinth


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.maxkeppeler.sheets.options.DisplayMode
import com.maxkeppeler.sheets.options.Option
import com.maxkeppeler.sheets.options.OptionsSheet
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.account.activities.ProfileGroupsAct
import org.bandev.labyrinth.adapters.GroupOrProjectListAdapter
import org.bandev.labyrinth.adapters.InfoListAdapter
import org.bandev.labyrinth.core.Pins
import org.bandev.labyrinth.databinding.MainActBinding
import org.bandev.labyrinth.projects.Commits
import org.bandev.labyrinth.projects.FileViewer
import org.bandev.labyrinth.projects.Issues
import org.json.JSONObject

class MainAct : AppCompatActivity() {

    private var profile: Profile = Profile()
    private lateinit var binding: MainActBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Login user
        profile.login(this, 0)

        //Set layout variables
        binding = MainActBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        with(binding.avatar) {
            load(profile.getData("avatarUrl")) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
            setOnClickListener {
                val intent = Intent(context, ProfileAct::class.java)
                startActivity(intent)
            }
        }

        top()
        bottom()

    }

    //All of the code that manages the 'top' part of the fragment
    private fun top() {
        //Setup top as the 'top' content only
        val top = binding.inner.top

        //Create MutableList infoList, and fill each element with json data
        val infoList = mutableListOf<String>()
        //infoList.add("{ 'left' : 'Issues', 'right' : '>', 'icon' : 'issue' }")  //Id: 0
        infoList.add("{ 'left' : 'Groups', 'right' : '>', 'icon' : 'groups' }") //Id: 1
        infoList.add("{ 'left' : 'Projects', 'right' : '>', 'icon' : 'repo' }") //Id: 2

        //Cast infoList to Array and send to InfoListAdapter to generate element for each item.
        top.infoListView.adapter = InfoListAdapter(this, infoList.toTypedArray())
        top.infoListView.divider = null
        //Handle clicks on infoList
        top.infoListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                //Find where to send the user based off the position of the click
                when (position) {
                    0 -> {
                        //Send user to see their groups
                        val intent = Intent(this, ProfileGroupsAct::class.java)
                        intent.putExtra("type", 0)
                        intent.putExtra("id", profile.getData("id").toInt())
                        startActivity(intent)
                    }
                    1 -> {
                        //Send user to see their repos
                        val intent = Intent(this, ProfileGroupsAct::class.java)
                        intent.putExtra("type", 1)
                        intent.putExtra("id", profile.getData("id").toInt())
                        startActivity(intent)
                    }
                }
            }
    }

    //All of the code that manages the 'bottom' part of the fragment
    private fun bottom() {
        //Setup bottom as the 'bottom' content only
        val bottom = binding.inner.bottom

        //When add button is pressed, send user to activity showing what can be pinned
        bottom.add.setOnClickListener {
            val intent = Intent(this, PinSomething::class.java)
            startActivityForResult(intent, 0)
        }

        val pins = Pins(this)
        bottom.infoListView.adapter = GroupOrProjectListAdapter(this, pins.data.toTypedArray())
        bottom.infoListView.divider = null
        bottom.infoListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                val intent = Intent(this, ProjectAct::class.java)
                val bundle = Bundle()
                bundle.putInt("id", JSONObject(selectedItem).getInt("id"))
                intent.putExtras(bundle)
                startActivity(intent)
            }

        bottom.infoListView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                showBottom(selectedItem, position)
                true
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            0 -> {
                val pins2 = Pins(this)
                if (!pins2.exists((data ?: return).getStringExtra("newPin").toString())) {
                    pins2.add(data.getStringExtra("newPin").toString())
                    pins2.save()
                    bottom()
                } else {
                    Toast.makeText(this, "Already is pinned", Toast.LENGTH_SHORT).show()
                }

            }
            // Other result codes
            else -> {
            }
        }
    }

    private fun showBottom(data: String, position: Int) {
        val datajs = JSONObject(data)
        val issuesDrawable = IconicsDrawable(this, Octicons.Icon.oct_issue_opened)
        val fileDrawable = IconicsDrawable(this, Octicons.Icon.oct_file_directory_fill)
        val commitDrawable = IconicsDrawable(this, Octicons.Icon.oct_git_commit)
        val deleteDrawable = IconicsDrawable(this, Octicons.Icon.oct_trash)
        OptionsSheet().show(this) {
            title(datajs.getString("name"))
            displayMode(DisplayMode.LIST)
            with(
                Option(issuesDrawable, "Issues"),
                Option(fileDrawable, "View Files"),
                Option(commitDrawable, "Commits"),
                Option(R.drawable.ic_internet, "Open"),
                Option(deleteDrawable, "Remove Pin")
            )
            onPositive { index: Int, option: Option ->
                // Handle selected option
                var isIntent = true

                if (index == 4) {
                    val pins = Pins(requireContext())
                    pins.remove(position + 1)
                    pins.save()
                    bottom()
                    isIntent = false
                }

                val act = when (index) {
                    0 -> Issues::class.java
                    1 -> FileViewer::class.java
                    2 -> Commits::class.java
                    else -> ProjectAct::class.java
                }

                if (isIntent) {
                    val intent = Intent(applicationContext, act)
                    intent.putExtra("id", datajs.getInt("id"))
                    intent.putExtra("branch", datajs.getString("default_branch"))
                    intent.putExtra("path", "")
                    startActivity(intent)
                }
            }
        }
    }
}