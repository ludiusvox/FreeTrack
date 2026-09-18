package com.freeapp.freetrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.freeapp.freetrack.data.AppDatabase
import com.freeapp.freetrack.databinding.FragmentMapBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * 100% Free Map Screen powered by OpenStreetMap (OsmDroid).
 * Requires zero Google Maps API keys and zero billing accounts!
 */
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var trailPolyline: Polyline
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Configuration.getInstance().load(
            requireContext(),
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup OpenStreetMap View
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(16.5)

        // Configure trail polyline
        trailPolyline = Polyline(binding.mapView).apply {
            outlinePaint.color = 0xFF2563EB.toInt() // Blue #2563EB
            outlinePaint.strokeWidth = 10f
        }
        binding.mapView.overlays.add(trailPolyline)

        // Observe Room DB points in real-time
        viewLifecycleOwner.lifecycleScope.launch {
            database.gpsPointDao().getAllTrackPoints().collectLatest { points ->
                if (points.isNotEmpty()) {
                    val geoPoints = points.map { GeoPoint(it.latitude, it.longitude) }
                    trailPolyline.setPoints(geoPoints)

                    val latest = points.last()
                    binding.tvCoordinates.text = "%.5f, %.5f".format(latest.latitude, latest.longitude)
                    binding.tvBaroAlt.text = "%.1f m".format(latest.altitudeBaro)
                    binding.tvSpeed.text = "%.1f km/h".format(latest.speedKmh)
                    binding.tvPressure.text = "%.1f hPa".format(latest.pressureHpa)

                    // Center camera on latest point
                    binding.mapView.controller.animateTo(GeoPoint(latest.latitude, latest.longitude))
                } else {
                    trailPolyline.setPoints(emptyList())
                    binding.tvCoordinates.text = "Lat: --, Lon: --"
                    binding.tvBaroAlt.text = "Alt (Baro): -- m"
                    binding.tvSpeed.text = "Speed: -- km/h"
                    binding.tvPressure.text = "Pres: -- hPa"
                }
                binding.mapView.invalidate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}