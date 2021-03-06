package org.bandev.labyrinth.core

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import org.bandev.labyrinth.R

class Compatibility {

    /**
     * Sets navigation bar based off android version
     * @param [context] context of activity (Context)
     * @param [window] context of window (Window)
     * @param [resources] context of resources (Resources)
     * @author Fennec_exe
     * @added [1008] v1.5.0 - 29/10/2020
     */

    fun setSystemBars(context: Context, window: Window, resources: Resources) {
        when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    window.navigationBarColor = Color.TRANSPARENT
                } else {
                    window.navigationBarColor =
                        ResourcesCompat.getColor(
                            resources,
                            R.color.design_default_color_primary_dark,
                            null
                        )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                window.navigationBarColor = Color.TRANSPARENT
            }
        }
    }

/*    fun edgeToEdge(window: Window, view: View, toolbar: androidx.appcompat.widget.Toolbar, resources: Resources) {

        WindowCompat.setDecorFitsSystemWindows(window, false)

        view.setOnApplyWindowInsetsListener { v, insets ->
            insets.isConsumed
            insets
        }

        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen",
                "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        val param = toolbar
        toolbar.setMargins(0, statusBarHeight, 0, 0)
        toolbar.layoutParams = param
    }*/

    fun edgeToEdge2(
        window: Window,
        view: View,
        toolbar: androidx.appcompat.widget.Toolbar,
        resources: Resources
    ) {

        WindowCompat.setDecorFitsSystemWindows(window, false)

        view.setOnApplyWindowInsetsListener { v, insets ->
            insets.isConsumed
            insets
        }

        var statusBarHeight = 0
        val resourceId = resources.getIdentifier(
            "status_bar_height", "dimen",
            "android"
        )
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        toolbar.setPadding(10, statusBarHeight, 15, 0)
    }
}