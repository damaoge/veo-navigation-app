package com.veo.navigationapp

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.veo.navigationapp.databinding.DialogTripSummaryBinding
import com.veo.navigationapp.model.TripSummary

/**
 * Trip summary dialog
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Dialog displaying statistical information after navigation trip ends, including trip duration, total distance and other data
 */
class TripSummaryDialog : DialogFragment() {
    
    private var _binding: DialogTripSummaryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var tripSummary: TripSummary
    
    companion object {
        fun show(activity: FragmentActivity, tripSummary: TripSummary) {
            val dialog = TripSummaryDialog().apply {
                arguments = Bundle().apply {
                    putLong("duration", tripSummary.duration)
                    putFloat("distance", tripSummary.distance)
                }
            }
            dialog.show(activity.supportFragmentManager, "TripSummaryDialog")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        arguments?.let { args ->
            tripSummary = TripSummary(
                duration = args.getLong("duration"),
                distance = args.getFloat("distance"),
                route = emptyList() // Simplified processing
            )
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTripSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
    }
    
    private fun setupUI() {
        // Set trip data
        binding.tvTripDuration.text = tripSummary.getFormattedDuration()
        binding.tvTripDistance.text = tripSummary.getFormattedDistance()
        binding.tvAverageSpeed.text = tripSummary.getFormattedAverageSpeed()
        
        // Set button click events
        binding.btnClose.setOnClickListener {
            dismiss()
        }
        
        binding.btnShareTrip.setOnClickListener {
            shareTrip()
        }
    }
    
    private fun shareTrip() {
        val shareText = buildString {
            append("🚗 Trip Summary\n\n")
            append("⏱️ Trip Duration: ${tripSummary.getFormattedDuration()}\n")
            append("📏 Total Distance: ${tripSummary.getFormattedDistance()}\n")
            append("⚡ Average Speed: ${tripSummary.getFormattedAverageSpeed()}\n\n")
            append("Recorded by Veo Navigation App")
        }
        
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Trip"))
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}