package com.nammahasiru.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nammahasiru.app.NammaHasiruApp
import com.nammahasiru.app.R
import com.nammahasiru.app.data.TreeRepository
import com.nammahasiru.app.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences("nh_prefs", 0)
    }

    private val repo: TreeRepository
        get() = (requireActivity().application as NammaHasiruApp).treeRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = TreeAdapter { id ->
            val bundle = Bundle().apply { putLong("treeId", id) }
            findNavController().navigate(R.id.action_home_to_treeDetail, bundle)
        }
        binding.recyclerTrees.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTrees.adapter = adapter

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addPlant)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.observeTrees().collect { trees ->
                    adapter.submitList(trees)
                    val village = prefs.getString("last_village", "") ?: ""
                    val snap = TreeRepository.aggregateSurvival(trees, village)
                    val pctText = snap.survivalPercent?.let { "$it%" } ?: "?"
                    binding.textSurvivalScore.text = buildString {
                        append(snap.villageLabel)
                        append(" survival: ")
                        append(pctText)
                        append(" ? checked ")
                        append(snap.checkedCount)
                        append(" (sprouted ")
                        append(snap.sproutedCount)
                        append(", died ")
                        append(snap.diedCount)
                        append(")")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // prefs may change after saving a plant ? Flow emits again on DB change; village label reads fresh on next collect
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
