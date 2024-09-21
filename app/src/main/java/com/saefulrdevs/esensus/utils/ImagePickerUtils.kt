package com.saefulrdevs.esensus.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saefulrdevs.esensus.R
import com.saefulrdevs.esensus.ui.form.InputFormFragment.Companion.CAMERA_REQUEST_CODE
import com.saefulrdevs.esensus.ui.form.InputFormFragment.Companion.GALLERY_REQUEST_CODE
import java.io.File
import java.io.OutputStream

object ImagePickerUtils {

    fun showImagePickerDialog(
        fragment: Fragment,
        context: Context,
        onImageSelected: (File) -> Unit
    ) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_image_picker, null)
        val btnCamera: MaterialButton = dialogView.findViewById(R.id.btn_camera)
        val btnGallery: MaterialButton = dialogView.findViewById(R.id.btn_gallery)

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCamera.setOnClickListener {
            openCamera(fragment)
            dialog.dismiss()
        }

        btnGallery.setOnClickListener {
            openGallery(fragment)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openCamera(fragment: Fragment) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        fragment.startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery(fragment: Fragment) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        fragment.startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    fun saveImageToInternalStorage(context: Context, bitmap: Bitmap): File {
        val filename = "${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        val outputStream: OutputStream = file.outputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }
}
