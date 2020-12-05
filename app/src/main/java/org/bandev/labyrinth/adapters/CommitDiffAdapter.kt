package org.bandev.labyrinth.adapters

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.chip.Chip
import io.noties.markwon.Markwon
import org.bandev.labyrinth.R
import org.json.JSONObject

class CommitDiffAdapter(private val context: Activity, private val text: Array<String?>) :
    BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater

        val rowView = inflater.inflate(R.layout.commit_diff_adapter, null)
        val body = rowView.findViewById(R.id.description) as TextView
        val creator = rowView.findViewById(R.id.creator) as TextView
        var date = rowView.findViewById<TextView>(R.id.time)

        val jsonObj = JSONObject(text[p0])

        body.text = jsonObj.getString("diff")
        creator.text = jsonObj.getString("new_path")




        return rowView
    }

    private fun getDateTime(s: String): String? {
        var arr = s.split("-")
        var arr2 = arr[2].split("T")
        return arr2[0] + "/" + arr[1] + "/" + arr[0]
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