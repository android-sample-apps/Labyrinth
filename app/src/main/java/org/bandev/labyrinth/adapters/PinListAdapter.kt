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
import coil.transform.RoundedCornersTransformation
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.bandev.labyrinth.R
import org.bandev.labyrinth.core.Type
import org.json.JSONObject

class PinListAdapter(private val context: Activity, private val text: Array<String?>) :
    BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater

        val rowView = inflater.inflate(R.layout.group_list_view, null)
        val name = rowView.findViewById(R.id.name) as TextView
        val visibility = rowView.findViewById(R.id.visibility) as TextView
        val avatar = rowView.findViewById(R.id.avatar_list) as ImageView

        val jsonObj = JSONObject(text[p0])
        val id = jsonObj.getString("iid")

        val url = when (jsonObj.getString("iid").toInt()) {
            Type.PROJECT -> "https://gitlab.com/api/v4/projects/$id"
            else -> ""
        }

        AndroidNetworking.initialize(context)
        AndroidNetworking.get(url)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    name.text = response.getString("name")
                    avatar.load(response.getString("avatar_url")) {
                        crossfade(true)
                        transformations(
                            RoundedCornersTransformation(
                                20f,
                                20f,
                                20f,
                                20f
                            )
                        )
                    }
                    visibility.text = response.getString("description")
                    if (jsonObj.getString("description") != "") {
                        visibility.text = jsonObj.getString("description")
                    } else {
                        visibility.text = "No description"
                    }
                }


                override fun onError(error: ANError?) {
                }
            })

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