package org.bandev.labyrinth.core.obj

import org.json.JSONObject

class AccessToken(data: JSONObject) {
    val id = data.getInt("id")
    val name = data.getString("name")
    val revoked = data.getBoolean("revoked")
    val createdAt = data.getString("created_at")
    val scopes = data.getJSONArray("scopes")
    val userId = data.getInt("user_id")
    val active = data.getBoolean("active")
    val expiresAt = data.getString("expires_at")
}