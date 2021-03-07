package org.bandev.labyrinth

import org.bandev.labyrinth.core.obj.Commit
import org.bandev.labyrinth.core.obj.Project
import org.bandev.labyrinth.core.obj.ProjectStats
import org.bandev.labyrinth.core.obj.User

sealed class Notify {
    data class ReturnProject(val project: Project) : Notify()
    data class ReturnFork(val forks: Int, val newProject: Project) : Notify()
    data class ReturnStar(val stars: Int, val positive: Boolean) : Notify()
    data class ReturnCommit(val commit: Commit) : Notify()
    data class ReturnAvatar(val url: String) : Notify()
    data class ReturnProjectStats(val projectStats: ProjectStats) : Notify()
    data class ReturnUser(val user: User) : Notify()
}
