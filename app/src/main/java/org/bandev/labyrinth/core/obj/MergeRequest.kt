package org.bandev.labyrinth.core.obj

import org.json.JSONObject

class MergeRequest(val json: JSONObject) {
    val id: Int = json.getInt("id")
    val iid: Int = json.getInt("iid")
    val projectId: Int = json.getInt("project_id")
    val title: String = json.getString("title")
    val description: String = json.getString("description")
    val state: String = json.getString("state")
    val createdAt: String = json.getString("created_at")
    val mergedAt: String = json.getString("merged_at")
    val closedBy: String = json.getString("closed_by")
    val closedAt: String = json.getString("closed_at")
    val targetBranch: String = json.getString("target_branch")
    val sourceBranch: String = json.getString("source_branch")
    val upVotes: Int = json.getInt("upvotes")
    val downVotes: Int = json.getInt("downvotes")
    val author: Author = Author(json.getJSONObject("author"))

    class Merger(json: JSONObject) {
        val id: Int = json.getInt("id")
        val name: String = json.getString("name")
        val username: String = json.getString("username")
        val avatarUrl: String = json.getString("avatar_url")
        val webUrl: String = json.getString("web_url")
    }

    class Author(json: JSONObject) {
        val id: Int = json.getInt("id")
        val name: String = json.getString("name")
        val username: String = json.getString("username")
        val avatarUrl: String = json.getString("avatar_url")
        val webUrl: String = json.getString("web_url")
    }
}