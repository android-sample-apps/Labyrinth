package org.bandev.labyrinth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.bandev.labyrinth.databinding.ChipBottomSheetBinding


class ChipInfoSheet : BottomSheetDialogFragment() {

    // The layout for this fragment
    private lateinit var binding: ChipBottomSheetBinding

    var icon: Int = 0
    var statistic: String = ""
    var description: String = ""

    // When the view is created
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChipBottomSheetBinding.inflate(layoutInflater)

        binding.icon.setImageDrawable(ContextCompat.getDrawable(requireContext(), icon))
        binding.statistic.text = statistic
        binding.description.text = description

        // Return the inflated view to the activity
        return binding.root
    }

    // Override the getTheme function to provide a custom theme
    override fun getTheme(): Int = R.style.Theme_NoWiredStrapInNavigationBar

}