package org.bandev.labyrinth.core

import android.content.Context
import android.view.GestureDetector
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.Arrays.toString

class api {
    fun getUser(){

    }

    fun getUserGroups(context: Context, token: String){
        AndroidNetworking.initialize(context)
        AndroidNetworking.get("https://gitlab.com/api/v4/groups?access_token=$token")
            .build()
            .getAsJSONArray(object: JSONArrayRequestListener {
                override fun onResponse(response: JSONArray?){
                    var index = 0;
                    var pref = context.getSharedPreferences("User-Groups",0)
                    var edit = pref.edit()
                    while(index != response?.length()){
                        var string = response?.get(index)?.toString().toString()
                        edit.putString(index.toString(),string)
                        index++
                    }
                    edit.putInt("numGroups", index)
                    edit.commit()
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
                    var index = 0;
                    var pref = context.getSharedPreferences("User-Projects",0)
                    var edit = pref.edit()
                    while(index != response?.length()){
                        var string = response?.get(index)?.toString().toString()
                        edit.putString(index.toString(),string)
                        index++
                    }
                    edit.putInt("numProjects", index)
                    edit.commit()
                }

                override fun onError(error: ANError?) {
                    // handle error
                }

            })
    }

    fun getProjectBadges(context: Context, token: String, projectId: String){
        AndroidNetworking.initialize(context)
        AndroidNetworking.get("https://gitlab.com/api/v4/projects/$projectId/badges?access_token=$token")
                .build()
                .getAsJSONArray(object: JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray?){
                        var string = response?.toString()

                    }

                    override fun onError(error: ANError?) {
                        // handle error
                    }

                })
    }
}