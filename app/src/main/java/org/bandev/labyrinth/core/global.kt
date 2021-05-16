package org.bandev.labyrinth.core

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.R
import kotlin.coroutines.coroutineContext
import kotlin.math.round

fun androidx.appcompat.widget.Toolbar.SetNavigateIcon(context: Context) {
    this.navigationIcon =
        IconicsDrawable(context, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(context, R.color.colorPrimary)
            sizeDp = 16
        }
}

fun getPipelineIcon(status: String, context: Context): Drawable {
    val icon = when (status) {
        "SUCCESS" -> IconicsDrawable(context, Octicons.Icon.oct_check_circle)
        "FAILED" -> IconicsDrawable(context, Octicons.Icon.oct_issue_opened)
        "RUNNING" -> IconicsDrawable(context, Octicons.Icon.oct_sync)
        else -> IconicsDrawable(context, Octicons.Icon.oct_circle_slash)
    }
    icon.colorInt = when (status) {
        "SUCCESS" -> ContextCompat.getColor(context, R.color.open)
        "FAILED" -> ContextCompat.getColor(context, R.color.failed)
        "RUNNING" -> ContextCompat.getColor(context, R.color.closed)
        else -> ContextCompat.getColor(context, R.color.textColorPrimary)
    }
    icon.sizeDp = 24
    return icon
}

/**
 * A function to draw behind system bars
 * @param [view] The view
 * @param [window] The window
 * @param [customInsets] The customInsets
 */
fun fitSystemBars(view: View, window: Window, customInsets: CustomInsets) {
    WindowCompat.setDecorFitsSystemWindows(window, false)

    ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
        customInsets.setCustomInsets(insets)
        insets
    }
}

/**
 * Custom Insets interface for each activity to get custom insets
 */

interface CustomInsets {
    fun setCustomInsets(insets: WindowInsetsCompat)
}

fun dpToPx(dp: Float, res: Resources): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        res.displayMetrics
    )
}

/**
 * Nicefy Bytes into human readable SI binary units
 * @author Jack Devey
 */

fun Float.nicefyBytes(iterations: Int = 0): String {
    val kBToB = 1024
    val prefixes = listOf("", "K", "M", "G", "P")
    return if (this < kBToB) {
        this.round(1).toString() + " " + prefixes[iterations] + "B"
    } else (this / kBToB).nicefyBytes(iterations + 1)
}

fun Float.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}