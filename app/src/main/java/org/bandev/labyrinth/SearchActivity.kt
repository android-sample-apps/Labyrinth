package org.bandev.labyrinth

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle

class SearchActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->

            }
        }
    }


}