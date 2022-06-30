package com.ravisharma.runningtrack.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.ravisharma.runningtrack.R
import com.ravisharma.runningtrack.databinding.FragmentSettingsBinding
import com.ravisharma.runningtrack.other.Constants.KEY_NAME
import com.ravisharma.runningtrack.other.Constants.KEY_WEIGHT
import com.ravisharma.runningtrack.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        loadFieldsFromSharedPref()
        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                Snackbar.make(view, "Saved Changes", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(view, "Please fill all the fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 70f)
        binding.etName.setText(name.toString())
        binding.etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }

        sharedPreferences.edit().apply {
            putString(KEY_NAME, name.trim())
            putFloat(KEY_WEIGHT, weight.toFloat())
            apply()
        }

        val toolbarText = "Hi, $name!"

        (requireActivity() as MainActivity).setToolbarTitle(toolbarText)

        return true
    }
}