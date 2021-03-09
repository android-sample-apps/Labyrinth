package org.bandev.labyrinth.core.obj

import org.json.JSONObject

class User(rawData: JSONObject) {

    var id: Int = 0
    var name: String = ""
    var username: String = ""
    var avatar: String = ""
    private var url: String = ""
    var bio: String = ""
    var location: String = ""
    var email: String = ""
    private var skype: String = ""
    private var linkedin: String = ""
    private var twitter: String = ""
    private var website: String = ""
    private var organization: String = ""
    private var job: String = ""

    init {
        id = rawData.getInt("id")
        name = rawData.getString("name")
        username = rawData.getString("username")
        avatar = rawData.getString("avatar_url")
        url = rawData.getString("web_url")
        bio = rawData.getString("bio")
        location = rawData.getString("location")
        email = rawData.getString("public_email")
        skype = rawData.getString("skype")
        linkedin = rawData.getString("linkedin")
        twitter = rawData.getString("twitter")
        website = rawData.getString("website_url")
        organization = rawData.getString("organization")
        job = rawData.getString("job_title")
    }

}