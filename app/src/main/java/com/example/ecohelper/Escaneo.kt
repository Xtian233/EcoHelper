package com.example.ecohelper

import ClassifyImageTf
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.ecohelper.databinding.EscaneoBinding
import com.ingenieriiajhr.jhrCameraX.BitmapResponse
import com.ingenieriiajhr.jhrCameraX.CameraJhr

class Escaneo : AppCompatActivity() {

    private lateinit var binding: EscaneoBinding
    private lateinit var cameraJhr: CameraJhr
    private lateinit var classifyTf: ClassifyImageTf

    private var isObjectDetected = false
    private var detectedObject: String? = null
    private var confidenceLevel: Float = 0f
    private val handler = Handler(Looper.getMainLooper())
    private val detectionRunnable = Runnable { verifyDetection() }

    companion object {
        const val INPUT_SIZE = 224
    }

    private val classes = arrayOf(
        "Latas", "Botellas de vidrio", "Botellas de plástico",
        "Bolsa de papeles", "Cartón", "No reconocido"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EscaneoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        classifyTf = ClassifyImageTf(this)
        cameraJhr = CameraJhr(this)
    }

    override fun onResume() {
        super.onResume()
        if (cameraJhr.allpermissionsGranted() && !cameraJhr.ifStartCamera) {
            startCameraJhr()
        } else {
            cameraJhr.noPermissions()
        }
    }

    private fun startCameraJhr() {
        cameraJhr.addlistenerBitmap(object : BitmapResponse {
            override fun bitmapReturn(bitmap: Bitmap?) {
                bitmap?.let { classifyImage(it) }
            }
        })
        cameraJhr.initBitmap()
        cameraJhr.initImageProxy()
        cameraJhr.start(1, 0, binding.cameraPreview, true, false, true)
    }

    private fun classifyImage(bitmap: Bitmap) {
        val bitmapScaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

        classifyTf.setListenerInterpreter(object : ReturnInterpreter {
            override fun classify(confidence: FloatArray, maxConfidence: Int) {
                runOnUiThread {
                    val detectedObjectTemp = classes[maxConfidence]
                    val confidenceThreshold = 0.9f

                    if (confidence[maxConfidence] >= confidenceThreshold) {
                        if (detectedObject != detectedObjectTemp) {
                            handler.removeCallbacks(detectionRunnable)
                            detectedObject = detectedObjectTemp
                            confidenceLevel = confidence[maxConfidence]
                            handler.postDelayed(detectionRunnable, 5000)
                        } else {
                            confidenceLevel = confidence[maxConfidence]
                        }
                    } else {
                        handler.removeCallbacks(detectionRunnable)
                        detectedObject = null
                        confidenceLevel = 0f
                    }

                    binding.txtResult.text = detectedObject ?: "No detectado"
                }
            }
        })
        classifyTf.classify(bitmapScaled)
        runOnUiThread {
            binding.imgBitMap.setImageBitmap(bitmap)
        }
    }

    private fun verifyDetection() {
        if (detectedObject != null && confidenceLevel >= 0.9f) {
            if (!isObjectDetected) {
                isObjectDetected = true
                val intent = Intent(this@Escaneo, RegistroEx::class.java)
                intent.putExtra("DETECTED_OBJECT", detectedObject)
                startActivity(intent)
            }
        } else {
            if (!isObjectDetected) {
                isObjectDetected = true
                val intent = Intent(this@Escaneo, RegistroEx::class.java)
                intent.putExtra("DETECTED_OBJECT", "No reconocido")
                startActivity(intent)
            }
        }
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        return Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            Matrix().apply { postRotate(degrees) },
            true
        )
    }
}
