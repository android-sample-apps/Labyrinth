package org.bandev.labyrinth.core.obj

import android.content.Context
import org.json.JSONObject

class Group(json: JSONObject, context: Context) {
    val id = json.getInt("id")
    val webUrl = json.getString("web_url")
    val name = json.getString("name")
    val path = json.getString("path")
    val description = json.getString("description")
    val visibility = json.getString("visibility")
    val avatarUrl = json.getString("avatar_url")

}