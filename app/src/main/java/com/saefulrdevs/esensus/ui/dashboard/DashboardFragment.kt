package com.saefulrdevs.esensus.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saefulrdevs.esensus.R
import com.saefulrdevs.esensus.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Dashboard"
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val navController = findNavController()

        binding.cardInformation.setOnClickListener {
            navController.navigate(R.id.informationFragment)
        }

        binding.cardInputData.setOnClickListener {
            navController.navigate(R.id.inputFragment)
        }

        binding.cardListData.setOnClickListener {
            navController.navigate(R.id.listFragment)
        }

        binding.cardExit.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Exit")
                .setMessage("Apakah yakin ingin keluar dari aplikasi?")
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Exit") { dialog, which ->
                    requireActivity().finish()
                }
                .show()
        }
        return binding.root
    }

}