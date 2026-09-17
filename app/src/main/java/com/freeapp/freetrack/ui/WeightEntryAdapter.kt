package com.freeapp.freetrack.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freeapp.freetrack.data.WeightEntryEntity
import com.freeapp.freetrack.databinding.ItemWeightBinding

class WeightEntryAdapter(private val onDelete: (WeightEntryEntity) -> Unit) :
    ListAdapter<WeightEntryEntity, WeightEntryAdapter.WeightViewHolder>(WeightDiffCallback()) {

    class WeightViewHolder(val binding: ItemWeightBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val binding = ItemWeightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            tvItemDate.text = item.date
            tvItemWeight.text = "${item.weightKg} kg"
            tvItemBodyFat.text = item.bodyFatPct?.let { "Body Fat: $it%" } ?: ""
            tvItemNotes.text = item.notes ?: ""
            root.setOnLongClickListener {
                onDelete(item)
                true
            }
        }
    }

    class WeightDiffCallback : DiffUtil.ItemCallback<WeightEntryEntity>() {
        override fun areItemsTheSame(oldItem: WeightEntryEntity, newItem: WeightEntryEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WeightEntryEntity, newItem: WeightEntryEntity): Boolean {
            return oldItem == newItem
        }
    }
}