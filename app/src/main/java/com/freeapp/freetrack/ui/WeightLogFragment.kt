package com.freeapp.freetrack.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.freeapp.freetrack.data.AppDatabase
import com.freeapp.freetrack.data.WeightEntryEntity
import com.freeapp.freetrack.databinding.FragmentWeightLogBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WeightLogFragment : Fragment() {

    private var _binding: FragmentWeightLogBinding? = null
    private val binding get() = _binding!!
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etDate.setText(dateFormat.format(calendar.time))
        binding.etDate.setOnClickListener { showDatePicker() }

        val adapter = WeightEntryAdapter { entryToDelete ->
            lifecycleScope.launch {
                database.weightDao().delete(entryToDelete)
                Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvWeights.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWeights.adapter = adapter

        // Log Weight Button
        binding.btnSaveWeight.setOnClickListener {
            val weightStr = binding.etWeight.text.toString().trim()
            if (weightStr.isEmpty()) {
                binding.etWeight.error = "Please enter weight"
                return@setOnClickListener
            }

            val weightVal = weightStr.toFloatOrNull()
            if (weightVal == null || weightVal <= 0) {
                binding.etWeight.error = "Invalid weight"
                return@setOnClickListener
            }

            val dateStr = binding.etDate.text.toString().trim()
            val bodyFat = binding.etBodyFat.text.toString().toFloatOrNull()
            val notes = binding.etNotes.text.toString().trim()

            lifecycleScope.launch {
                val entry = WeightEntryEntity(
                    date = dateStr,
                    weightKg = weightVal,
                    bodyFatPct = bodyFat,
                    notes = if (notes.isNotEmpty()) notes else null
                )
                database.weightDao().insert(entry)
                binding.etWeight.text?.clear()
                binding.etBodyFat.text?.clear()
                binding.etNotes.text?.clear()
                Toast.makeText(requireContext(), "Weight saved locally!", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe 7-day rolling average
        viewLifecycleOwner.lifecycleScope.launch {
            database.weightDao().getSevenDayAverage().collectLatest { avg ->
                binding.tvSevenDayAvg.text = if (avg != null) "%.1f kg".format(avg) else "--"
            }
        }

        // Observe all weight entries
        viewLifecycleOwner.lifecycleScope.launch {
            database.weightDao().getAllEntries().collectLatest { list ->
                adapter.submitList(list)
                binding.tvTotalLogs.text = "${list.size} entries"
            }
        }
    }

    private fun showDatePicker() {
        val picker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                binding.etDate.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        picker.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}