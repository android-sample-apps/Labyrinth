package org.bandev.labyrinth.recycleradapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.bandev.labyrinth.core.obj.Info
import org.bandev.labyrinth.databinding.InfoRecyclerAdapterBinding

/**
 * Show a list of [Info] classes to the user.
 * @param infoList [MutableList] - The list of [Info] to display
 * @param clickListener [ClickListener]
 * @param context [Context] - The context of the activity
 * @author Jack Devey
 * @since v0.0.3
 */

class InfoRecyclerAdapter(
    private val infoList: MutableList<Info>,
    private val clickListener: ClickListener,
    private val context: Context
) : RecyclerView.Adapter<InfoRecyclerAdapter.ViewHolder>() {

    /** The ViewHolder for each item in the list
     * @param itemView [View]
     * @return [RecyclerView.ViewHolder]
     * */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val binding: InfoRecyclerAdapterBinding = InfoRecyclerAdapterBinding.bind(itemView)
        val textLeft: TextView = binding.textLeft
        val textRight: TextView = binding.textRight
        val icon: ImageView = binding.icon
        val end: ImageView = binding.end

        init {
            itemView.setOnClickListener(this)
        }

        /**
         * On item click
         * @param v [View]
         */
        override fun onClick(v: View?) {
            clickListener.onClick(infoList[bindingAdapterPosition])
        }
    }

    /** Setup the view for each part of the list
     * @param parent [ViewGroup]
     * @param viewType [Int]
     * @return [ViewHolder]
     * */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = InfoRecyclerAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding.root)
    }

    /** Bind the view for each part of the list
     * @param holder [ViewHolder]
     * @param position [Int]
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = infoList[position]
        // Set the text
        with(holder) {
            textLeft.text = info.leftText // The left text
            textRight.text = info.rightText // The right text
            icon.setImageDrawable(info.icon) // The left icon
            icon.setImageDrawable(info.end) // The end icon
        }
    }

    /** Count the items, literally just the size of the list */
    override fun getItemCount(): Int = infoList.size

    /** On item click listener **/
    interface ClickListener {
        fun onClick(info: Info)
    }
}