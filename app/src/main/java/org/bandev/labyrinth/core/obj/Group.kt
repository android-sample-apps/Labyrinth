package org.bandev.labyrinth.core.obj

import android.content.Context
import org.json.JSONObject

class Group(json: JSONObject) {
    val id = json.getInt("id")
    val webUrl = json.getString("web_url")
    val name = json.getString("name")
    val path = json.getString("path")
    val localDesc = json.getString("description")
    val description = if (localDesc != "") localDesc else "No Description"
    val visibility = json.getString("visibility")
    val avatar = json.getString("avatar_url")

}