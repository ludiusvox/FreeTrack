package com.freeapp.freetrack.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.freeapp.freetrack.R
import com.freeapp.freetrack.data.AppDatabase
import com.freeapp.freetrack.databinding.FragmentElevationChartBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Elevation profile view using MPAndroidChart.
 * Plots cumulative distance (km) on X-axis vs barometric altitude (meters) on Y-axis.
 */
class ElevationChartFragment : Fragment() {

    private var _binding: FragmentElevationChartBinding? = null
    private val binding get() = _binding!!
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentElevationChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart(binding.elevationChart)

        viewLifecycleOwner.lifecycleScope.launch {
            database.gpsPointDao().getAllTrackPoints().collectLatest { points ->
                if (points.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.elevationChart.visibility = View.GONE
                    return@collectLatest
                }

                binding.tvEmptyState.visibility = View.GONE
                binding.elevationChart.visibility = View.VISIBLE

                var totalGain = 0.0
                var totalLoss = 0.0
                var minAlt = Double.MAX_VALUE
                var maxAlt = Double.MIN_VALUE

                val entries = mutableListOf<Entry>()
                for (i in points.indices) {
                    val p = points[i]
                    val alt = p.altitudeBaro
                    minAlt = minOf(minAlt, alt)
                    maxAlt = maxOf(maxAlt, alt)

                    if (i > 0) {
                        val diff = alt - points[i - 1].altitudeBaro
                        if (diff > 0) totalGain += diff else totalLoss += kotlin.math.abs(diff)
                    }

                    // X = distance in km, Y = elevation in meters
                    entries.add(Entry(p.distanceKm.toFloat(), alt.toFloat()))
                }

                binding.tvGain.text = "+${totalGain.roundToInt()} m"
                binding.tvLoss.text = "-${totalLoss.roundToInt()} m"
                binding.tvMaxAlt.text = "${maxAlt.roundToInt()} m"
                binding.tvMinAlt.text = "${minAlt.roundToInt()} m"

                updateChartData(entries)
            }
        }
    }

    private fun setupChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                gridColor = 0x22FFFFFF
                textColor = Color.DKGRAY
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = 0x22FFFFFF
                textColor = Color.DKGRAY
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun updateChartData(entries: List<Entry>) {
        val dataSet = LineDataSet(entries, "Barometric Elevation").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f
            color = ContextCompat.getColor(requireContext(), R.color.primary)
            lineWidth = 2.5f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient_fill)
        }

        binding.elevationChart.data = LineData(dataSet)
        binding.elevationChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}