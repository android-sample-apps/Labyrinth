package org.bandev.labyrinth.apis

import android.content.Context
import android.content.SharedPreferences

class Pins2(ctx: Context) {

    private val prefs: SharedPreferences = ctx.getSharedPreferences("Pins2", 0)
    private val editor: SharedPreferences.Editor = prefs.edit()

    fun read(): MutableList<String> = prefs.getString("list", "null")
        .toString().split("||").toMutableList()

    fun exists(gid: String): Boolean = read().contains(gid)

    fun add(gid: String): Boolean = with(read()) {
        if (contains(gid)) return false
        editor.putString("list", this.joinToString("||") + "||$gid")
        return true
    }

    fun remove(gid: String): Boolean = with(read()) {
        if (!contains(gid)) return false
        remove(gid)
        editor.putString("list", this.joinToString("||"))
        return true
    }
}