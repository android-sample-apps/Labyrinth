package org.bandev.labyrinth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.bandev.labyrinth.account.Profile
import org.bandev.labyrinth.core.Appearance
import org.bandev.labyrinth.intro.First

class SettingsAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)

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
            val theme = findPreference<Preference>("theme") as ListPreference?
            theme?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    Appearance().setAppTheme(newValue.toString())
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
                        profile.delete()
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
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
