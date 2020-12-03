package org.bandev.labyrinth.adapters

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.view.isGone
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.androidnetworking.common.ANResponse
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.bandev.labyrinth.CircleTransform
import org.bandev.labyrinth.R
import org.bandev.labyrinth.databinding.FileViewAdapterBinding
import org.json.JSONObject


class BranchSelectorAdapter(private val context: Activity, private val text: Array<String?>, val currentBranch: String) :
    BaseAdapter() {


    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {

        val inflater = context.layoutInflater

        val rowView = inflater.inflate(R.layout.branch_selector_adapter, null)

        val name = rowView.findViewById<TextView>(R.id.name)
        val icon = rowView.findViewById<ImageView>(R.id.icon)

        val jsonObj = JSONObject(text[p0] ?: return null)
        name.text = jsonObj.getString("name")

        if (currentBranch == name.text) {
            icon.isGone = false
        }

        return rowView
    }

    override fun getItem(p0: Int): String? {
        return text[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return text.size
    }
}