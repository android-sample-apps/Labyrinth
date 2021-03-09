package org.bandev.labyrinth.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.bandev.labyrinth.core.obj.AccessToken
import org.bandev.labyrinth.databinding.TokensItemBinding

class TokenListAdapter(
    private val context: Activity,
    private val tokens: MutableList<AccessToken>
) : BaseAdapter() {

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val binding = TokensItemBinding.inflate(context.layoutInflater)
        val token = tokens[p0]
        binding.title.text = token.name

        return binding.root
    }

    override fun getItem(p0: Int): AccessToken = tokens[p0]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = tokens.size
}