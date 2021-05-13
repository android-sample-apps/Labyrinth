package org.bandev.labyrinth

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.core.Connection
import org.bandev.labyrinth.core.GraphQLQueryCreator
import org.bandev.labyrinth.core.Notify
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException

class NewProjectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profile = Profile()
        profile.login(this, 0)

        val req = GraphQLQueryCreator(profile.getData("token")).getProject("bandev/Labyrinth")
        OkHttpClient().newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                Log.d("testx", (response.body ?: return).string())
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyReceive(event: Notify) {
        Log.d("testx", "n")
        when (event) {
            is Notify.ReturnText -> Log.d("testx", event.text)
            else -> null
        }
    }

}