package org.bandev.labyrinth.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.bandev.labyrinth.OthersProfileAct
import org.bandev.labyrinth.R
import org.bandev.labyrinth.account.Profile
import org.json.JSONArray
import org.json.JSONObject

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

     var profile = Profile()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        profile.login(requireContext(),0)
        var token=profile.getData("token")
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)


        val button = root.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            Toast.makeText(requireContext(), "Sending req ...", LENGTH_SHORT).show()
            val text = root.findViewById<EditText>(R.id.hi).text
            AndroidNetworking.initialize(context)
            Toast.makeText(requireContext(), text, LENGTH_SHORT).show()
            AndroidNetworking.get("https://gitlab.com/api/v4/users?username=$text")
                    .build()
                    .getAsJSONArray(object : JSONArrayRequestListener {
                        override fun onResponse(response: JSONArray?) {
                            var ids = response!![0].toString()

                            var id = JSONObject(ids).getInt("id")
                            var i = Intent(requireContext(), OthersProfileAct::class.java)
                            i.putExtra("data", "{" +
                                    "   'id': $id" +
                                    "}")
                            startActivity(i)

                        }

                        override fun onError(error: ANError?) {
                            // handle error
                            Toast.makeText(requireContext(), error.toString(), LENGTH_SHORT).show()
                        }

                    })
        }

        return root
    }
}