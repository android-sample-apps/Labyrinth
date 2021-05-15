package org.bandev.labyrinth.core.obj

import android.graphics.drawable.Drawable
import org.bandev.labyrinth.recycleradapters.InfoRecyclerAdapter

/**
 * Data class for the [InfoRecyclerAdapter]
 */
data class Info(
    val icon: Drawable,
    val leftText: String,
    val rightText: String,
    val end: Drawable
)

