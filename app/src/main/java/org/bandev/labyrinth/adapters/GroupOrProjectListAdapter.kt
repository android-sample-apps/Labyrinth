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
import org.bandev.labyrinth.R
import org.bandev.labyrinth.RoundedTransform
import org.json.JSONObject

class GroupOrProjectListAdapter(private val context: Activity, private val text: Array<String?>) :
        BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater

        val rowView = inflater.inflate(R.layout.group_list_view, null)
        val name = rowView.findViewById(R.id.name) as TextView
        val visibility = rowView.findViewById(R.id.visibility) as TextView
        val avatar = rowView.findViewById(R.id.avatar_list) as ImageView

        val jsonObj = JSONObject(text[p0])
        name.text = jsonObj.getString("name")
        if (jsonObj.getString("visibility") == "public") {
            visibility.text = "Public"
            Picasso.get().load(jsonObj.getString("avatar_url") + "?size=100")
                    .transform(RoundedTransform(90, 0)).into(avatar)
        } else {
            visibility.text = "Private"
            visibility.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_padlock, 0, 0, 0)
            Picasso.get().load("file:///android_asset/lock.png").transform(RoundedTransform(90, 0))
                    .into(avatar)
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