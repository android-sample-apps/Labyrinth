package org.bandev.labyrinth.core

import android.os.Build
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView

class Animations {
    fun toolbarShadowScroll(scroll: ScrollView, toolbar: Toolbar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scroll.setOnScrollChangeListener { _, _, _, _, _ ->
                toolbar.isSelected = scroll.canScrollVertically(-1)
            }
        }

    }

    fun toolbarShadowScroll(scroll: RecyclerView, toolbar: Toolbar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scroll.setOnScrollChangeListener { _, _, _, _, _ ->
                toolbar.isSelected = scroll.canScrollVertically(-1)
            }
        }

    }
}