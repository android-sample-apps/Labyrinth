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

        val drawable = when (jsonObj.getString("icon")) {
            "settings" -> IconicsDrawable(context, Octicons.Icon.oct_gear)
            "about" -> IconicsDrawable(context, Octicons.Icon.oct_info)
            "branch" -> IconicsDrawable(context, Octicons.Icon.oct_git_branch)
            "merge" -> IconicsDrawable(context, Octicons.Icon.oct_git_pull_request)
            "issue" -> IconicsDrawable(context, Octicons.Icon.oct_issue_opened)
            "commit" -> IconicsDrawable(context, Octicons.Icon.oct_git_commit)
            "file" -> IconicsDrawable(context, Octicons.Icon.oct_file_directory_fill)
            "repo" -> IconicsDrawable(context, Octicons.Icon.oct_file_submodule)
            "groups" -> IconicsDrawable(context, Octicons.Icon.oct_people)
            "status" -> IconicsDrawable(context, Octicons.Icon.oct_hubot)
            "secure" -> IconicsDrawable(context, Octicons.Icon.oct_lock)
            "email" -> IconicsDrawable(context, Octicons.Icon.oct_mail)
            else -> IconicsDrawable(context, Octicons.Icon.oct_key)
        }


        drawable.sizeDp = 16
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