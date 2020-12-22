package org.bandev.labyrinth.core

import android.content.Context
import android.content.SharedPreferences

class Pins(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("Pins", 0)
    private val editor = sharedPreferences.edit()
    var data: MutableList<String> = mutableListOf()
    private var dataP: MutableList<String> = mutableListOf()
    var itemCount: Int = 0

    init {
        val arrTmp = sharedPreferences.getString("data", " null").toString().split("/#@#/")
        dataP = arrTmp.toMutableList()
        data = arrTmp.toMutableList()
        data.removeAt(0)
        itemCount = sharedPreferences.getInt("itemCount", 0)
    }

    fun add(info: String) {
        dataP.add(info)
    }

    fun save() {
        editor.putString("data", dataP.joinToString("/#@#/"))
        editor.putInt("itemCount", dataP.size)
        editor.commit()
    }

    fun getItem(id: Int): String {
        return dataP[id]
    }

    fun remove(id: Int) {

    }

    fun debug_showAll(): String{
        return sharedPreferences.getString("data", " null").toString()
    }

    fun exists(info: String): Boolean{
        return data.contains(info)
    }
}