package org.bandev.labyrinth.projects

import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import br.tiagohm.codeview.Language
import br.tiagohm.codeview.Theme
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.databinding.IndividualFileViewerBinding
import org.json.JSONObject
import java.net.URLEncoder

/*

    IndividualFileViewer displays files in a  layout,
    it makes a call to GitLab using
    https://gitlab.com/api/v4/projects/{id}/repository/files/{path}?ref={branch}
    of which documentation can be found here
    https://docs.gitlab.com/ee/api/repository_files.html#get-file-from-repository.

    Writtern by Jack, 03/12/2020, added in initial release

*/

class IndividualFileViewer : AppCompatActivity() {

    lateinit var binding: IndividualFileViewerBinding
    lateinit var fileInfo: JSONObject

    var profile: Profile = Profile()
    private var path: String = ""
    private var token: String = ""
    private var branch: String = ""
    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize dependencies used in activity
        AndroidNetworking.initialize(applicationContext)

        //Set the layout file with view binding
        binding = IndividualFileViewerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Login user making profile.getData(key) active & set token variable
        profile.login(this, 0)
        token = profile.getData("token")

        //Get data from bundle passed with intent
        id = (intent.extras?.getInt("id") ?: return)
        branch = (intent.extras?.getString("branch") ?: return)
        path = (intent.extras?.getString("path") ?: return)

        //Configure Toolbar
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

        //Turn on edge to edge
        Compatibility().edgeToEdge(window, View(this), toolbar, resources)

        //Set title depending on file path
        binding.title.text = path

        //Configure pull to refresh & make it run fillData()
        binding.pullToRefresh.setColorSchemeColors(
                ContextCompat.getColor(
                        this,
                        R.color.colorPrimary
                )
        )
        binding.pullToRefresh.setOnRefreshListener {
            fillData()
            binding.pullToRefresh.isRefreshing = false
        }

        //Fill the activity with some data
        fillData()
    }

    override fun onSupportNavigateUp(): Boolean {
        //Close the instance when back arrow is pressed
        finish()
        return true
    }

    fun fillData() {
        //Hide all the content and show spinner
        hideAll()

        //Get JSONArray of files from GitLab
        AndroidNetworking
                .get("https://gitlab.com/api/v4/projects/$id/repository/files/{path}")
                .addQueryParameter("access_token", token)
                .addQueryParameter("ref", branch)
                .addPathParameter("path", URLEncoder.encode(path, "utf-8"))
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(result: JSONObject) {
                        //Save response to fileInfo
                        fileInfo = result

                        //Convert base64 response to readble format
                        val dataBase64 = fileInfo.getString("content")
                        val code = String(Base64.decode(dataBase64, Base64.NO_WRAP))

                        println(code)

                        //Show code in codeView element & show element
                        binding.codeView
                                .setTheme(Theme.ANDROIDSTUDIO)
                                .setCode(code)
                                .setWrapLine(true)
                                .setFontSize(14F)
                                .setLanguage(Language.AUTO)
                                .setZoomEnabled(true)
                                .apply()

                        showAll()
                    }

                    override fun onError(error: ANError) {
                        //Alert user that something went wrong, let them try again (fillData())
                        Snackbar.make(binding.root, R.string.project_fv_error, Snackbar.LENGTH_LONG)
                                .setAction(R.string.project_fv_error_retry) {
                                    fillData()
                                }
                                .show()

                        //Show empty elements but remove spinner
                        showAll()
                    }
                })
    }

    private fun hideAll() {
        //Hide all 'ugly' loading elements and just show spinner
        binding.codeView.isGone = true
        binding.spinner.isGone = false
    }

    fun showAll() {
        //Show all elements now they have loaded, remove spinner
        binding.codeView.isGone = false
        binding.spinner.isGone = true
    }

}