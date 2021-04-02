package org.bandev.labyrinth.core.obj

import android.content.Context
import org.bandev.labyrinth.R
import org.json.JSONObject

/** Each gitlab member for a project or group */
class Member(raw: JSONObject) {
    val id: Int = raw.getInt("id")
    val name: String = raw.getString("name")
    val username: String = raw.getString("username")
    val state: String = raw.getString("state")
    val avatarUrl: String = raw.getString("avatar_url")
    val webUrl: String = raw.getString("web_url")
    val accessLevel: Int = raw.getInt("access_level")
    val createdAt: String = raw.getString("created_at")
    val expiresAt: String = raw.getString("expires_at")

    fun getAccessLevel(context: Context): String {
        return context.getString(
            when (accessLevel) {
                50 -> R.string.access_50 //Owner
                40 -> R.string.access_40 //Maintainer
                30 -> R.string.access_30 //Developer
                20 -> R.string.access_20 //Reporter
                10 -> R.string.access_10 //Guest
                5 -> R.string.access_5 //Minimal access
                else -> R.string.access_0 //No access
            }
        )
    }
}