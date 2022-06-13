package com.ravisharma.runningtrack.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ravisharma.runningtrack.R
import com.ravisharma.runningtrack.databinding.FragmentRunDetailBinding
import com.ravisharma.runningtrack.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunDetailFragment : Fragment(R.layout.fragment_run_detail) {

    private lateinit var binding: FragmentRunDetailBinding

    private val viewModel : MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRunDetailBinding.bind(view)

        val detail = requireArguments().getString("detail").toString()

        viewModel.getSingleRun(detail.toInt()).observe(viewLifecycleOwner) {
            binding.detailsTextView.text = it.toString()
        }
    }
}