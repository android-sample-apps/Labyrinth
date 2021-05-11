package org.bandev.labyrinth.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import coil.ImageLoader
import coil.transform.CircleCropTransformation
import org.bandev.labyrinth.core.Central
import org.bandev.labyrinth.databinding.IssuesListViewBinding
import org.json.JSONObject


class CommitAdapterVague(
    private val context: Activity,
    private val text: Array<String?>,
    val imageLoader: ImageLoader
) :
    BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val binding = IssuesListViewBinding.inflate(context.layoutInflater, p2, false)

        val jsonObj = JSONObject(text[p0])

        binding.description.text = jsonObj.getString("title")
        binding.date.text = jsonObj.getString("author_name")
        Central().loadAvatar(
            "null",
            jsonObj.getString("author_name"),
            binding.avatar,
            CircleCropTransformation(),
            imageLoader,
            200,
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

    override fun getCount(): Int {
        return text.size
    }
}