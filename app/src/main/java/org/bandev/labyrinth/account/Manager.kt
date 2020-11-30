package org.bandev.labyrinth.account

import android.app.Service
import android.content.Intent

import android.os.IBinder


class Manager : Service() {
    private var authenticator: Authenticator? = null

    override fun onCreate() {
        authenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder {
        return authenticator!!.iBinder
    }
}