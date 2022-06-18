package com.ravisharma.runningtrack.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ravisharma.runningtrack.R
import com.ravisharma.runningtrack.databinding.FragmentRunDetailBinding
import com.ravisharma.runningtrack.other.TrackingUtility
import com.ravisharma.runningtrack.ui.MainActivity
import com.ravisharma.runningtrack.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class RunDetailFragment : Fragment(R.layout.fragment_run_detail) {

    private lateinit var binding: FragmentRunDetailBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRunDetailBinding.bind(view)

        val detail = requireArguments().getString("detail").toString()

        viewModel.getSingleRun(detail.toInt()).observe(viewLifecycleOwner) { run ->
            binding.apply {
                ivRunImage.setImageBitmap(run.img)

                val distanceInKm = "${run.distanceInMeters / 1000f}km"
                val avgSpeed = "${run.avgSpeedInKMH}km/h"
                val caloriesBurned = "${run.caloriesBurned}kcal"

                tvDistance.text = distanceInKm
                tvSpeed.text = avgSpeed
                tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
                tvCalories.text = caloriesBurned

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = run.timestamp
                }
                val dateFormat = SimpleDateFormat("dd/MMM/yy - hh:mm aa", Locale.getDefault())
                val date = dateFormat.format(calendar.time)

                (requireActivity() as MainActivity).setToolbarTitle(date)
            }
        }
    }
}