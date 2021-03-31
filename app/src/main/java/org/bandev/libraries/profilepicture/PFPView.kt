package org.bandev.libraries.profilepicture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat.getColor
import org.bandev.labyrinth.R

class PFPView {
    fun draw(size: Int, name: String, context: Context): BitmapDrawable {
        // Get the first letter from the name of the entity,
        // e.g. "labyrinth" -> 'L'
        val letter = name[0].toUpperCase()

        // Create a bitmap with the height given as a parameter
        val bitmap: Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        // Use bitmap to create a bitmap
        val canvas = Canvas(bitmap)

        // Draw the background colour to the canvas
        canvas.drawColor(getColorFromLetter(letter, context))

        // Stylise the text, center it & set the textSize to 3/5 of the height
        val textPaint = Paint()
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = (size * 0.6).toFloat()
        textPaint.color = Color.WHITE
        textPaint.isFakeBoldText = true

        // Calculate the coordinates of where to show the text
        val x = (size / 2).toFloat()
        val y = (size / 2 - (textPaint.descent() + textPaint.ascent()) / 2)

        // Draw the text to the canvas
        canvas.drawText(name[0].toString().toUpperCase(), x, y, textPaint)

        // Return it as a bitmap drawable
        return BitmapDrawable(context.resources, bitmap)
    }

    private fun getColorFromLetter(letter: Char, context: Context): Int {
        return getColor(
            context, (when (letter) {
                'A', 'N' -> R.color.red_700
                'B', 'O' -> R.color.pink_600
                'C', 'P' -> R.color.purple_400
                'D', 'Q' -> R.color.deep_purple_400
                'E', 'R' -> R.color.indigo_400
                'F', 'S' -> R.color.blue_700
                'G', 'T' -> R.color.light_blue_800
                'H', 'U' -> R.color.cyan_900
                'I', 'V' -> R.color.teal_700
                'J', 'W' -> R.color.green_800
                'K', 'X' -> R.color.light_green_900
                'L', 'Y' -> R.color.brown_600
                'M', 'Z' -> R.color.deep_orange_900
                else -> R.color.blue_grey_600
            })
        )
    }
}