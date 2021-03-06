package com.example.tsundoku

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import kotlinx.android.synthetic.main.activity_barcode_camera.*
import java.io.File
import java.util.concurrent.Executors


// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)


class BarcodeCamera : AppCompatActivity(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_camera)

        viewFinder = findViewById(R.id.view_finder)

        // Request camera permissions
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        Snackbar.make(barcode_camera_layout, "Scan barcode by centering it within the camera view.", Snackbar.LENGTH_INDEFINITE).show();
    }

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView

    private fun startCamera() {
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640, 480))
            setTargetRotation(Surface.ROTATION_180)
        }.build()


        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val imageCaptureConfig = ImageCaptureConfig.Builder()
                .apply {
                    // We don't set a resolution for image capture; instead, we
                    // select a capture mode which will infer the appropriate
                    // resolution based on aspect ratio and requested mode
                    setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
                }.build()

        // Build the image capture use case and attach button click listener
        val imageCapture = ImageCapture(imageCaptureConfig)

//        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
//
//            Log.d("DEBUG", "entered onClick image capture method")
//
//            val file = File(externalMediaDirs.first(),
//                    "${System.currentTimeMillis()}.jpg")
//
//            imageCapture.takePicture(file, executor,
//                    object : ImageCapture.OnImageSavedListener {
//                        override fun onError(
//                                imageCaptureError: ImageCapture.ImageCaptureError,
//                                message: String,
//                                exc: Throwable?
//                        ) {
//                            val msg = "Photo capture failed: $message"
//                            Log.d("DEBUG", msg)
//                            viewFinder.post {
//                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                            }
//                        }
//
//                        override fun onImageSaved(file: File) {
//                            val msg = "Photo capture succeeded: ${file.absolutePath}"
//                            Log.d("DEBUG", msg)
//                            viewFinder.post {
//                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    })
//        }

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(
                    ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        val analyzer = BarcodeImageAnalyzer(this)

        // Build the image analysis use case and instantiate our analyzer
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(executor, analyzer)
        }
        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.

        // We have three different and totally independent components:
        // The preview shows a stream of images from the phone's camera
        // The imageCapture lies in wait and captures an image when you hit the button
        // The analyzerUseCase analyzes the stream of images from the camera
        CameraX.bindToLifecycle(this, preview, imageCapture, analyzerUseCase)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    private class BarcodeImageAnalyzer(val barcodeCamera: BarcodeCamera) : ImageAnalysis.Analyzer {

        private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
        }

        override fun analyze(imageProxy: ImageProxy?, degrees: Int) {
            val mediaImage = imageProxy?.image
            val imageRotation = degreesToFirebaseRotation(degrees)
            if (mediaImage != null) {
                val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
                // Pass image to an ML Kit Vision API
                // ...
                // [START set_detector_options]
                val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_EAN_13)
                        .build()
                // [END set_detector_options]

                // [START get_detector]
//                val detector = FirebaseVision.getInstance()
//                        .visionBarcodeDetector
                // Or, to specify the formats to recognize:
                val detector = FirebaseVision.getInstance()
                        .getVisionBarcodeDetector(options)
                // [END get_detector]

                // [START run_detector]
                val result = detector.detectInImage(image).addOnSuccessListener { barcodes ->

                    val barcode_amount = barcodes.size
                    Log.d("DEBUG", "onSuccess barcode listener, $barcode_amount barcodes detected")

                    // Task completed successfully
                    // [START_EXCLUDE]
                    // [START get_barcodes]
                    if (barcodes.size == 1) {

                        val barcode = barcodes[0]

                        val rawValue = barcode.rawValue
                        Log.d("DEBUG", "raw value is $rawValue")

                        val valueType = barcode.valueType
                        Log.d("DEBUG", "barcode value is $valueType")
                        // See API reference for complete list of supported types

                        //attach isbn to intent
                        val barcodeIntent = Intent()
                        Log.d("DEBUG", "Intent initiated")
                        barcodeIntent.putExtra("scannedIsbn", rawValue.toString())
                        Log.d("DEBUG", "Intent putExtra ************************************************************")

                        // [END get_barcodes]
                        // [END_EXCLUDE]
                        barcodeCamera.setResult(RESULT_OK, barcodeIntent)
                        barcodeCamera.finish()
                    }
                }
                .addOnFailureListener {
                    Log.d("DEBUG", "barcode detection failed")
                }
            }
        }
    }

}
