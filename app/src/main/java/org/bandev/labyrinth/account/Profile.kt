package org.bandev.labyrinth.account

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import org.bandev.labyrinth.core.Api
import org.json.JSONObject
import java.lang.IndexOutOfBoundsException

class Profile {

    lateinit var account: Account
    private lateinit var accountManager: AccountManager

    /**
     * Logs the user in using the account stored on their device.
     *
     * This must be called before using any other function inside the [Profile] class. Logging in
     * will also fill out [account] variable allowing for your own custom implementation.
     *
     * @author jack.txt
     * @since v1.0.0
     * @param context the context of the calling activity, [Context]
     * @param accountNum index of the account requested, [Int]
     */

    fun login(context: Context, accountNum: Int) {
        accountManager = AccountManager.get(context)
        val accounts = accountManager.getAccountsByType("org.bandev.labyrinth.account")
        account = accounts[accountNum]
    }

    /**
     * Returns data from the user's profile as a [String]
     *
     * This data from the user's labyrinth profile as stored in account manager that correlates with
     * the [key] parameter. All data returned is a [String]
     *
     * @author jack.txt
     * @since v1.0.0
     * @param key name of attribute being requested, [String]
     * @return the value of the data, [String]
     */

    fun getData(key: String): String {
        return accountManager.getUserData(account, key).toString()
    }

    /**
     * Returns data from the user's profile as a [String]
     *
     * This data from the user's labyrinth profile as stored in account manager that correlates with
     * the [key] parameter. All data returned is a [String]
     *
     * @author jack.txt
     * @since v1.0.0
     */

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun delete() {
        accountManager.removeAccountExplicitly(account)
    }

    /**
     * Syncs the user account manually
     *
     * Data is requested from gitlab and the userData bundle is updated
     *
     * @author jack.txt
     * @since v1.0.0
     * @param context the context of the calling activity, [Context]
     */

    fun sync(context: Context) {
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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        delete()
                    }

                    Account(
                        response.getString("username"),
                        "org.bandev.labyrinth.account"
                    ).also { account ->
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