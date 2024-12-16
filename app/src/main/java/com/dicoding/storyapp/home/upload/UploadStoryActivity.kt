package com.dicoding.storyapp.home.upload

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.Result
import com.dicoding.storyapp.databinding.ActivityUploadStoryBinding
import com.dicoding.storyapp.getImageUri
import com.dicoding.storyapp.home.HomeActivity
import com.dicoding.storyapp.reduceFileImage
import com.dicoding.storyapp.uriToFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class UploadStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadStoryBinding
    private var currentImageUri: Uri? = null

    private lateinit var viewModel: UploadStoryViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val requestPermissionLauncer = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
        }

    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (!allPermissionsGranted()) {
            requestPermissionLauncer.launch(REQUIRED_PERMISSION)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.edAddLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getLocation()
            } else {
                binding.latitudeEditText.text = null
                binding.longitudeEditText.text = null
            }
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.idButtonAdd.setOnClickListener { uploadImage() }

        val factory = ViewModelFactory.getInstance(this@UploadStoryActivity)
        viewModel = ViewModelProvider(this@UploadStoryActivity, factory)[UploadStoryViewModel::class.java]
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        binding.latitudeEditText.setText(location.latitude.toString())
                        binding.longitudeEditText.setText(location.longitude.toString())
                    } else {
                        Toast.makeText(this, "Unknown Location", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to Get Location: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

 
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, "Location permission is required to get the current location.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            lifecycleScope.launch {
                val imageFile = uriToFile(uri, this@UploadStoryActivity).reduceFileImage()
                val description = binding.edAddDescription.text.toString()

                val latitude = if (binding.edAddLocation.isChecked) {
                    binding.latitudeEditText.text.toString().toDoubleOrNull()
                } else {
                    null
                }

                val longitude = if (binding.edAddLocation.isChecked) {
                    binding.longitudeEditText.text.toString().toDoubleOrNull()
                } else {
                    null
                }

                viewModel.uploadImage(imageFile, description,
                    latitude, longitude).observe(this@UploadStoryActivity) { result ->
                    when (result) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showToast(result.data.message ?: "Upload Success")
                            startActivity(Intent(this@UploadStoryActivity, HomeActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                            showLoading(false)
                        }
                        is Result.Error -> {
                            showToast(result.error)
                            showLoading(false)
                        }

                        else -> {
                            showLoading(false)
                        }
                    }
                }
            }
        }
    }


    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Toast.makeText(this, "No Media Selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Toast.makeText(this, "Show Image", Toast.LENGTH_SHORT).show()
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}