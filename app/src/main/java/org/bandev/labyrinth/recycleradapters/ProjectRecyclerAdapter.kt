package org.bandev.labyrinth.recycleradapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.transform.RoundedCornersTransformation
import org.bandev.labyrinth.core.Central
import org.bandev.labyrinth.core.obj.Project
import org.bandev.labyrinth.databinding.ProjectRecyclerAdapterBinding

/**
 * Show a list of [Project] classes to the user.
 * @param projectList [MutableList] - The list of Projects to display
 * @param imageLoader [ImageLoader] - The imageLoader from the activity
 * @param clickListener [ClickListener]
 * @param context [Context] - The context of the activity
 * @author Jack Devey
 * @since v0.0.3
 */

class ProjectRecyclerAdapter(
    private val projectList: MutableList<Project>,
    private val imageLoader: ImageLoader,
    private val clickListener: ClickListener,
    private val context: Context
) : RecyclerView.Adapter<ProjectRecyclerAdapter.ViewHolder>() {

    private val central: Central = Central() // Used to load avatars more efficiently

    /** The ViewHolder for each item in the list
     * @param itemView [View]
     * @return [RecyclerView.ViewHolder]
     * */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val binding: ProjectRecyclerAdapterBinding = ProjectRecyclerAdapterBinding.bind(itemView)
        val name: TextView = binding.name
        val description: TextView = binding.description
        val avatar: ImageView = binding.avatar

        init {
            itemView.setOnClickListener(this)
        }

        /**
         * On item click
         * @param v [View]
         */
        override fun onClick(v: View?) {
            clickListener.onClick(projectList[bindingAdapterPosition])
        }
    }

    /** Setup the view for each part of the list
     * @param parent [ViewGroup]
     * @param viewType [Int]
     * @return [ViewHolder]
     * */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProjectRecyclerAdapterBinding.inflate(
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
        val project = projectList[position]
        // Set the text
        with(holder) {
            name.text = project.name // Project name
            description.text = project.description // Project Description
        }
        // Load the avatar using the avatar loader from central
        central.loadAvatar(
            project.avatar, // Avatar url
            project.name, // Name for the placeholder
            holder.avatar, // ImageView to load into
            RoundedCornersTransformation(20f), // Crop the corners
            imageLoader, // Use the global imageLoader
            100, // Only 100px, not too big ;)
            context // Context passed by activity
        )
    }

    /** Count the items, literally just the size of the list */
    override fun getItemCount(): Int = projectList.size

    /** On item click listener **/
    interface ClickListener {
        fun onClick(project: Project)
    }
}