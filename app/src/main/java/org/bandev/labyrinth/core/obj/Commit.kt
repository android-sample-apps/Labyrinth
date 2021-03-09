package org.bandev.labyrinth.core.obj

import org.json.JSONObject

class Commit(rawData: JSONObject) {

    var id: String = ""
    var shortID: String = ""
    var title: String = ""
    private var message: String = ""
    private var authorName: String = ""
    var authorEmail: String = ""
    private var authoredDate: String = ""
    private var url: String = ""
    private var additions: Int = 0
    private var deletions: Int = 0
    var status: String = ""

    init {
        id = rawData.getString("id")
        shortID = rawData.getString("short_id")
        title = rawData.getString("title")
        message = rawData.getString("message")
        authorName = rawData.getString("author_name")
        authorEmail = rawData.getString("author_email")
        authoredDate = rawData.getString("authored_date")
        url = rawData.getString("web_url")
        additions = rawData.getJSONObject("stats").getInt("additions")
        deletions = rawData.getJSONObject("stats").getInt("deletions")
        status = rawData.getString("status")
    }
}