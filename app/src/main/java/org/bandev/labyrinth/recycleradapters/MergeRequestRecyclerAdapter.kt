package org.bandev.labyrinth.recycleradapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import org.bandev.labyrinth.core.Central
import org.bandev.labyrinth.core.obj.MergeRequest
import org.bandev.labyrinth.databinding.MergeRequestRecyclerAdapterBinding

/**
 * Show a list of [MergeRequest] classes to the user.
 * @param mergeList [MutableList] - The list of Merge Requests to display
 * @param imageLoader [ImageLoader] - The imageLoader from the activity
 * @param clickListener [ClickListener]
 * @param context [Context] - The context of the activity
 * @author Jack Devey
 * @since v0.0.3
 */

class MergeRequestRecyclerAdapter(
    private val mergeList: MutableList<MergeRequest>,
    private val imageLoader: ImageLoader,
    private val clickListener: ClickListener,
    private val context: Context
) : RecyclerView.Adapter<MergeRequestRecyclerAdapter.ViewHolder>() {

    private val central: Central = Central() // Used to load avatars more efficiently

    /** The ViewHolder for each item in the list
     * @param itemView [View]
     * @return [RecyclerView.ViewHolder]
     * */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val binding: MergeRequestRecyclerAdapterBinding = MergeRequestRecyclerAdapterBinding.bind(itemView)
        val title: TextView = binding.title
        val subheading: TextView = binding.subheading
        val avatar: ImageView = binding.avatar

        init {
            itemView.setOnClickListener(this)
        }

        /**
         * On item click
         * @param v [View]
         */
        override fun onClick(v: View?) {
            clickListener.onClick(mergeList[adapterPosition])
        }
    }

    /** Setup the view for each part of the list
     * @param parent [ViewGroup]
     * @param viewType [Int]
     * @return [ViewHolder]
     * */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MergeRequestRecyclerAdapterBinding.inflate(
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
        val mergeRequest = mergeList[position]
        // Set the text
        with(holder) {
            title.text = mergeRequest.title // Merge Request name
            subheading.text = mergeRequest.author.username // Who made it
        }
        // Load the avatar using the avatar loader from central
        central.loadAvatar(
            mergeRequest.author.avatarUrl, // Avatar url
            mergeRequest.author.name, // Name for the placeholder
            holder.avatar, // ImageView to load into
            CircleCropTransformation(), // Crop into a circle
            imageLoader, // Use the global imageLoader
            100, // Only 100px, not too big ;)
            context // Context passed by activity
        )
    }

    /** Count the items, literally just the size of the list */
    override fun getItemCount(): Int = mergeList.size

    /** On item click listener **/
    interface ClickListener {
        fun onClick(mergeRequest: MergeRequest)
    }
}