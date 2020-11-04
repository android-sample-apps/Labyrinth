package org.bandev.labyrinth

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.squareup.picasso.Picasso
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val pref = getSharedPreferences("Settings", 0)


        AndroidNetworking.initialize(this)
        AndroidNetworking.get("https://gitlab.com/api/v4/user?access_token=" + pref.getString("token", null))
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?){
                    if (response != null) {
                        var username = findViewById<TextView>(R.id.username)
                        var email = findViewById<TextView>(R.id.email)
                        var avatar = findViewById<ImageView>(R.id.avatar)
                        username.text = response.getString("username")
                        email.text = response.getString("email")

                        Picasso.get().load(response.getString("avatar_url")).transform(
                            CircleTransform()
                        )
                            .into(avatar)

                    }else{
                        Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(error: ANError?) {
                    // handle error
                    Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_LONG).show()
                }

            })


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}