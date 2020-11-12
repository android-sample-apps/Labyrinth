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
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.CircleTransform
import org.bandev.labyrinth.R
import org.bandev.labyrinth.RoundedTransform
import org.json.JSONObject

class issues(private val context: Activity, private val text: Array<String?>) :
    BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater

        val rowView = inflater.inflate(R.layout.issues_list_view, null)
        val name = rowView.findViewById(R.id.name) as TextView
        val visibility = rowView.findViewById(R.id.desc) as TextView

        val jsonObj = JSONObject(text[p0])
        name.text = jsonObj.getString("title")
        visibility.text = "#" + jsonObj.getString("iid") + " | Created by " +  jsonObj.getJSONObject("author").getString("name")




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
        editor.commit()
    }

    fun getDefaults(key: String?, context: Context?): String? {
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, null)
    }

    override fun getCount(): Int {
        return text.size
    }

}