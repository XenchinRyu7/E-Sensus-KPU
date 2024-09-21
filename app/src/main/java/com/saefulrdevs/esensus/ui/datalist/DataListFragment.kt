package com.saefulrdevs.esensus.ui.datalist

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.saefulrdevs.esensus.databinding.FragmentDataListBinding
import com.saefulrdevs.esensus.viewmodel.CitizensAdapter
import com.saefulrdevs.esensus.viewmodel.ViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataListFragment : Fragment() {

    private var _binding: FragmentDataListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by viewModels()
    private lateinit var citizensAdapter: CitizensAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataListBinding.inflate(inflater, container, false)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Data Pemilu"
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navController = findNavController()

        citizensAdapter = CitizensAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citizensAdapter
        }

        viewModel.citizensList.observe(viewLifecycleOwner) { citizensList ->
            Log.d("CitizensList", "Citizens list size: ${citizensList.size}")
            citizensList.forEach {
                Log.d("CitizensList", "Citizen ID: ${it.id}, NIK: ${it.nik}, Name: ${it.name}")
            }
            if (citizensList.isNullOrEmpty()) {
                binding.tvEmptyMessage.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvEmptyMessage.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                citizensAdapter.setCitizensList(citizensList)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}