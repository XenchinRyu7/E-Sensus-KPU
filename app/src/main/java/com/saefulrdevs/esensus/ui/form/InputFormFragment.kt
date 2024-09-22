package com.saefulrdevs.esensus.ui.form

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saefulrdevs.esensus.R
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
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var checkFormRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputFormBinding.inflate(inflater, container, false)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Input Data Pemilu"
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val citizen = arguments?.getParcelable<Citizens>("citizens")
        if (citizen != null) {
            setupEditMode(citizen)
        } else {
            setupInputMode()
        }

        setupTextWatchers()

        binding.btnOpenMaps.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.mapsFragment)
        }

        binding.dateET.setOnClickListener {
            DatePickerUtils.showDatePicker(this, binding.dateET)
        }

        binding.btnOpenMaps.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.mapsFragment)
        }

        setFragmentResultListener("locationRequestKey") { _, bundle ->
            val latitude = bundle.getString("latitude")
            val longitude = bundle.getString("longitude")
            binding.tfLatitude.setText(latitude)
            binding.tfLongitude.setText(longitude)
            checkFormFields()
        }

        binding.btnPhoto.setOnClickListener {
            ImagePickerUtils.showImagePickerDialog(this, requireContext()) { photoFile ->

                val oldPhotoFilePath = photoFilePath

                photoFilePath = photoFile.absolutePath

                if (File(photoFilePath!!).exists()) {
                    deleteOldPhoto(oldPhotoFilePath)
                }

                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.imagePreview.setImageBitmap(bitmap)
                binding.imagePreview.visibility = View.VISIBLE
            }
        }


        checkFormRunnable = object : Runnable {
            override fun run() {
                checkFormFields()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(checkFormRunnable)

        return binding.root
    }

    private fun deleteOldPhoto(oldPhotoFilePath: String?) {
        oldPhotoFilePath?.let {
            val oldFile = File(it)
            if (oldFile.exists()) {
                val isDeleted = oldFile.delete()
                if (isDeleted) {
                    Log.d("PhotoUpdate", "Old photo deleted")
                } else {
                    Log.d("PhotoUpdate", "Failed to delete old photo")
                }
            } else {
                Log.d("PhotoUpdate", "Old photo does not exist")
            }
        }
    }


    private fun setupEditMode(citizen: Citizens) {
        binding.btnSubmit.text = "Edit"
        binding.btnPhoto.text = "Ubah Foto"
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Edit Data Pemilu"
        populateFieldsWithCitizenData(citizen)

        binding.btnSubmit.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Data")
                .setMessage("Apakah anda yakin ingin mengubah data ini?")
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Edit") { dialog, which ->
                    updateCitizen(createCitizenFromFields(citizen.id ?: ""))
                }
                .show()
        }
    }

    private fun setupInputMode() {
        binding.btnSubmit.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tambah Data")
                .setMessage("Apakah sudah yakin?")
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Tambah") { dialog, which ->
                    insertCitizen(createCitizenFromFields(UUID.randomUUID().toString()))
                }
                .show()
        }
    }

    private fun populateFieldsWithCitizenData(citizen: Citizens) {
        binding.nikET.setText(citizen.nik)
        binding.nameET.setText(citizen.name)
        binding.numberPhoneET.setText(citizen.numberPhone)
        binding.dateET.setText(citizen.date)
        val fullLocation = citizen.location.split(". Longitude: ", " Latitude: ")
        if (fullLocation.size == 3) {
            binding.tvLocation.setText(fullLocation[0])
            binding.tfLongitude.setText(fullLocation[1])
            binding.tfLatitude.setText(fullLocation[2])
        }
        photoFilePath = citizen.photo
        if (File(photoFilePath!!).exists()) {
            val bitmap = BitmapFactory.decodeFile(photoFilePath)
            binding.imagePreview.setImageBitmap(bitmap)
            binding.imagePreview.visibility = View.VISIBLE
        }
        if (citizen.gender == "L") binding.manSelected.isChecked = true
        else if (citizen.gender == "P") binding.womanSelected.isChecked = true
    }

    private fun setupTextWatchers() {
        val fields = listOf(
            binding.nikET,
            binding.nameET,
            binding.numberPhoneET,
            binding.dateET,
            binding.tvLocation,
            binding.tfLongitude,
            binding.tfLatitude
        )

        fields.forEach { field ->
            field.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    checkFormFields()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    private fun createCitizenFromFields(id: String): Citizens {
        val nik = binding.nikET.text.toString()
        val name = binding.nameET.text.toString()
        val numberPhone = binding.numberPhoneET.text.toString()
        val date = binding.dateET.text.toString()
        val location = binding.tvLocation.text.toString()
        val longitude = binding.tfLongitude.text.toString()
        val latitude = binding.tfLatitude.text.toString()
        val gender = if (binding.manSelected.isChecked) "L" else "P"
        val photo = photoFilePath ?: ""

        return Citizens(
            id = id,
            nik = nik,
            name = name,
            numberPhone = numberPhone,
            date = date,
            location = "$location. Longitude: $longitude Latitude: $latitude",
            gender = gender,
            photo = photo
        )
    }

    private fun checkFormFields() {
        val isValid = listOf(
            binding.nikET.text.toString(),
            binding.nameET.text.toString(),
            binding.numberPhoneET.text.toString(),
            binding.dateET.text.toString(),
            binding.tvLocation.text.toString(),
            binding.tfLongitude.text.toString(),
            binding.tfLatitude.text.toString(),
            photoFilePath ?: ""
        ).all { it.isNotEmpty() }

        binding.btnSubmit.isEnabled = isValid
    }

    private fun insertCitizen(citizen: Citizens) {
        viewModel.insertCitizen(citizen) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(R.id.dashboardFragment)
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
                findNavController().navigate(R.id.listFragment)
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

                    val oldPhotoFilePath = photoFilePath
                    photoFilePath = savedFile.absolutePath

                    if (File(photoFilePath!!).exists()) {
                        deleteOldPhoto(oldPhotoFilePath)
                    }

                    binding.imagePreview.setImageBitmap(imageBitmap)
                    binding.imagePreview.visibility = View.VISIBLE
                    checkFormFields()
                }

                GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver,
                        imageUri
                    )
                    val savedFile =
                        ImagePickerUtils.saveImageToInternalStorage(requireContext(), bitmap)

                    val oldPhotoFilePath = photoFilePath
                    photoFilePath = savedFile.absolutePath

                    if (File(photoFilePath!!).exists()) {
                        deleteOldPhoto(oldPhotoFilePath)
                    }

                    binding.imagePreview.setImageBitmap(bitmap)
                    binding.imagePreview.visibility = View.VISIBLE
                    checkFormFields()
                }
            }
        }
    }


    companion object {
        const val CAMERA_REQUEST_CODE = 100
        const val GALLERY_REQUEST_CODE = 200
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(checkFormRunnable)
        _binding = null
    }
}







