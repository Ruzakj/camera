package com.ric.camera

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.core.content.ContextCompat
import com.ric.camera.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var focusMode = 0
    private var focusLocked = false
    private lateinit var scaleDetector: ScaleGestureDetector

    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) startCamera() else Toast.makeText(this, "Camera permission diperlukan", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
        else permission.launch(Manifest.permission.CAMERA)

        scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val c = camera ?: return false
                val state = c.cameraInfo.zoomState.value ?: return false
                c.cameraControl.setZoomRatio((state.zoomRatio * detector.scaleFactor).coerceIn(state.minZoomRatio, state.maxZoomRatio))
                return true
            }
        })
        binding.shutterButton.setOnClickListener { takePhoto() }
        binding.focusModeButton.setOnClickListener { focusMode = (focusMode + 1) % 3; focusLocked = false; updateFocusUi() }
        binding.focusLockButton.setOnClickListener { focusLocked = !focusLocked; if (!focusLocked) camera?.cameraControl?.cancelFocusAndMetering(); binding.focusLockButton.text = if (focusLocked) "AF LOCKED" else "AF LOCK" }
        binding.focusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) { if (fromUser && focusMode == 2) setManualFocus(p / 1000f) }; override fun onStartTrackingTouch(s: SeekBar?) {}; override fun onStopTrackingTouch(s: SeekBar?) {} })
        binding.switchButton.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            startCamera()
        }
        binding.flashButton.setOnClickListener {
            flashMode = when (flashMode) {
                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_AUTO
                ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                else -> ImageCapture.FLASH_MODE_OFF
            }
            imageCapture?.flashMode = flashMode
            binding.flashButton.text = when (flashMode) {
                ImageCapture.FLASH_MODE_AUTO -> "FLASH AUTO"
                ImageCapture.FLASH_MODE_ON -> "FLASH ON"
                else -> "FLASH OFF"
            }
        }
        binding.previewView.setOnTouchListener { _, e ->
            scaleDetector.onTouchEvent(e)
            if (e.action == MotionEvent.ACTION_UP && focusMode != 2 && !focusLocked) {
                val p = binding.previewView.meteringPointFactory.createPoint(e.x, e.y)
                showFocusRing(e.x, e.y)
                camera?.cameraControl?.startFocusAndMetering(FocusMeteringAction.Builder(p).setAutoCancelDuration(3, TimeUnit.SECONDS).build())
            }
            true
        }
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = binding.previewView.surfaceProvider }
            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).setFlashMode(flashMode).build()
            try {
                provider.unbindAll()
                camera = provider.bindToLifecycle(this, CameraSelector.Builder().requireLensFacing(lensFacing).build(), preview, imageCapture)
                binding.statusText.text = "READY"
                updateFocusUi()
            } catch (e: Exception) {
                binding.statusText.text = "CAMERA ERROR"
                Toast.makeText(this, e.message ?: "Camera gagal dibuka", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun updateFocusUi() {
        binding.focusModeButton.text = when (focusMode) { 0 -> "AF-C"; 1 -> "AF-S"; else -> "MF" }
        binding.manualFocusPanel.visibility = if (focusMode == 2) View.VISIBLE else View.GONE
        binding.statusText.text = when (focusMode) { 0 -> "CONTINUOUS AF"; 1 -> "SINGLE AF"; else -> "MANUAL" }
    }

    private fun showFocusRing(x: Float, y: Float) {
        binding.focusRing.visibility = View.VISIBLE
        binding.focusRing.translationX = x - binding.focusRing.width / 2f
        binding.focusRing.translationY = y - binding.focusRing.height / 2f
        binding.focusRing.scaleX = 1.25f; binding.focusRing.scaleY = 1.25f
        binding.focusRing.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(180).withEndAction {
            binding.focusRing.postDelayed({ binding.focusRing.animate().alpha(0f).setDuration(250).withEndAction { binding.focusRing.visibility = View.GONE }.start() }, 900)
        }.start()
    }

    private fun setManualFocus(value: Float) {
        val c = camera ?: return
        try {
            val control = Camera2CameraControl.from(c.cameraControl)
            val options = CaptureRequestOptions.Builder()
                .setCaptureRequestOption(android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE, android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE_OFF)
                .setCaptureRequestOption(android.hardware.camera2.CaptureRequest.LENS_FOCUS_DISTANCE, value * 10f)
                .build()
            control.captureRequestOptions = options
            binding.focusValueText.text = if (value <= .001f) "MANUAL FOCUS • INFINITY" else "MANUAL FOCUS • " + (value * 100).toInt() + "%"
        } catch (_: Exception) { binding.focusValueText.text = "MANUAL FOCUS • UNSUPPORTED" }
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "RIC_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()))
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Ric Camera")
        }
        val output = ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values).build()
        capture.takePicture(output, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                binding.statusText.text = "SAVED"
                Toast.makeText(this@MainActivity, "Foto tersimpan", Toast.LENGTH_SHORT).show()
            }
            override fun onError(exception: ImageCaptureException) {
                binding.statusText.text = "SAVE ERROR"
                Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_LONG).show()
            }
        })
    }
}