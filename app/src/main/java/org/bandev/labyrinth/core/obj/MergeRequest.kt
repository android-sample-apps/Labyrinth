package org.bandev.labyrinth.core.obj

import org.json.JSONObject

class MergeRequest(val json: JSONObject) {
    val id = json.getInt("id")
    val iid = json.getInt("iid")
    val projectId = json.getInt("project_id")
    val title = json.getString("title")
    val descripton = json.getString("description")
    val state = json.getString("state")
    val createdAt = json.getString("created_at")
    val mergedAt = json.getString("merged_at")
    val closedBy = json.getString("closed_by")
    val closedAt = json.getString("closed_at")
    val targetBranch = json.getString("target_branch")
    val sourceBranch = json.getString("source_branch")
    val upVotes = json.getInt("upvotes")
    val downVotes = json.getInt("downvotes")
    val author = Author(json.getJSONObject("author"))

    class Merger(json: JSONObject) {
        val id = json.getInt("id")
        val name = json.getString("name")
        val username = json.getString("username")
        val avatarUrl = json.getString("avatar_url")
        val webUrl = json.getString("web_url")
    }

    class Author(json: JSONObject) {
        val id = json.getInt("id")
        val name = json.getString("name")
        val username = json.getString("username")
        val avatarUrl = json.getString("avatar_url")
        val webUrl = json.getString("web_url")
    }
}