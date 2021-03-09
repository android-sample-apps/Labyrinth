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

        val image = when(jsonObj.getString("icon")){
            "settings" -> R.drawable.ic_settings
            "about" -> R.drawable.ic_info
            "branch" -> R.drawable.ic_branch
            "merge" -> R.drawable.ic_mr_16
            "issue" -> R.drawable.ic_issues
            "commit" -> R.drawable.ic_commit
            "file" -> R.drawable.ic_file
            "repo" -> R.drawable.ic_repo
            "groups" -> R.drawable.ic_groups
            "status" -> R.drawable.ic_status
            "secure" -> R.drawable.ic_secure
            "email" -> R.drawable.ic_email
            else -> R.drawable.ic_key
        }

        icon.setImageResource(image)



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