package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.squareup.picasso.Picasso
import java.util.regex.Pattern

class MainAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_act)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_notifications,
                R.id.navigation_dashboard,
                R.id.navigation_home
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        val flag = findViewById<TextView>(R.id.title)
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            toolbar.setTitle(destination.label, flag, arguments)
        }

        val pref = getSharedPreferences("User", 0)

        //Defines avatar, sets the image and handles a click
        val avatar = findViewById<ImageView>(R.id.avatar)
        Picasso.get().load(pref?.getString("avatarUrl", "null")).transform(CircleTransform())
            .into(avatar)
        avatar.setOnClickListener {
            val intent = Intent(this, profileAct::class.java)
            this.startActivity(intent)
        }

        navView.setupWithNavController(navController)
    }

    private fun Toolbar.setTitle(label: CharSequence?, textView: TextView, arguments: Bundle?) {
        if (label != null) {
            // Fill in the data pattern with the args to build a valid URI
            val title = StringBuffer()
            val fillInPattern = Pattern.compile("\\{(.+?)\\}")
            val matcher = fillInPattern.matcher(label)
            while (matcher.find()) {
                val argName = matcher.group(1)
                if (arguments != null && arguments.containsKey(argName)) {
                    matcher.appendReplacement(title, "")
                    title.append(arguments.get(argName).toString())
                } else {
                    return //returning because the argument required is not found
                }
            }
            matcher.appendTail(title)
            setTitle("")
            textView.text = title
        }



        fun setFlag(input: String) {

        }
    }
}