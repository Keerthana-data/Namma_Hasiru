package com.nammahasiru.app.ui.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nammahasiru.app.NammaHasiruApp
import com.nammahasiru.app.data.TreeRepository
import com.nammahasiru.app.databinding.FragmentSpeciesGuideBinding
import kotlinx.coroutines.launch

class SpeciesGuideFragment : Fragment() {

    private var _binding: FragmentSpeciesGuideBinding? = null
    private val binding get() = _binding!!

    private val repo: TreeRepository
        get() = (requireActivity().application as NammaHasiruApp).treeRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        _binding = FragmentSpeciesGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SpeciesGuideAdapter()
        binding.recyclerGuide.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerGuide.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.observeSpeciesGuide().collect { rows ->
                    adapter.submitList(rows)
                    val empty = rows.isEmpty()
                    binding.textEmptyGuide.visibility = if (empty) View.VISIBLE else View.GONE
                    binding.recyclerGuide.visibility = if (empty) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
