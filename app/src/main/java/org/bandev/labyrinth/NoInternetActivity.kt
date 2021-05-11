package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executor

class NoInternetActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_no_internet)

        val button2: Button = findViewById(R.id.logMeOut)

        button2.setOnClickListener {

            val intent = Intent(this, SplashActivity::class.java)
            this.startActivity(intent)

        }


    }

}
