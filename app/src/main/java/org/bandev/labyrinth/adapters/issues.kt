package org.bandev.labyrinth.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.Color.rgb
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import androidx.core.graphics.toColorLong
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso
import org.bandev.labyrinth.CircleTransform
import org.bandev.labyrinth.R
import org.bandev.labyrinth.RoundedTransform
import org.json.JSONObject

class issues(private val context: Activity, private val text: Array<String?>) :
    BaseAdapter() {
    @SuppressLint("ResourceType")
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater

        val rowView = inflater.inflate(R.layout.issues_list_view, null)
        val name = rowView.findViewById(R.id.name) as TextView
        val visibility = rowView.findViewById(R.id.desc) as TextView

        val jsonObj = JSONObject(text[p0])
        name.text = jsonObj.getString("title")
        visibility.text = "#" + jsonObj.getString("iid") + " | Created by " +  jsonObj.getJSONObject("author").getString("name")

        var status: Chip = rowView.findViewById(R.id.status)

        if(jsonObj.getString("state") == "opened"){
            status.text = "Open"
            status.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.open))

        }else if(jsonObj.getString("state") == "closed"){
            status.text = "Closed"
            status.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.closed))
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