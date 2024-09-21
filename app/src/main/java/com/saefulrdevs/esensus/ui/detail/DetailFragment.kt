package com.saefulrdevs.esensus.ui.detail

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.saefulrdevs.esensus.R
import com.saefulrdevs.esensus.data.model.Citizens
import com.saefulrdevs.esensus.databinding.FragmentDetailBinding
import com.saefulrdevs.esensus.utils.ImagePickerUtils
import java.io.File

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

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
            binding.tvLocation.append(it.location)
        }

        binding.btnEdit.setOnClickListener{
            val bundle = Bundle().apply {
                putParcelable("citizens", citizen)
            }
            Navigation.findNavController(it).navigate(R.id.inputFragment, bundle)
        }

        return binding.root
    }

}