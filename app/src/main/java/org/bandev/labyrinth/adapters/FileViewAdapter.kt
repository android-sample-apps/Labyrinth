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
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.androidnetworking.common.ANResponse
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.bandev.labyrinth.CircleTransform
import org.bandev.labyrinth.R
import org.bandev.labyrinth.databinding.FileViewAdapterBinding
import org.json.JSONObject


class FileViewAdapter(private val context: Activity, private val text: Array<String?>) :
    BaseAdapter() {


    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater


        val rowView = inflater.inflate(R.layout.file_view_adapter, null)

        val name = rowView.findViewById<TextView>(R.id.name)
        val icon = rowView.findViewById<ImageView>(R.id.icon)

        val jsonObj = JSONObject(text[p0] ?: return null)
        name.text = jsonObj.getString("name")
        when(jsonObj.getString("type")) {
            "tree" -> icon.setImageResource(R.drawable.ic_folder)
            "blob" -> icon.setImageResource(R.drawable.ic_paper)
        }







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