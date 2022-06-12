package com.ravisharma.runningtrack.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ravisharma.runningtrack.R
import com.ravisharma.runningtrack.databinding.FragmentSetupBinding
import com.ravisharma.runningtrack.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.ravisharma.runningtrack.other.Constants.KEY_NAME
import com.ravisharma.runningtrack.other.Constants.KEY_WEIGHT
import com.ravisharma.runningtrack.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private lateinit var binding: FragmentSetupBinding

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetupBinding.bind(view)
        if (!isFirstAppOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        binding.fab.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.setupFragment, true)
                    .build()

                findNavController().navigate(
                    R.id.action_setupFragment_to_runFragment,
                    savedInstanceState,
                    navOptions
                )
            } else {
                Snackbar.make(requireView(), "Please enter all field", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }

        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()

        val toolbarText = "Let's go, $name!"
        (requireActivity() as MainActivity).setToolbarTitle(toolbarText)
        return true
    }
}