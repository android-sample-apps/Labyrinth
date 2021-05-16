package org.bandev.labyrinth.core.obj

import org.json.JSONArray
import org.json.JSONObject

class ProjectQL(json: JSONObject) {
    val data: JSONObject = json.getJSONObject("data").getJSONObject("project")


    var hasCommits = true

    val id: String = data.getString("id")
    val name: String = data.getString("name")
    val description: String = data.getString("description")
    val fullPath: String = data.getString("fullPath")
    val avatarUrl: String = data.getString("avatarUrl")
    val forksCount: Int = data.getInt("forksCount")
    val starCount: Int = data.getInt("starCount")

    var commits: Int = 0
    var repoSize: Double = 0.0

    val releases: MutableList<String> = mutableListOf()

    lateinit var lastCommit: LastCommitQL

    init {
        if(!data.isNull("statistics")) {
            commits = data.getJSONObject("statistics").getInt("commitCount")
            repoSize = data.getJSONObject("statistics").getDouble("repositorySize")
        }

        if (data.getJSONObject("repository")
                .isNull("tree")) {
            hasCommits = false

        }else {
            lastCommit = LastCommitQL(
                data.getJSONObject("repository")
                    .getJSONObject("tree").getJSONObject("lastCommit"))
        }


        val releasesTmp = data.getJSONObject("releases").getJSONArray("nodes")
        if (!releasesTmp.isNull(0)) {
            for (i in 0 until releasesTmp.length()) {
                val obj = JSONObject(releasesTmp[i].toString())
                releases.add(obj.getString("tagName"))
            }
        } else releases.add("null")
    }

    class LastCommitQL(json: JSONObject) {
        val author: Author = Author(json.getJSONObject("author"))

        val shortId: String = json.getString("shortId")
        val title: String = json.getString("title")
        val description: String = json.getString("description")
        val signatureHtml: String = json.getString("signatureHtml")

        val pipelines: MutableList<Pipeline> = mutableListOf()

        init {
            val pipelinesTmp: JSONArray = json.getJSONObject("pipelines").getJSONArray("nodes")
            for (i in 0 until pipelinesTmp.length()) {
                pipelines.add(Pipeline(JSONObject(pipelinesTmp[i].toString())))
            }
            if (!pipelinesTmp.isNull(0)) {
                for (i in 0 until pipelinesTmp.length()) {
                    val obj = JSONObject(pipelinesTmp[i].toString())
                    pipelines.add(Pipeline(JSONObject(pipelinesTmp[i].toString())))
                }
            } else pipelines.add(Pipeline(JSONObject("{'status': 'NULL'}")))
        }

        class Author(json: JSONObject) {
            val id: String = json.getString("id")
            val name: String = json.getString("name")
            val username: String = json.getString("username")
            val avatarUrl: String = json.getString("avatarUrl")
        }

        class Pipeline(json: JSONObject) {
            val status: String = json.getString("status")
        }
    }
}