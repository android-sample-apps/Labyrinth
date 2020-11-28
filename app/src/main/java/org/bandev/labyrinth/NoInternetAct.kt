package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import org.bandev.labyrinth.intro.First
import java.util.concurrent.Executor

class NoInternetAct : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_no_internet)

        val button2: Button = findViewById(R.id.button3)

        button2.setOnClickListener {

            val intent = Intent(this, Splash::class.java)
            this.startActivity(intent)

        }


    }

}
