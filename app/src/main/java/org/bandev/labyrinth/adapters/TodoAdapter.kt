package org.bandev.labyrinth.adapters

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.bandev.labyrinth.R
import org.bandev.labyrinth.core.User
import org.json.JSONObject

class TodoAdapter(private val context: Activity, private val text: Array<String?>) :
    BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater

        val rowView = inflater.inflate(R.layout.todos_list_view, null)
        val name = rowView.findViewById(R.id.name) as TextView
        val visibility = rowView.findViewById(R.id.desc) as TextView

        val jsonObj = JSONObject(text[p0])
        name.text = jsonObj.getString("body")
        visibility.text = jsonObj.getString("state")

        if (jsonObj.getString("target_type") == "Issue") {
            if (jsonObj.getJSONObject("target").getJSONObject("assignee")
                    .getString("username") == User().getUsername(context)
            ) {
                visibility.text = "Assigned by you"
            } else {
                visibility.text =
                    "Assigned by" + jsonObj.getJSONObject("target").getJSONObject("assignee")
                        .getString("name")
            }
        }


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