package org.bandev.labyrinth.models

import org.json.JSONArray
import org.json.JSONObject

class Commit(data: JSONObject) {

    val id: String = data.getString("id")
    val shortId: String = data.getString("short_id")
    val createdAt: String = data.getString("created_at")
    val parentIds: JSONArray = data.getJSONArray("parent_ids")
    val title: String = data.getString("title")
    val message: String = data.getString("message")


}