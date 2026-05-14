package com.nammahasiru.app.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.nammahasiru.app.NammaHasiruApp
import com.nammahasiru.app.R
import com.nammahasiru.app.data.TreeEntity
import com.nammahasiru.app.data.TreeRepository
import com.nammahasiru.app.databinding.FragmentTreeMapBinding
import kotlinx.coroutines.launch

class TreeMapFragment : Fragment() {

    private var _binding: FragmentTreeMapBinding? = null
    private val binding get() = _binding!!

    private val repo: TreeRepository
        get() = (requireActivity().application as NammaHasiruApp).treeRepository

    private var map: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        _binding = FragmentTreeMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFrag = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFrag.getMapAsync { googleMap ->
            map = googleMap
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.setOnMarkerClickListener { m ->
                val id = m.tag as? Long ?: return@setOnMarkerClickListener false
                val bundle = Bundle().apply { putLong("treeId", id) }
                findNavController().navigate(R.id.action_map_to_treeDetail, bundle)
                true
            }
            observeTrees()
        }
    }

    private fun observeTrees() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.observeTrees().collect { trees ->
                    val gm = map ?: return@collect
                    gm.clear()
                    if (trees.isEmpty()) return@collect
                    val b = LatLngBounds.builder()
                    for (t in trees) {
                        val pos = com.google.android.gms.maps.model.LatLng(t.latitude, t.longitude)
                        b.include(pos)
                        gm.addMarker(
                            MarkerOptions()
                                .position(pos)
                                .title(t.speciesName)
                                .snippet(t.villageTag)
                        )?.apply { tag = t.id }
                    }
                    runCatching {
                        gm.animateCamera(CameraUpdateFactory.newLatLngBounds(b.build(), 96))
                    }.onFailure {
                        val t = trees.first()
                        gm.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                com.google.android.gms.maps.model.LatLng(t.latitude, t.longitude),
                                12f,
                            ),
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
