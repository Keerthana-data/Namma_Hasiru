package com.nammahasiru.app.ui.add

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.gms.maps.model.LatLng
import com.nammahasiru.app.NammaHasiruApp
import com.nammahasiru.app.data.TreeEntity
import com.nammahasiru.app.data.TreeRepository
import com.nammahasiru.app.data.TreeStatus
import com.nammahasiru.app.databinding.FragmentAddPlantBinding
import com.nammahasiru.app.location.LocationHelper
import com.nammahasiru.app.reminders.CheckupReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class AddPlantFragment : Fragment() {

    private var _binding: FragmentAddPlantBinding? = null
    private val binding get() = _binding!!

    private val repo: TreeRepository
        get() = (requireActivity().application as NammaHasiruApp).treeRepository

    private val prefs by lazy { requireContext().getSharedPreferences("nh_prefs", 0) }
    private val locationHelper by lazy { LocationHelper(requireContext()) }

    private var latLng: LatLng? = null
    private var locationAddress: String = ""
    private var photoUri: Uri? = null
    private var photoFile: File? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) photoUri?.let { binding.imagePreview.load(it) }
    }

    private val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) preparePhotoFileAndLaunch()
        else Toast.makeText(requireContext(), "Camera permission needed", Toast.LENGTH_SHORT).show()
    }

    private val requestLocation = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { map ->
        val ok = map[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            map[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (ok) fetchLocation()
        else Toast.makeText(requireContext(), "Location permission needed", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        _binding = FragmentAddPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonPhoto.setOnClickListener {
            val camOk = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            if (camOk) preparePhotoFileAndLaunch()
            else requestCamera.launch(Manifest.permission.CAMERA)
        }

        binding.buttonLocation.setOnClickListener {
            val fine = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            if (fine || coarse) fetchLocation()
            else {
                requestLocation.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }
        }

        binding.buttonSave.setOnClickListener {
            val species = binding.inputSpecies.text?.toString()?.trim().orEmpty()
            val village = binding.inputVillage.text?.toString()?.trim().orEmpty()
            val coords = latLng
            if (species.isEmpty()) {
                Toast.makeText(requireContext(), "Add a species name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (coords == null) {
                Toast.makeText(requireContext(), "Tag GPS before saving", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val finalVillage = village.ifBlank {
                locationAddress.substringBefore(',').takeIf { it.isNotBlank() } ?: "Community"
            }
            val path = photoFile?.absolutePath

            lifecycleScope.launch {
                val now = System.currentTimeMillis()
                val entity = TreeEntity(
                    speciesName = species,
                    latitude = coords.latitude,
                    longitude = coords.longitude,
                    plantedAtMillis = now,
                    photoPath = path,
                    villageTag = finalVillage,
                    addressLine = locationAddress,
                    status = TreeStatus.PLANTED.storageKey,
                    growthPhotoPath = null,
                    lastUpdatedMillis = now,
                )
                val id = repo.insert(entity)
                prefs.edit().putString("last_village", finalVillage).apply()
                CheckupReminderScheduler.scheduleForNewTree(requireContext().applicationContext, id)
                Toast.makeText(requireContext(), "Saved, reminder set for 90 days", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun fetchLocation() {
        lifecycleScope.launch {
            latLng = locationHelper.lastKnownLatLng()
            val coords = latLng
            if (coords == null) {
                Toast.makeText(requireContext(), "Could not read GPS yet, try outdoors", Toast.LENGTH_LONG).show()
                updateCoordsLabel()
                return@launch
            }
            locationAddress = resolveAddress(coords.latitude, coords.longitude)
            if (binding.inputVillage.text.isNullOrBlank()) {
                val guess = locationAddress.substringBefore(',').trim()
                if (guess.isNotBlank()) binding.inputVillage.setText(guess)
            }
            updateCoordsLabel()
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveAddress(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val list = geocoder.getFromLocation(lat, lng, 1)
            list?.firstOrNull()?.getAddressLine(0).orEmpty()
        }.getOrDefault("")
    }

    private fun updateCoordsLabel() {
        val c = latLng
        binding.textCoords.text = if (c == null) {
            "Location not set"
        } else {
            buildString {
                append("Lat %.5f, Lng %.5f".format(c.latitude, c.longitude))
                if (locationAddress.isNotBlank()) {
                    append("\n")
                    append(locationAddress)
                }
            }
        }
    }

    private fun preparePhotoFileAndLaunch() {
        val dir = File(requireContext().cacheDir, "photos").apply { mkdirs() }
        val file = File(dir, "plant_${System.currentTimeMillis()}.jpg")
        photoFile = file
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.nammahasiru.app.fileprovider",
            file,
        )
        photoUri = uri
        takePicture.launch(uri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
