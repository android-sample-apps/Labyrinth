package org.bandev.labyrinth

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import org.bandev.labyrinth.core.Appearance
import org.bandev.labyrinth.intro.First
import java.util.concurrent.Executor


class Splash : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    lateinit var account: Account
    private lateinit var accountManager: AccountManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var hasAccount = false

        accountManager = AccountManager.get(this)
        val accounts: Array<out Account> =
            accountManager.getAccountsByType("org.bandev.labyrinth.account.authenticator")
        if (accounts.isNotEmpty()) {
            hasAccount = true
        }

        Appearance().setAppTheme(Appearance().getAppTheme(this))

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)

                    val i = Intent(applicationContext, BiometricFailAct::class.java)
                    val mBundle = Bundle()
                    i.putExtras(mBundle)
                    startActivity(i)
                    finish()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)


                    checkWifi()

                    val i = Intent(applicationContext, MainAct::class.java)
                    val mBundle = Bundle()
                    i.putExtras(mBundle)
                    startActivity(i)
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    val i = Intent(applicationContext, BiometricFailAct::class.java)
                    val mBundle = Bundle()
                    i.putExtras(mBundle)
                    startActivity(i)
                    finish()
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

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val biometrics = sharedPrefs.getBoolean("biometric", false)

        if (hasAccount) {
            if (biometrics) {
                biometricPrompt.authenticate(promptInfo)
            } else {

                checkWifi()

                val i = Intent(applicationContext, MainAct::class.java)
                val mBundle = Bundle()
                i.putExtras(mBundle)
                startActivity(i)
                finish()
            }

        } else {

            checkWifi()
            val i = Intent(this, First::class.java)
            val mBundle = Bundle()
            i.putExtras(mBundle)
            startActivity(i)
            finish()
        }


    }

    fun checkWifi() {
        if (!isOnline()) {
            val i = Intent(this, NoInternetAct::class.java)
            this.startActivity(i)
        }
    }

    private fun isOnline(): Boolean {
        val conMgr =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = conMgr.activeNetworkInfo
        if (netInfo == null || !netInfo.isConnected || !netInfo.isAvailable) {
            Toast.makeText(this, "No Internet connection!", LENGTH_LONG).show()
            return false
        }
        return true
    }


}
