package org.bandev.labyrinth

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * [ProjectStatsChipGroup] manages the chips shown in the [ProjectActivity],
 * each chip assigned to the group should be given a tag to identify it later.
 * The [ChipGroup] will not show any [Chip]s more than once.
 */

class ProjectStatsChipGroup : ChipGroup {

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, atr: AttributeSet) : super(ctx, atr)
    constructor(ctx: Context, atr: AttributeSet, sty: Int) : super(ctx, atr, sty)

    // A MutableList of chip tags
    private var chips: MutableList<String> = mutableListOf()

    /**
     * Add a new [Chip]
     * @param [tag] String - The unique identifier of the chip
     * @param [stat] String - The statistic to show
     * @param [onClick] fun (chip) - When the chip is clicked
     * @return [Boolean], true if the chip was added, false if an error occured
     */
    fun add(tag: String, stat: String, onClick: (Chip) -> Unit): Boolean {
        val chip = Chip(context)
        with(chip) {
            text = stat
            chipIcon = getIcon(context, tag)
            setOnClickListener { onClick(this) }
        }
        if (!chips.contains(tag)) {
            this.addView(chip)
            chips.add(tag)
            return true
        }
        return false
    }

    /**
     * Flush all the [Chip]s from the view. This also
     * resets the chips list, so new chips can be added.
     */
    fun flush() {
        this.removeAllViews()
        chips.clear()
    }

    private fun getIcon(ctx: Context, tag: String): Drawable {
        return ContextCompat.getDrawable(
            ctx, when (tag) {
                "stars" -> R.drawable.ic_star_full
                "forks" -> R.drawable.ic_forks
                "tags" -> R.drawable.ic_tag
                "size" -> R.drawable.ic_code
                "commits" -> R.drawable.ic_commit
                else -> R.drawable.ic_alert
            }
        )!!
    }
}