package org.bandev.labyrinth

import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.core.Appearance
import org.bandev.labyrinth.intro.First

class SettingsAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backDrawable = IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
            colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            sizeDp = 16
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = backDrawable

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
                val intent = Intent(context, AboutActivity::class.java)
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
                ContentResolver.requestSync(
                    profile.account,
                    "org.bandev.labyrinth.account.provider",
                    settingsBundle
                )
                true
            }


            val async = findPreference("async") as SwitchPreferenceCompat?
            async?.setOnPreferenceClickListener { preference ->
                if (sharedPrefs.getBoolean("async", true)) {
                    ContentResolver.setSyncAutomatically(
                        profile.account,
                        "org.bandev.labyrinth.account.provider",
                        true
                    )
                } else {
                    ContentResolver.setSyncAutomatically(
                        profile.account,
                        "org.bandev.labyrinth.account.provider",
                        false
                    )
                }
                true
            }

        }

        private fun setAutoSyncVals() {
            val async = findPreference("async") as SwitchPreferenceCompat?
            (async ?: return).isChecked = ContentResolver.getSyncAutomatically(
                profile.account,
                "org.bandev.labyrinth.account.provider"
            )
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
