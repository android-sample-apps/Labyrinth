package org.bandev.labyrinth.apis

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.bandev.labyrinth.core.GraphQLQueryCreator
import org.bandev.labyrinth.core.obj.ProjectQL
import org.json.JSONObject
import java.io.IOException

class Explore {

    val fields: Array<String> = arrayOf(
        "Android Apps", "iOS Apps", "Web Development", "JavaScript"
    )

    val client: OkHttpClient = OkHttpClient()
    var field: String = fields[0]
    private var index = 0

    private fun newField() {
        index++
        if(index > 3) index = 0
        field = fields[index]
    }

    fun getProjects(
        limit: Int,
        token: String,
        after: (list: MutableList<ProjectOnExplore>) -> Unit
    ) {

        newField()

        val req = GraphQLQueryCreator(token).exploreProjects(field, limit)

        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val ret = mutableListOf<ProjectOnExplore>()
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val res = (response.body ?: return).string()
                val nodes = JSONObject(res).getJSONObject("data").getJSONObject("projects").getJSONArray("nodes")
                for (i in 0 until nodes.length()) ret.add(ProjectOnExplore(JSONObject(nodes[i].toString())))
                after(ret)
            }
        })

    }

    class ProjectOnExplore(data: JSONObject) {
        val fullPath: String = data.getString("fullPath")
        val name: String = data.getString("name")
        val starCount: Int = data.getInt("starCount")
        val avatarUrl: String = data.getString("avatarUrl")
        val description: String = data.getString("description")
    }

}