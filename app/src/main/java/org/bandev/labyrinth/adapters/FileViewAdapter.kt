package org.bandev.labyrinth.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.bandev.labyrinth.databinding.FileViewAdapterBinding
import org.json.JSONObject


class FileViewAdapter(private val context: Activity, private val text: Array<String>) :
    BaseAdapter() {


    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val binding = FileViewAdapterBinding.inflate(context.layoutInflater)

        val jsonObj = JSONObject(text[p0])
        binding.title.text = jsonObj.getString("name")

        return binding.root
    }

    override fun getItem(p0: Int): String {
        return text[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return text.size
    }
}