package org.bandev.labyrinth.adapters

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.coroutines.flow.merge
import org.bandev.labyrinth.R
import org.json.JSONObject

class InfoListAdapter(private val context: Activity, private val text: Array<String?>) :
    BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater

        val rowView = inflater.inflate(R.layout.info_view, null)
        val left = rowView.findViewById(R.id.textLeft) as TextView
        val right = rowView.findViewById(R.id.textRight) as TextView
        val icon = rowView.findViewById(R.id.icon) as ImageView

        val jsonObj = JSONObject(text[p0])
        left.text = jsonObj.getString("left")
        right.text = jsonObj.getString("right")

        val settingsDrawable =
            IconicsDrawable(context, Octicons.Icon.oct_gear).apply { sizeDp = 16 }
        val infoDrawable = IconicsDrawable(context, Octicons.Icon.oct_info).apply { sizeDp = 16 }
        val branchDrawable =
            IconicsDrawable(context, Octicons.Icon.oct_git_branch).apply { sizeDp = 16 }
        val mergeDrawable =
            IconicsDrawable(context, Octicons.Icon.oct_git_pull_request).apply { sizeDp = 16 }
        val issueDrawable =
            IconicsDrawable(context, Octicons.Icon.oct_issue_opened).apply { sizeDp = 16 }
        val commitDrawable =
            IconicsDrawable(context, Octicons.Icon.oct_git_commit).apply { sizeDp = 16 }
        val fileDrawable =
            IconicsDrawable(context, Octicons.Icon.oct_file_directory_fill).apply { sizeDp = 16 }
        val repoDrawable =
            IconicsDrawable(context, Octicons.Icon.oct_file_submodule).apply { sizeDp = 16 }
        val groupsDrawable =
            IconicsDrawable(context, Octicons.Icon.oct_people).apply { sizeDp = 16 }
        val statusDrawable = IconicsDrawable(context, Octicons.Icon.oct_hubot).apply { sizeDp = 16 }
        val secureDrawable = IconicsDrawable(context, Octicons.Icon.oct_lock).apply { sizeDp = 16 }
        val emailDrawable = IconicsDrawable(context, Octicons.Icon.oct_mail).apply { sizeDp = 16 }
        val keyDrawable = IconicsDrawable(context, Octicons.Icon.oct_key).apply { sizeDp = 16 }

        val drawable = when (jsonObj.getString("icon")) {
            "settings" -> settingsDrawable
            "about" -> infoDrawable
            "branch" -> branchDrawable
            "merge" -> mergeDrawable
            "issue" -> issueDrawable
            "commit" -> commitDrawable
            "file" -> fileDrawable
            "repo" -> repoDrawable
            "groups" -> groupsDrawable
            "status" -> statusDrawable
            "secure" -> secureDrawable
            "email" -> emailDrawable
            else -> keyDrawable
        }

        icon.setImageDrawable(drawable)

        return rowView

    }

    override fun getItem(p0: Int): String? {
        return text[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    fun setDefaults(key: String?, value: String?, context: Context?) {
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getDefaults(key: String?, context: Context?): String? {
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, null)
    }

    override fun getCount(): Int {
        return text.size
    }
}