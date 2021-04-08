package org.bandev.labyrinth.core

import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import coil.transform.Transformation
import org.bandev.libraries.profilepicture.PFPView
import java.text.CharacterIterator
import java.text.StringCharacterIterator

class Central {

    val pfpView = PFPView()

    fun humanReadableByteCountSI(bytes: Long): String? {
        var bytes = bytes
        if (-1000 < bytes && bytes < 1000) {
            return "$bytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000
            ci.next()
        }
        return java.lang.String.format("%.1f %cB", bytes / 1000.0, ci.current())
    }

    fun loadAvatar(
        url: String,
        name: String,
        imageView: ImageView,
        transformation: Transformation,
        imageLoader: ImageLoader,
        size: Int,
        context: Context
    ) {
        val request = ImageRequest.Builder(context)
            .crossfade(true)
            .target(imageView)
            .transformations(transformation)
            .data(
                if (url != "null") url
                else pfpView.draw(size, name, context)
            )
            .build()

        imageLoader.enqueue(request)
    }

    fun fitSystemBars(view: View, window: Window, toolbar: androidx.appcompat.widget.Toolbar) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val ins = insets.getInsets(WindowInsets.Type.statusBars())
                toolbar.updatePadding(top = ins.top)
            }
            insets
        }
    }
}