package org.bandev.labyrinth

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import org.bandev.labyrinth.intro.First
import java.util.concurrent.Executor


class Splash : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = getSharedPreferences("Settings", 0)



        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    biometricPrompt.authenticate(promptInfo)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    val i = Intent(applicationContext, MainAct::class.java)
                    val mBundle = Bundle()
                    i.putExtras(mBundle)
                    startActivity(i)
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    biometricPrompt.authenticate(promptInfo)
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for Labyrinth")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        biometricPrompt.authenticate(promptInfo)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val biometrics = sharedPrefs.getBoolean("biometric", false)

        if (pref.getString("token", "null") != "null") {
            if(biometrics){
                biometricPrompt.authenticate(promptInfo)
            }else{
                val i = Intent(applicationContext, MainAct::class.java)
                val mBundle = Bundle()
                i.putExtras(mBundle)
                startActivity(i)
                finish()
            }

        }else{
            val i = Intent(this, First::class.java)
            val mBundle = Bundle()
            i.putExtras(mBundle)
            startActivity(i)
            finish()
        }

    }
}