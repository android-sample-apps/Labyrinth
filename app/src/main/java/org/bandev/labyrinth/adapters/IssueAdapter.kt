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
import coil.load
import coil.transform.CircleCropTransformation
import org.bandev.labyrinth.R
import org.json.JSONObject

class IssueAdapter(private val context: Activity, private val text: Array<String?>) :
    BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater

        val rowView = inflater.inflate(R.layout.issues_list_view, null)
        val name = rowView.findViewById(R.id.description) as TextView
        val visibility = rowView.findViewById(R.id.date) as TextView

        val avatar = rowView.findViewById<ImageView>(R.id.avatar)
        val jsonObj = JSONObject(text[p0])

        avatar.load(jsonObj.getJSONObject("author").getString("avatar_url")) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }


        name.text = jsonObj.getString("title")
        visibility.text =
            "#" + jsonObj.getString("iid") + " | Created by " + jsonObj.getJSONObject("author")
                .getString("name")


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