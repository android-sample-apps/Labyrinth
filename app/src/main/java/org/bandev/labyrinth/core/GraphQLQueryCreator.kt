@file:Suppress("ProblematicWhitespace")

package org.bandev.labyrinth.core

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class GraphQLQueryCreator(var token: String) {
    private var BaseUrl: String = "https://gitlab.com/api/graphql"

    fun searchProjects(query: String): Request {
        val data = """{
            projects(search: "$query") {
		        nodes {
                    name
                }
            }
        }"""

        return buildRequest(data)
    }

    fun exploreProjects(searchTerm: String, limit: Int): Request {
        val data = """{
  projects(search: "$searchTerm", sort: "stars_desc", first: $limit) {
    nodes {
      fullPath
      name
      starCount
      avatarUrl
      description
    }
  }
}

"""

        return buildRequest(data)
    }

    fun getProject(fullpath: String): Request {
        val data = """{
  project(fullPath: "$fullpath") {
    id
    name
    avatarUrl
    fullPath
    description
    forksCount
    starCount
    releases {
      nodes {
        tagName
      }
    }
    repository {
      tree {
        lastCommit {
          shortId
          title
          description
          author {
            id
            name
            username
            avatarUrl
          }
          signatureHtml
          pipelines {
            nodes {
              status
            }
          }
        }
      }
    }
    statistics {
      commitCount
      repositorySize
    }
  }
}
"""

        return buildRequest(data)
    }

    private fun buildRequest(data: String): Request {
        val bodyJSON = JSONObject().put("query", data).toString()

        return Request.Builder()
            .url(BaseUrl)
            .header("Authorization", "Bearer $token")
            .post(bodyJSON.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
    }
}