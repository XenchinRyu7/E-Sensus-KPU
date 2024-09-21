package com.saefulrdevs.esensus.ui.form

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.saefulrdevs.esensus.data.model.Citizens
import com.saefulrdevs.esensus.databinding.FragmentInputFormBinding
import com.saefulrdevs.esensus.utils.DatePickerUtils
import com.saefulrdevs.esensus.utils.ImagePickerUtils
import com.saefulrdevs.esensus.viewmodel.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.UUID

@AndroidEntryPoint
class InputFormFragment : Fragment() {

    private var _binding: FragmentInputFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by viewModels()

    private var photoFilePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputFormBinding.inflate(inflater, container, false)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Input Data Pemilu"
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (arguments != null) {
            val citizen = arguments?.getParcelable<Citizens>("citizens")
            citizen?.let {
                binding.nikET.setText(it.nik)
                binding.nameET.setText(it.name)
                binding.numberPhoneET.setText(it.numberPhone)
                binding.dateET.setText(it.date)
                binding.tvLocation.setText(it.location)

                val imageFile = File(it.photo)
                if (imageFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    if (bitmap != null) {
                        binding.imagePreview.setImageBitmap(bitmap)
                        binding.imagePreview.visibility = View.VISIBLE
                        Log.d("ImageCheck", "Bitmap successfully loaded")
                    } else {
                        Log.d("ImageCheck", "Failed to load bitmap")
                    }
                }

                if (it.gender == "L") {
                    binding.manSelected.isChecked = true
                } else if (it.gender == "P") {
                    binding.womanSelected.isChecked = true
                }

            }

            binding.btnSubmit.text = "Edit"
            binding.btnPhoto.text = "Ubah Foto"
            binding.btnSubmit.setOnClickListener {
                val nik = binding.nikET.text.toString()
                val name = binding.nameET.text.toString()
                val numberPhone = binding.numberPhoneET.text.toString()
                val date = binding.dateET.text.toString()
                val location = binding.tvLocation.text.toString()
                val gender = if (binding.manSelected.isChecked) "L" else "P"
                val photo = photoFilePath ?: ""

                val updateCitizen = Citizens(
                    id = citizen?.id ?: "",
                    nik = nik,
                    name = name,
                    numberPhone = numberPhone,
                    date = date,
                    location = location,
                    gender = gender,
                    photo = photo
                )

                if (nik.isNotEmpty() && name.isNotEmpty() && numberPhone.isNotEmpty() && date.isNotEmpty() && location.isNotEmpty() && gender.isNotEmpty() && photo.isNotEmpty()) {
                    updateCitizen(updateCitizen)
                } else {
                    Toast.makeText(
                        context,
                        "Semua field harus diisi!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }
        } else {
            binding.btnSubmit.setOnClickListener {
                val nik = binding.nikET.text.toString()
                val name = binding.nameET.text.toString()
                val numberPhone = binding.numberPhoneET.text.toString()
                val date = binding.dateET.text.toString()
                val location = binding.tvLocation.text.toString()
                val gender = if (binding.manSelected.isChecked) "L" else "P"
                val photo = photoFilePath ?: ""

                val newCitizen = Citizens(
                    id = UUID.randomUUID().toString(),
                    nik = nik,
                    name = name,
                    numberPhone = numberPhone,
                    date = date,
                    location = location,
                    gender = gender,
                    photo = photo
                )

                if (nik.isNotEmpty() && name.isNotEmpty() && numberPhone.isNotEmpty() && date.isNotEmpty() && location.isNotEmpty() && gender.isNotEmpty() && photo.isNotEmpty()) {
                    insertCitizen(newCitizen)
                } else {
                    Toast.makeText(
                        context,
                        "Semua field harus diisi!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }
        }

        binding.dateET.setOnClickListener {
            DatePickerUtils.showDatePicker(this, binding.dateET)
        }

        binding.btnPhoto.setOnClickListener {
            ImagePickerUtils.showImagePickerDialog(this, requireContext()) { photoFile ->
                photoFilePath = photoFile.absolutePath
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.imagePreview.setImageBitmap(bitmap)
                binding.imagePreview.visibility = View.VISIBLE
            }
        }

        return binding.root
    }

    private fun insertCitizen(citizen: Citizens) {
        viewModel.insertCitizen(citizen) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Data gagal disimpan", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun updateCitizen(citizen: Citizens) {
        viewModel.updateCitizen(citizen) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Data berhasil diupdate", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Data gagal diupdate", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    val savedFile =
                        ImagePickerUtils.saveImageToInternalStorage(requireContext(), imageBitmap)
                    photoFilePath = savedFile.absolutePath
                    Log.d("PhotoFilePath", "Photo file path: $photoFilePath")
                    binding.imagePreview.setImageBitmap(imageBitmap)
                    binding.imagePreview.visibility = View.VISIBLE
                }

                GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver,
                        imageUri
                    )
                    val savedFile =
                        ImagePickerUtils.saveImageToInternalStorage(requireContext(), bitmap)
                    photoFilePath = savedFile.absolutePath
                    Log.d("PhotoFilePath", "Photo file path: $photoFilePath")
                    binding.imagePreview.setImageBitmap(bitmap)
                    binding.imagePreview.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        const val CAMERA_REQUEST_CODE = 100
        const val GALLERY_REQUEST_CODE = 200
    }
}