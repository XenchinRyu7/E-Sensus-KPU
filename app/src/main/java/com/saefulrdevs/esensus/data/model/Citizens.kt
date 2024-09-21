package com.saefulrdevs.esensus.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "citizens")
data class Citizens(
    @PrimaryKey(autoGenerate = false) val id: String = UUID.randomUUID().toString(),
    val nik: String,
    val name: String,
    val numberPhone: String,
    val gender: String,
    val date: String,
    val location: String,
    val photo: String
) : Parcelable
