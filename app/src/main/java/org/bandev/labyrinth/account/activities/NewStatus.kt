package org.bandev.labyrinth.account.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import io.wax911.emojify.model.Emoji
import io.wax911.emojify.parser.EmojiParser
import org.bandev.labyrinth.Notify
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.databinding.AccountNewStatusBinding
import org.bandev.labyrinth.databinding.ProjectsNewIssueBinding
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NewStatus : AppCompatActivity() {

    private lateinit var binding: AccountNewStatusBinding
    private var profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AccountNewStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_left)

        Compatibility().edgeToEdge(window, binding.root, binding.toolbar, resources)


        profile.login(this, 0)
        binding.inTitle.performClick()
        if (intent.extras?.getString("emoji") != ":null:"){
            binding.inEmoji.setText(EmojiParser.parseToUnicode(intent.extras?.getString("emoji").toString()))
            binding.inTitle.setText(intent.extras?.getString("message"))
        }

    }

    private fun send() {
        val emoji = binding.inEmoji.text.toString()
        val title = binding.inTitle.text.toString()
        val emojis = EmojiParser.extractEmojis(emoji)
        var safe = true

        // If not an emoji
        if (emoji != "" && emojis == listOf<Emoji>()) {
            binding.boxEmoji.error = "Must be an emoji"
            safe = false
        }

        if (safe) {
            val data = Intent()
            data.putExtra("emoji", emoji)
            data.putExtra("title", title)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_new_status, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.confirm -> {
                send()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}