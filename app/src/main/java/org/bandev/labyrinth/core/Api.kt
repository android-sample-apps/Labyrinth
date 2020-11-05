package org.bandev.labyrinth.core

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import org.json.JSONArray

class Api {
    fun getUser(){

    }

    fun getUserGroups(context: Context, token: String){
        AndroidNetworking.initialize(context)
        AndroidNetworking.get("https://gitlab.com/api/v4/groups?access_token=$token")
            .build()
            .getAsJSONArray(object: JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?){
                    var index = 0
                    val pref = context.getSharedPreferences("User-Groups",0)
                    val edit = pref.edit()
                    while(index != response?.length()){
                        val string = response?.get(index)?.toString().toString()
                        edit.putString(index.toString(),string)
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

    fun getUserProjects(context: Context, token: String){
        AndroidNetworking.initialize(context)
        AndroidNetworking.get("https://gitlab.com/api/v4/projects?access_token=$token&membership=true")
            .build()
            .getAsJSONArray(object: JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?){
                    var index = 0
                    val pref = context.getSharedPreferences("User-Projects",0)
                    val edit = pref.edit()
                    while(index != response?.length()){
                        val string = response?.get(index)?.toString().toString()
                        edit.putString(index.toString(),string)
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
}