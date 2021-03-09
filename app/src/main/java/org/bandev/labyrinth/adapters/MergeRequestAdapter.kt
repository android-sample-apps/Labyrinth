package org.bandev.labyrinth.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import coil.load
import coil.transform.CircleCropTransformation
import org.bandev.labyrinth.core.obj.MergeRequest
import org.bandev.labyrinth.databinding.MergesItemBinding

class MergeRequestAdapter(
    private val context: Activity,
    private val list: MutableList<MergeRequest>
) : BaseAdapter() {

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val binding = MergesItemBinding.inflate(context.layoutInflater)
        val mr = list[p0]
        binding.title.text = mr.title
        binding.subheading.text = mr.author.username

        binding.avatar.load(mr.author.avatarUrl){
            crossfade(true)
            transformations(CircleCropTransformation())
        }

        return binding.root
    }

    override fun getItem(p0: Int): MergeRequest = list[p0]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = list.size
}