package com.ravisharma.runningtrack.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.ravisharma.runningtrack.R
import com.ravisharma.runningtrack.databinding.FragmentTrackingBinding
import com.ravisharma.runningtrack.db.Run
import com.ravisharma.runningtrack.other.Constants.ACTION_PAUSE_SERVICE
import com.ravisharma.runningtrack.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.ravisharma.runningtrack.other.Constants.ACTION_STOP_SERVICE
import com.ravisharma.runningtrack.other.Constants.MAP_ZOOM
import com.ravisharma.runningtrack.other.Constants.POLYLINE_COLOR
import com.ravisharma.runningtrack.other.Constants.POLYLINE_WIDTH
import com.ravisharma.runningtrack.other.TrackingUtility
import com.ravisharma.runningtrack.services.Polyline
import com.ravisharma.runningtrack.services.TrackingService
import com.ravisharma.runningtrack.ui.MainActivity
import com.ravisharma.runningtrack.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG_TAG = "CancelDialog"

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private lateinit var binding: FragmentTrackingBinding

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false

    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var currentTimeMillis = 0L

    private var menu: Menu? = null

    @set:Inject
    var weight = 70f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrackingBinding.bind(view)
        binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        (requireActivity() as MainActivity).showHideAppBarLayout(false)
//        appBarLayout.visibility = View.GONE

        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG
            ) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        binding.fabCancelRun.setOnClickListener {
            showCancelTrackingDialog()
        }

        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeMillis, true)
            binding.tvTimer.text = formattedTime
            if (currentTimeMillis > 0L) {
                showCancelButton()
            }
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        binding.tvTimer.text = "00:00:00:00"
        binding.btnToggleRun.text = "START"
        binding.fabCancelRun.visibility = View.GONE
        binding.btnFinishRun.visibility = View.GONE
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().popBackStack()
        (requireActivity() as MainActivity).showHideAppBarLayout(true)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeMillis > 0L) {
            binding.btnToggleRun.text = "RESUME"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            binding.btnToggleRun.text = "STOP"
            binding.fabCancelRun.visibility = View.VISIBLE
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun showCancelButton() {
        if (!isTracking) {
            binding.btnToggleRun.text = "RESUME"
        }
        binding.fabCancelRun.visibility = View.VISIBLE
        binding.btnFinishRun.visibility = View.VISIBLE
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyLine in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyLine).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (currentTimeMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

            val run = Run(
                bmp,
                dateTimeStamp,
                avgSpeed,
                distanceInMeters,
                currentTimeMillis,
                caloriesBurned
            )
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run Saved Successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()

            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)

        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}