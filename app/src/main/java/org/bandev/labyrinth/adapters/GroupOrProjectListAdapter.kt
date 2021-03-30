package org.bandev.labyrinth.adapters

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import coil.ImageLoader
import coil.transform.RoundedCornersTransformation
import org.bandev.labyrinth.core.Central
import org.bandev.labyrinth.databinding.GroupListViewBinding
import org.json.JSONObject

class GroupOrProjectListAdapter(private val context: Activity, private val text: Array<String?>, val imageLoader: ImageLoader) :
    BaseAdapter() {

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val binding = GroupListViewBinding.inflate(context.layoutInflater, p2, false)
        val json = JSONObject(text[p0])

        binding.name.text = json.getString("name")
        binding.visibility.text = (if (json.getString("description") != "") {
            json.getString("description")
        } else "No description").toString()
        Central().loadAvatar(
            json.getString("avatar_url"),
            json.getString("name"),
            binding.avatarList,
            RoundedCornersTransformation(20f),
            imageLoader,
            100,
            context
        )
        return binding.root
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