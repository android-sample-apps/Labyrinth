package org.bandev.labyrinth.core

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import okhttp3.*
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.core.obj.*
import org.bandev.labyrinth.core.obj.User
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.IOException

class Connection(val context: Context) {
    val profile: Profile = Profile()
    val client = OkHttpClient()
    private var token = ""

    init {
        profile.login(context, 0)
        token = profile.getData("token")
    }

    inner class Project {

        fun get(id: Int) {
            val request = Request.Builder()
                .url("https://gitlab.com/api/v4/projects/$id")
                .header("PRIVATE-TOKEN", token)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        val project =
                            Project(JSONObject((response.body ?: return@use).string()), context)
                        EventBus.getDefault().post(Notify.ReturnProject(project))
                    }
                }
            })
        }

        fun getStats(id: Int) {
            val request = Request.Builder()
                .url("https://gitlab.com/api/v4/projects/$id?statistics=true")
                .header("PRIVATE-TOKEN", token)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        val responseStr = (response.body ?: return@use).string()
                        val project = ProjectStats(JSONObject(responseStr))
                        EventBus.getDefault().post(Notify.ReturnProjectStats(project))
                    }
                }
            })
        }

        fun fork(project: org.bandev.labyrinth.core.obj.Project) {
            val id = project.id
            AndroidNetworking.post("https://gitlab.com/api/v4/projects/$id/fork")
                .addHeaders("PRIVATE-TOKEN", token)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        project.forks = project.forks + 1
                        EventBus.getDefault().post(
                            Notify.ReturnFork(
                                project.forks,
                                Project(response, context)
                            )
                        )
                    }

                    override fun onError(error: ANError) {
                        // handle error
                    }
                })
        }

        fun star(project: org.bandev.labyrinth.core.obj.Project) {
            val id = project.id
            AndroidNetworking.post("https://gitlab.com/api/v4/projects/$id/star")
                .addHeaders("PRIVATE-TOKEN", token)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        project.stars = project.stars + 1
                        EventBus.getDefault().post(
                            Notify.ReturnStar(
                                project.stars,
                                true
                            )
                        )
                    }

                    override fun onError(error: ANError) {
                        //There is no propper way to see what error code was returned
                        //so just unstar anyway no matter what
                        unstar(project)
                    }
                })
        }

        fun unstar(project: org.bandev.labyrinth.core.obj.Project) {
            val id = project.id
            AndroidNetworking.post("https://gitlab.com/api/v4/projects/$id/unstar")
                .addHeaders("PRIVATE-TOKEN", token)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        project.stars = project.stars - 1
                        EventBus.getDefault().post(
                            Notify.ReturnStar(
                                project.stars,
                                false
                            )
                        )
                    }

                    override fun onError(error: ANError) {

                    }
                })
        }

        fun getLastCommit(project: org.bandev.labyrinth.core.obj.Project) {
            val id = project.id
            val branch = project.defaultBranch
            AndroidNetworking.get("https://gitlab.com/api/v4/projects/$id/repository/commits/$branch")
                .addHeaders("PRIVATE-TOKEN", token)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        val commit = Commit(response)
                        EventBus.getDefault().post(
                            Notify.ReturnCommit(commit)
                        )
                    }

                    override fun onError(error: ANError) {

                    }
                })
        }
    }

    inner class Users {

        fun getAvatar(email: String) {
            AndroidNetworking.get("https://gitlab.com/api/v4/avatar")
                .addQueryParameter("email", email)
                .addHeaders("PRIVATE-TOKEN", token)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        EventBus.getDefault().post(
                            Notify.ReturnAvatar(response.getString("avatar_url"))
                        )
                    }

                    override fun onError(error: ANError) {

                    }
                })
        }

        fun get(id: Int) {
            AndroidNetworking.get("https://gitlab.com/api/v4/users/$id")
                .addHeaders("PRIVATE-TOKEN", token)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        EventBus.getDefault().post(Notify.ReturnUser(User(response)))
                    }

                    override fun onError(error: ANError) {

                    }
                })
        }

    }

    inner class Groups {

        fun get(id: Int) {
            val request = Request.Builder()
                .url("https://gitlab.com/api/v4/groups/$id?with_projects=false")
                .header("PRIVATE-TOKEN", token)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        val responseStr = (response.body ?: return@use).string()
                        val group = Group(JSONObject(responseStr), context)
                        EventBus.getDefault().post(Notify.ReturnGroup(group))
                    }
                }
            })
        }

    }

}