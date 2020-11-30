package org.bandev.labyrinth.account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

class Profile {

    lateinit var account: Account
    lateinit var accountManager: AccountManager

    fun login(context: Context, accountNum: Int) {
        accountManager = AccountManager.get(context)
        val accounts: Array<out Account> = accountManager.getAccountsByType("org.bandev.labyrinth.account.authenticator")
        account = accounts[accountNum]
    }

    fun getData(key: String): String {
        return accountManager.getUserData(account, key).toString()
    }

    fun delete() {
        accountManager.removeAccountExplicitly(account)
    }

}