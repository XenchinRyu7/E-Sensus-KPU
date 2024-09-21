package com.saefulrdevs.esensus.viewmodel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.saefulrdevs.esensus.R
import com.saefulrdevs.esensus.data.model.Citizens
import com.saefulrdevs.esensus.databinding.CardCitizenBinding

class CitizensAdapter : RecyclerView.Adapter<CitizensAdapter.ViewHolder>() {

    private var citizensList = listOf<Citizens>()

    class ViewHolder(val binding: CardCitizenBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardCitizenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val citizens = citizensList[position]
        holder.binding.apply {
            nikTv.append(citizens.nik)
            fullNameTv.append(citizens.name)
        }
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("citizens", citizens)
            }
            Navigation.findNavController(it).navigate(R.id.detailFragment, bundle)
        }
    }

    override fun getItemCount(): Int {
        return citizensList.size
    }

    fun setCitizensList(citizensList: List<Citizens>) {
        this.citizensList = citizensList
        notifyDataSetChanged()
    }
}