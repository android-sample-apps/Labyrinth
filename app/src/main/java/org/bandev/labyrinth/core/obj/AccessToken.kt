package org.bandev.labyrinth.core.obj

import org.json.JSONArray
import org.json.JSONObject

class AccessToken(data: JSONObject) {
    val id: Int = data.getInt("id")
    val name: String = data.getString("name")
    val revoked: Boolean = data.getBoolean("revoked")
    val createdAt: String = data.getString("created_at")
    val scopes: JSONArray = data.getJSONArray("scopes")
    val userId: Int = data.getInt("user_id")
    val active: Boolean = data.getBoolean("active")
    val expiresAt: String = data.getString("expires_at")
}