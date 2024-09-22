package com.saefulrdevs.esensus.ui.detail

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saefulrdevs.esensus.R
import com.saefulrdevs.esensus.data.model.Citizens
import com.saefulrdevs.esensus.databinding.FragmentDetailBinding
import com.saefulrdevs.esensus.viewmodel.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Detail Pemilu"
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val citizen = arguments?.getParcelable<Citizens>("citizens")

        citizen?.let {
            val imageFile = File(it.photo)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bitmap != null) {
                    binding.imagePreview.setImageBitmap(bitmap)
                    Log.d("ImageCheck", "Bitmap successfully loaded")
                } else {
                    Log.d("ImageCheck", "Failed to load bitmap")
                }
            }

            val gender = it.gender

            if (gender == "L") {
                binding.tvGender.append("Laki-laki")
            } else if (gender == "P") {
                binding.tvGender.append("Perempuan")
            }

            binding.tvNik.append(it.nik)
            binding.tvName.append(it.name)
            binding.tvNumberPhone.append(it.numberPhone)
            binding.tvDate.append(it.date)
            val fullLocation = citizen.location.split(". Longitude: ", " Latitude: ")
            if (fullLocation.size == 3) {
                binding.tvLocation.append(fullLocation[0])
                binding.tvLongitude.append(fullLocation[1])
                binding.tvLatitude.append(fullLocation[2])
            }
        }

        binding.btnEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("citizens", citizen)
            }
            Navigation.findNavController(it).navigate(R.id.inputFragment, bundle)
        }

        binding.btnDelete.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Data")
                .setMessage("Apakah anda yakin ingin menghapus data ini?")
                .setNegativeButton("Batal") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Delete") { dialog, which ->
                    if (citizen != null) {
                        viewModel.deleteCitizen(citizen) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(
                                    requireContext(),
                                    "Data Berhasil dihapus",
                                    Toast.LENGTH_SHORT
                                ).show()
                                citizen.photo.let {
                                    val oldFile = File(it)
                                    if (oldFile.exists()) {
                                        oldFile.delete()
                                        Log.d("PhotoUpdate", "Old photo deleted")
                                    }
                                }
                                findNavController().navigate(R.id.dashboardFragment)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Data gagal dihapus",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            }
                        }
                    }
                }
                .show()

        }

        return binding.root
    }

}