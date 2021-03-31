package org.bandev.labyrinth.core

import org.bandev.labyrinth.core.obj.*
import org.bandev.labyrinth.core.obj.User

sealed class Notify {
    data class ReturnProject(val project: Project) : Notify()
    data class ReturnFork(val forks: Int, val newProject: Project) : Notify()
    data class ReturnStar(val stars: Int, val positive: Boolean) : Notify()
    data class ReturnCommit(val commit: Commit) : Notify()
    data class ReturnAvatar(val url: String) : Notify()
    data class ReturnUser(val user: User) : Notify()
    data class ReturnGroup(val group: Group) : Notify()
    data class ReturnProjects(val projectsList: MutableList<Project>) : Notify()
    data class ReturnGroups(val groupsList: MutableList<Group>) : Notify()
}
