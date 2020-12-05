package org.bandev.labyrinth.core

import android.os.Build
import android.widget.ScrollView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar

class Animations {
    fun ToolbarShadowScroll(scroll: ScrollView, toolbar: Toolbar){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            scroll.setOnScrollChangeListener { _, _, _, _, _ ->
                toolbar.isSelected = scroll.canScrollVertically(-1)
            }
        }

    }
}