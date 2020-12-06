package org.bandev.labyrinth.account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.view.View
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import org.bandev.labyrinth.core.Api
import org.json.JSONObject

class Profile {

    lateinit var account: Account
    private lateinit var accountManager: AccountManager

    fun login(context: Context, accountNum: Int) {
        accountManager = AccountManager.get(context)
        val accounts: Array<out Account> =
            accountManager.getAccountsByType("org.bandev.labyrinth.account.authenticator")
        account = accounts[accountNum]
    }

    fun getData(key: String): String {
        return accountManager.getUserData(account, key).toString()
    }

    fun delete() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccountExplicitly(account)
        }
    }

    fun sync(context: Context, view: View) {
        val token = getData("token")
        val server = getData("server")

        AndroidNetworking.initialize(context)
        AndroidNetworking.get("$server/api/v4/user?access_token=$token")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val userData = Bundle()
                    userData.putString("token", token)
                    userData.putString("server", "https://gitlab.com")
                    userData.putString("username", response.getString("username"))
                    userData.putString("email", response.getString("email"))
                    userData.putString("bio", response.getString("bio"))
                    userData.putString("location", response.getString("location"))
                    userData.putInt("id", response.getInt("id"))
                    userData.putString("avatarUrl", response.getString("avatar_url"))
                    userData.putString("webUrl", response.getString("web_url"))

                    delete()

                    Account(response.getString("username"), "org.bandev.labyrinth.account.authenticator").also { account ->
                        accountManager.addAccountExplicitly(account, token, userData)
                    }

                    Api().getUserGroups(context, token)
                    Api().getUserProjects(context, token)
                    Api().getUserTodos(context, token)

                    Snackbar.make(view, "Account successfully synced!", Snackbar.LENGTH_SHORT)
                        .show()
                }

                override fun onError(error: ANError?) {
                    // handle error
                    Snackbar.make(view, "Error syncing with GitLab!", Snackbar.LENGTH_SHORT).setAction("Retry") {
                        sync(context, view)
                    }.show()
                }
            })
    }

    fun syncNoFeedback(context: Context) {
        val token = getData("token")
        val server = getData("server")

        AndroidNetworking.initialize(context)
        AndroidNetworking.get("$server/api/v4/user?access_token=$token")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val userData = Bundle()
                    userData.putString("token", token)
                    userData.putString("server", "https://gitlab.com")
                    userData.putString("username", response.getString("username"))
                    userData.putString("email", response.getString("email"))
                    userData.putString("bio", response.getString("bio"))
                    userData.putString("location", response.getString("location"))
                    userData.putInt("id", response.getInt("id"))
                    userData.putString("avatarUrl", response.getString("avatar_url"))
                    userData.putString("webUrl", response.getString("web_url"))

                    delete()

                    Account(response.getString("username"), "org.bandev.labyrinth.account.authenticator").also { account ->
                        accountManager.addAccountExplicitly(account, token, userData)
                    }

                    Api().getUserGroups(context, token)
                    Api().getUserProjects(context, token)
                    Api().getUserTodos(context, token)

                }

                override fun onError(error: ANError?) {
                    // handle error
                }
            })
    }

}