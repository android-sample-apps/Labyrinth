package org.bandev.labyrinth.core

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mikepenz.iconics.Iconics.applicationContext
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.R

class GlobalVars {
    fun getBackDrawable(context: Context): Drawable {
        return IconicsDrawable(context, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
    }

    fun setupPullToRefresh(refresher: SwipeRefreshLayout, context: Context) {
        refresher.setColorSchemeColors(ContextCompat.getColor(context, R.color.colorPrimary))
    }
}