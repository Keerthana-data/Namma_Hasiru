package com.nammahasiru.app.ui.detail

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.nammahasiru.app.NammaHasiruApp
import com.nammahasiru.app.R
import com.nammahasiru.app.data.TreeRepository
import com.nammahasiru.app.data.TreeStatus
import com.nammahasiru.app.databinding.FragmentTreeDetailBinding
import com.nammahasiru.app.reminders.CheckupReminderScheduler
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TreeDetailFragment : Fragment() {

    private var _binding: FragmentTreeDetailBinding? = null
    private val binding get() = _binding!!

    private val repo: TreeRepository
        get() = (requireActivity().application as NammaHasiruApp).treeRepository

    private val treeId: Long by lazy {
        arguments?.getLong("treeId") ?: -1L
    }

    private var growthUri: Uri? = null
    private var growthFile: File? = null

    private val takeGrowth = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) growthUri?.let { binding.imageGrowth.load(it) }
    }

    private val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchGrowthPhoto()
        else Toast.makeText(requireContext(), "Camera permission needed", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        _binding = FragmentTreeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (treeId < 0) {
            Toast.makeText(requireContext(), "Missing tree", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        binding.buttonGrowthPhoto.setOnClickListener {
            val ok = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            if (ok) launchGrowthPhoto()
            else requestCamera.launch(Manifest.permission.CAMERA)
        }

        binding.buttonSaveStatus.setOnClickListener {
            val checked = binding.toggleStatus.checkedButtonId
            if (checked == View.NO_ID) {
                Toast.makeText(requireContext(), "Choose sprouted or died", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val current = repo.getTree(treeId) ?: return@launch
                val next = when (checked) {
                    R.id.btn_sprouted -> TreeStatus.SPROUTED
                    R.id.btn_died -> TreeStatus.DIED
                    else -> return@launch
                }
                val growthPath = growthFile?.absolutePath ?: current.growthPhotoPath
                val updated = current.copy(
                    status = next.storageKey,
                    growthPhotoPath = growthPath,
                    lastUpdatedMillis = System.currentTimeMillis(),
                )
                repo.update(updated)
                CheckupReminderScheduler.cancelForTree(requireContext().applicationContext, treeId)
                Toast.makeText(requireContext(), "Status saved", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.observeTree(treeId).collect { tree ->
                    if (tree == null) return@collect
                    binding.textDetailSpecies.text = tree.speciesName
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val meta = buildString {
                        append("Planted ")
                        append(sdf.format(Date(tree.plantedAtMillis)))
                        append(" - ")
                        append(tree.villageTag)
                        if (tree.addressLine.isNotBlank()) {
                            append("\n")
                            append(tree.addressLine)
                        }
                    }
                    binding.textDetailMeta.text = meta

                    tree.photoPath?.let { binding.imagePlanting.load(File(it)) }
                        ?: binding.imagePlanting.setImageResource(android.R.drawable.ic_menu_gallery)

                    tree.growthPhotoPath?.let { binding.imageGrowth.load(File(it)) }

                    when (TreeStatus.fromStorage(tree.status)) {
                        TreeStatus.SPROUTED -> binding.toggleStatus.check(R.id.btn_sprouted)
                        TreeStatus.DIED -> binding.toggleStatus.check(R.id.btn_died)
                        TreeStatus.PLANTED -> binding.toggleStatus.clearChecked()
                    }
                }
            }
        }
    }

    private fun launchGrowthPhoto() {
        val dir = File(requireContext().cacheDir, "photos").apply { mkdirs() }
        val file = File(dir, "growth_$treeId.jpg")
        growthFile = file
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.nammahasiru.app.fileprovider",
            file,
        )
        growthUri = uri
        takeGrowth.launch(uri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
