package com.saefulrdevs.esensus.utils

import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.saefulrdevs.esensus.utils.DateUtils.formatDate

object DatePickerUtils {
    fun showDatePicker(
        fragment: Fragment,
        editText: TextInputEditText? = null,
        cardView: CardView? = null,
        textView: TextView? = null
    ) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .build()

        datePicker.show(fragment.parentFragmentManager, "MATERIAL_DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val formattedDate = formatDate(selection)

            editText?.setText(formattedDate)
            textView?.text = formattedDate
        }
    }
}
