package org.bandev.labyrinth.core.obj

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import org.bandev.labyrinth.core.Connection
import org.json.JSONObject

class Project(rawData: JSONObject, context: Context) {

    var id: Int = 0
    var name: String = ""
    var avatar: String = ""
    var namespace: String = ""
    var description: String = ""
    private var url: String = ""
    var defaultBranch: String = ""
    var stars: Int = 0
    var forks: Int = 0
    var issues: Int = 0

    private var projectConnection: Connection.Project

    init {
        id = rawData.getInt("id")
        name = rawData.getString("name")
        avatar = rawData.getString("avatar_url")
        namespace = rawData.getString("path_with_namespace")
        val localDesc = rawData.getString("description")
        description = if (localDesc != "") localDesc else "No Description"
        url = rawData.getString("web_url")
        defaultBranch = rawData.getString("default_branch")
        stars = rawData.getInt("star_count")
        forks = rawData.getInt("forks_count")
        issues = rawData.getInt("open_issues_count")

        projectConnection = Connection(context).Project()
    }

    /**
     * Fork the project
     * @author Jack Devey
     */

    fun fork(): Unit = projectConnection.fork(this)

    /**
     * Star or Unstar the project
     * @author Jack Devey
     */

    fun star(): Unit = projectConnection.star(this)

    /**
     * Open a project in [CustomTabsIntent] browser
     * @param context [Context]
     * @author Jack Devey
     */

    fun openInBrowser(context: Context) {
        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(Color.parseColor("#0067f4"))
        val customTabsIntent: CustomTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    fun getLastCommit(): Unit = projectConnection.getLastCommit(this)
}