/**

Labyrinth
Copyright (C) 2021  BanDev

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

 */

package org.bandev.labyrinth

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.octicons.Octicons
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import org.bandev.labyrinth.databinding.AboutActivityBinding

class AboutActivity : AppCompatActivity() {

    // Declare view binding variables
    private lateinit var binding: AboutActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup view binding
        binding = AboutActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set version code
        binding.version.text = BuildConfig.VERSION_NAME

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.navigationIcon =
            IconicsDrawable(this, Octicons.Icon.oct_chevron_left).apply {
                colorInt = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                sizeDp = 16
            }
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        val contributors = resources.getStringArray(R.array.contributors)
        val contributorsAdapter = ArrayAdapter(this, R.layout.contributors_list, contributors)
        with(binding.contributors) {
            adapter = contributorsAdapter
            divider = null
        }
        binding.contributors.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val ids = resources.getStringArray(R.array.contributors_id)
                val userId = ids[position]
                startActivity(
                    Intent(this, OtherProfile::class.java)
                        .putExtra("id", userId.toInt())
                )
            }

        val promises = resources.getStringArray(R.array.promises)
        val promisesAdapter = ArrayAdapter(this, R.layout.promises_list, promises)

        with(binding.promise) {
            adapter = promisesAdapter
            divider = null
            isClickable = false
        }

        with(binding.madeWith) {
            setOnClickListener { showGroup() }
            setOnClickListener { showGroup() }
        }
    }

    private fun showGroup() {
        startActivity(Intent(this, GroupsActivity::class.java).putExtra("id", 8650010))
    }
}
