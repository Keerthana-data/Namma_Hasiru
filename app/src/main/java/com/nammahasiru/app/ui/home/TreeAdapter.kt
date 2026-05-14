package com.nammahasiru.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nammahasiru.app.data.TreeEntity
import com.nammahasiru.app.data.TreeStatus
import com.nammahasiru.app.databinding.ItemTreeRowBinding
import java.io.File

class TreeAdapter(
    private val onClick: (Long) -> Unit,
) : ListAdapter<TreeEntity, TreeAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        val binding = ItemTreeRowBinding.inflate(inf, parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemTreeRowBinding,
        private val onClick: (Long) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TreeEntity) {
            binding.textSpecies.text = item.speciesName
            binding.textVillage.text = item.villageTag.ifBlank { "Community" }
            val label = when (TreeStatus.fromStorage(item.status)) {
                TreeStatus.PLANTED -> "Awaiting check-up"
                TreeStatus.SPROUTED -> "Sprouted"
                TreeStatus.DIED -> "Did not survive"
            }
            binding.textStatus.text = label
            val path = item.photoPath
            if (!path.isNullOrBlank()) {
                binding.imageThumb.load(File(path))
            } else {
                binding.imageThumb.setImageResource(android.R.drawable.ic_menu_gallery)
            }
            binding.root.setOnClickListener { onClick(item.id) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TreeEntity>() {
            override fun areItemsTheSame(a: TreeEntity, b: TreeEntity) = a.id == b.id
            override fun areContentsTheSame(a: TreeEntity, b: TreeEntity) = a == b
        }
    }
}
