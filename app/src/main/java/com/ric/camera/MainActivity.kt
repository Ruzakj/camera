package com.ric.camera

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var galleryController: GalleryController
    private lateinit var cameraController: CameraController
    private lateinit var focusController: FocusController
    private lateinit var captureController: CaptureController
    private lateinit var stillCaptureCoordinator: StillCaptureExecutionCoordinator
    private var lensSwitchController: LensSwitchController? = null
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
        galleryController = GalleryController(contentResolver)
        cameraController = CameraController()
        focusController = FocusController(cameraController)
        captureController = CaptureController()
        stillCaptureCoordinator = StillCaptureExecutionCoordinator(captureController)
        refreshGalleryThumbnail()
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
        binding.exposureSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val c = camera ?: return
                val range = c.cameraInfo.exposureState.exposureCompensationRange
                if (range.lower == 0 && range.upper == 0) return
                val index = (progress + range.lower).coerceIn(range.lower, range.upper)
                c.cameraControl.setExposureCompensationIndex(index)
                val step = c.cameraInfo.exposureState.exposureCompensationStep.toFloat()
                binding.exposureValueText.text = "EV " + String.format(Locale.US, "%+.1f", index * step)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.shutterButton.setOnClickListener { takePhoto() }
        binding.focusModeButton.setOnClickListener { focusMode = (focusMode + 1) % 3; focusLocked = false; updateFocusUi() }
        binding.focusLockButton.setOnClickListener { focusLocked = !focusLocked; if (!focusLocked) focusController.cancel(); binding.focusLockButton.text = if (focusLocked) "AF LOCKED" else "AF LOCK" }
        binding.focusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) { if (fromUser && focusMode == 2) setManualFocus(p / 1000f) }; override fun onStartTrackingTouch(s: SeekBar?) {}; override fun onStopTrackingTouch(s: SeekBar?) {} })
        binding.switchButton.setOnClickListener {
            val controller = lensSwitchController ?: return@setOnClickListener
            val nextLens = controller.nextLens(lensFacing)
            if (nextLens != lensFacing) {
                lensFacing = nextLens
                startCamera()
            }
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
                focusController.focus(p)
            }
            true
        }
    }

    private fun currentRotation(): Int = binding.previewView.display?.rotation ?: Surface.ROTATION_0

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()
            val switchController = LensSwitchController(LensCapabilities.from(provider))
            lensSwitchController = switchController
            binding.switchButton.isEnabled = switchController.canSwitch(lensFacing)
            binding.switchButton.alpha = if (binding.switchButton.isEnabled) 1f else .35f
            val rotation = currentRotation()
            val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).setTargetRotation(rotation).build().also { it.surfaceProvider = binding.previewView.surfaceProvider }
            imageCapture = ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).setTargetRotation(rotation).setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).setFlashMode(flashMode).build()
            cameraController.beginBinding(lensFacing)
            captureController.detach()
            try {
                provider.unbindAll()
                val boundCamera = provider.bindToLifecycle(this, CameraSelector.Builder().requireLensFacing(lensFacing).build(), preview, imageCapture)
                camera = boundCamera
                cameraController.attach(boundCamera, lensFacing)
                captureController.attach(requireNotNull(imageCapture))
                binding.statusText.text = "READY"
                setupExposureControl()
                updateFocusUi()
            } catch (e: Exception) {
                cameraController.failBinding(e)
                captureController.detach()
                camera = null
                binding.statusText.text = "CAMERA ERROR"
                Toast.makeText(this, e.message ?: "Camera gagal dibuka", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onResume() {
        super.onResume()
        binding.previewView.post { imageCapture?.targetRotation = currentRotation() }
        if (::galleryController.isInitialized) refreshGalleryThumbnail()
    }

    private fun refreshGalleryThumbnail(preferredUri: Uri? = null) {
        val uri = preferredUri?.takeIf { galleryController.isReadable(it) }
            ?: galleryController.latestImageUri()?.takeIf { galleryController.isReadable(it) }
        binding.galleryThumbnail.setImageURI(null)
        if (uri != null) binding.galleryThumbnail.setImageURI(uri)
        binding.galleryThumbnail.alpha = if (uri != null) 1f else .35f
    }

    private fun setupExposureControl() {
        val state = camera?.cameraInfo?.exposureState ?: return
        val range = state.exposureCompensationRange
        val supported = !(range.lower == 0 && range.upper == 0)
        binding.exposureSeekBar.isEnabled = supported
        binding.exposureSeekBar.alpha = if (supported) 1f else .35f
        if (!supported) { binding.exposureValueText.text = "EV • UNSUPPORTED"; return }
        binding.exposureSeekBar.max = range.upper - range.lower
        binding.exposureSeekBar.progress = state.exposureCompensationIndex - range.lower
        val ev = state.exposureCompensationIndex * state.exposureCompensationStep.toFloat()
        binding.exposureValueText.text = "EV " + String.format(Locale.US, "%+.1f", ev)
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
                .setCaptureRequestOption(android.hardware.camera2.CaptureRequest.LENS_FOCUS_DISTANCE, value * 10f).build()
            control.captureRequestOptions = options
            binding.focusValueText.text = if (value <= .001f) "MANUAL FOCUS • INFINITY" else "MANUAL FOCUS • " + (value * 100).toInt() + "%"
        } catch (_: Exception) { binding.focusValueText.text = "MANUAL FOCUS • UNSUPPORTED" }
    }

    private fun takePhoto() {
        val boundCamera = camera ?: return
        imageCapture?.targetRotation = currentRotation()
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "RIC_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()))
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Ric Camera")
        }
        val output = ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values).build()
        stillCaptureCoordinator.execute(boundCamera.cameraInfo) { capture ->
            capture.takePicture(output, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                    val savedUri = result.savedUri
                    if (galleryController.isReadable(savedUri)) {
                        binding.statusText.text = "SAVED"
                        refreshGalleryThumbnail(savedUri)
                        Toast.makeText(this@MainActivity, "Foto tersimpan", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.statusText.text = "SAVE VERIFY ERROR"
                        refreshGalleryThumbnail()
                        Toast.makeText(this@MainActivity, "Foto tersimpan tetapi belum dapat dibaca", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    binding.statusText.text = "SAVE ERROR"
                    Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}