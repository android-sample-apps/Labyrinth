package org.bandev.labyrinth.core

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import coil.load
import coil.transform.RoundedCornersTransformation
import coil.transform.Transformation
import org.bandev.libraries.profilepicture.PFPView
import java.text.CharacterIterator
import java.text.StringCharacterIterator

class Central {
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
        transformations: Transformation,
        size: Int,
        context: Context
    ) {
        val placeholder = PFPView().draw(size, name, context)
        if (url != "null") {
            imageView.load(url) {
                transformations(transformations)
                crossfade(true)
                placeholder(placeholder)
            }
        } else {
            imageView.load(placeholder) {
                transformations(transformations)
                crossfade(true)
            }
        }

    }
}