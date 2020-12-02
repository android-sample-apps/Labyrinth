package org.bandev.labyrinth.core

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

class Api {
    fun getUserToken(context: Context): String {
        val pref = context.getSharedPreferences("User", 0)
        return pref.getString("token", "null").toString()
    }

    fun getAvatar(email: String, context: Context) : String =
        runBlocking {
            var out =""
            val job: Job = launch(context = Dispatchers.Default) {
                AndroidNetworking.initialize(context)
                AndroidNetworking.get("https://gitlab.com/api/v4/avatar?email=$email")
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            out = response.getString("avatar_url")

                        }

                        override fun onError(error: ANError?) {
                            // handle error
                        }

                    })
            }
            job.join()
            return@runBlocking out
        }



    fun getUserGroups(context: Context, token: String) {
        AndroidNetworking.initialize(context)
        AndroidNetworking.get("https://gitlab.com/api/v4/groups?access_token=$token")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?) {
                    var index = 0
                    val pref = context.getSharedPreferences("User-Groups", 0)
                    val edit = pref.edit()
                    while (index != response?.length()) {
                        val string = response?.get(index)?.toString().toString()
                        edit.putString(index.toString(), string)
                        index++
                    }
                    edit.putInt("numGroups", index)
                    edit.apply()
                }

                override fun onError(error: ANError?) {
                    // handle error
                }

            })
    }

    fun getUserTodos(context: Context, token: String) {
        AndroidNetworking.initialize(context)
        AndroidNetworking.get("https://gitlab.com/api/v4/todos?access_token=$token")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?) {
                    var index = 0
                    val pref = context.getSharedPreferences("User-Todos", 0)
                    val edit = pref.edit()
                    while (index != response?.length()) {
                        val string = response?.get(index)?.toString().toString()
                        edit.putString(index.toString(), string)
                        index++
                    }
                    edit.putInt("numTodos", index)
                    edit.apply()
                }

                override fun onError(error: ANError?) {
                    // handle error
                }

            })
    }

    fun getUserProjects(context: Context, token: String) {
        AndroidNetworking.initialize(context)
        AndroidNetworking.get("https://gitlab.com/api/v4/projects?access_token=$token&membership=true")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?) {
                    var index = 0
                    val pref = context.getSharedPreferences("User-Projects", 0)
                    val edit = pref.edit()
                    while (index != response?.length()) {
                        val string = response?.get(index)?.toString().toString()
                        edit.putString(index.toString(), string)
                        index++
                    }
                    edit.putInt("numProjects", index)
                    edit.apply()
                }

                override fun onError(error: ANError?) {
                    // handle error
                }

            })
    }

    fun getProjectBadges(context: Context, token: String, projectId: String) {
        AndroidNetworking.initialize(context)
        AndroidNetworking.get("https://gitlab.com/api/v4/projects/$projectId/badges?access_token=$token")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?) {
                    var string = response?.toString()

                }

                override fun onError(error: ANError?) {
                    // handle error
                }

            })
    }
}