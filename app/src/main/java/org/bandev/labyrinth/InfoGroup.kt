package org.bandev.labyrinth

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ListView
import org.bandev.labyrinth.adapters.InfoListAdapter

class InfoGroup : ListView {

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, atr: AttributeSet) : super(ctx, atr)
    constructor(ctx: Context, atr: AttributeSet, sty: Int) : super(ctx, atr, sty)

    private var items: MutableList<Item> = mutableListOf()

    fun addItem(left: String, right: String, icon: Int): Boolean =
        items.add(Item(left, right, icon))

    data class Item(
        val left: String,
        val right: String,
        val icon: Int
    )




}