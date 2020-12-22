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
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.bandev.labyrinth.R
import org.json.JSONObject


class CommitAdapterVague(private val context: Activity, private val text: Array<String?>) :
    BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater

        val rowView = inflater.inflate(R.layout.commit_list_view_vague, null)
        val name = rowView.findViewById(R.id.title) as TextView
        val visibility = rowView.findViewById(R.id.date) as TextView
        val icon = rowView.findViewById<ImageView>(R.id.imageView2)

        val jsonObj = JSONObject(text[p0])

        name.text = jsonObj.getString("title")
        visibility.text = jsonObj.getString("author_name")
        val image = R.drawable.ic_commit

        icon.setImageResource(image)











        return rowView
    }

    fun getAvatar(email: String){
        val value = GlobalScope.async {
            AndroidNetworking.initialize(context)
            AndroidNetworking.get("https://gitlab.com/api/v4/avatar?email=$email")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        response.getString("avatar_url")

                    }

                    override fun onError(error: ANError?) {
                        // handle error
                    }

                })
            ""
        }

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