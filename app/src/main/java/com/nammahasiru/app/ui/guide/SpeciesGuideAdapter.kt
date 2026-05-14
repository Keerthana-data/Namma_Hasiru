package com.nammahasiru.app.ui.guide

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammahasiru.app.data.SpeciesGuideRow
import com.nammahasiru.app.databinding.ItemSpeciesStatBinding

class SpeciesGuideAdapter : ListAdapter<SpeciesGuideRow, SpeciesGuideAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSpeciesStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(private val binding: ItemSpeciesStatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: SpeciesGuideRow) {
            binding.textSpeciesName.text = row.speciesName
            binding.textScientificName.text = row.scientificName
            binding.textLocality.text = "Locality: ${row.villageTag}"
            val pct = row.successPercent?.let { "$it%" } ?: "n/a"
            val outcomes = row.sprouted + row.died
            binding.textSuccessRate.text =
                "Success: $pct (${row.sprouted} sprouted / $outcomes outcomes)"
            binding.textCareInfo.text =
                "Soil: ${row.preferredSoil}  |  Water: ${row.waterNeed}"
            binding.textBenefits.text = "Benefits: ${row.benefits}"
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SpeciesGuideRow>() {
            override fun areItemsTheSame(a: SpeciesGuideRow, b: SpeciesGuideRow) =
                a.villageTag == b.villageTag && a.speciesName == b.speciesName

            override fun areContentsTheSame(a: SpeciesGuideRow, b: SpeciesGuideRow) = a == b
        }
    }
}
