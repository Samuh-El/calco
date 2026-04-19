package com.example.calco

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.calco.databinding.ActivityMainBinding
import com.example.calco.util.CameraManager
import com.example.calco.util.GestureManager
import com.example.calco.util.PermissionManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    
    private lateinit var permissionManager: PermissionManager
    private lateinit var cameraManager: CameraManager
    private lateinit var gestureManager: GestureManager

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.overlayImage.setImageURI(it)
            binding.overlayImage.visibility = View.VISIBLE
            binding.transparencySlider.visibility = View.VISIBLE
            gestureManager.resetMatrix()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        if (permissionManager.allPermissionsGranted()) {
            cameraManager.startCamera()
        } else {
            Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initManagers()
        setupUI()

        if (permissionManager.allPermissionsGranted()) {
            cameraManager.startCamera()
        } else {
            permissionManager.requestPermissions(requestPermissionLauncher)
        }
    }

    private fun initManagers() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        permissionManager = PermissionManager(this)
        cameraManager = CameraManager(this, this, binding.viewFinder)
        gestureManager = GestureManager(this, binding.overlayImage)
    }

    private fun setupUI() {
        binding.btnGallery.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnFlip.setOnClickListener {
            cameraManager.flipCamera()
        }

        binding.btnFlash.setOnClickListener {
            cameraManager.toggleFlash()
        }

        binding.transparencySlider.addOnChangeListener { _, value, _ ->
            binding.overlayImage.alpha = value
        }
        
        binding.btnInfo.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_title))
            .setMessage(getString(R.string.about_content))
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
