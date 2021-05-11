package org.bandev.labyrinth.core.obj

import org.json.JSONObject

class Group(json: JSONObject) {
    val id: Int = json.getInt("id")
    val webUrl: String = json.getString("web_url")
    val name: String = json.getString("name")
    val path: String = json.getString("path")
    private val localDesc: String = json.getString("description")
    val description: String = if (localDesc != "") localDesc else "No Description"
    val visibility: String = json.getString("visibility")
    val avatar: String = json.getString("avatar_url")

}