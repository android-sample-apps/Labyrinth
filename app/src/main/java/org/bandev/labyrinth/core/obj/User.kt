package org.bandev.labyrinth.core.obj

import org.json.JSONObject

class User(rawData: JSONObject) {

    var id: Int = 0
    var name: String = ""
    var username: String = ""
    var avatar: String = ""
    var url: String = ""
    var bio: String = ""
    var location: String = ""
    var email: String = ""
    var skype: String = ""
    var linkedin: String = ""
    var twitter: String = ""
    var website: String = ""
    var organization: String = ""
    var job: String = ""

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