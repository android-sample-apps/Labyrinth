package org.bandev.labyrinth

import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.core.Appearance
import org.bandev.labyrinth.core.Compatibility
import org.bandev.labyrinth.intro.First
import org.bandev.labyrinth.widgets.About

class SettingsAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        Compatibility().edgeToEdge(window, View(this), toolbar, resources)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back_white)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.colorPrimary)

        }

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private var profile: Profile = Profile()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            profile.login(requireContext(), 0)
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            setAutoSyncVals()

            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

            val theme = findPreference<Preference>("theme") as ListPreference?
            theme?.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { preference, newValue ->
                        Appearance().setAppTheme(newValue.toString())
                        true
                    }

            val about = findPreference("about") as Preference?
            about?.setOnPreferenceClickListener { preference ->
                val intent = Intent(context, About::class.java)
                startActivity(intent)
                true
            }

            val delete = findPreference("delete") as Preference?
            delete?.setOnPreferenceClickListener { preference ->
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Are you sure?")
                        .setMessage("Removing your account will erase all of your user data on this device, this means you will need an access token to login again.")
                        .setNeutralButton("No") { dialog, which ->

                        }
                        .setPositiveButton("Yes") { dialog, which ->
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                                profile.delete()
                            }
                            val preferences: SharedPreferences =
                                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                            val editor = preferences.edit()
                            editor.clear()
                            editor.apply()
                            val intent = Intent(requireContext(), First::class.java)
                            this.startActivity(intent)
                        }
                        .show()
                true
            }

            val sync = findPreference("sync") as Preference?
            sync?.setOnPreferenceClickListener { preference ->
                val settingsBundle = Bundle().apply {
                    putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                    putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
                }
                ContentResolver.requestSync(profile.account, "org.bandev.labyrinth.account.provider", settingsBundle)
                true
            }


            val async = findPreference("async") as SwitchPreferenceCompat?
            async?.setOnPreferenceClickListener { preference ->
                if (sharedPrefs.getBoolean("async", true)) {
                    ContentResolver.setSyncAutomatically(
                            profile.account,
                            "org.bandev.labyrinth.account.provider",
                            true)
                } else {
                    ContentResolver.setSyncAutomatically(
                            profile.account,
                            "org.bandev.labyrinth.account.provider",
                            false)
                }
                true
            }

        }

        private fun setAutoSyncVals() {
            val async = findPreference("async") as SwitchPreferenceCompat?
            if (ContentResolver.getSyncAutomatically(profile.account, "org.bandev.labyrinth.account.provider")) {
                (async ?: return).isChecked = true
            } else {
                (async ?: return).isChecked = false
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
