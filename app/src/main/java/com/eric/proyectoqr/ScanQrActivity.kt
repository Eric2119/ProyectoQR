package com.eric.proyectoqr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.eric.proyectoqr.databinding.ActivityScanQrBinding
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.util.concurrent.Executors

class ScanQrActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanQrBinding
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    @Volatile private var handled = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) startCamera() else finish() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(cameraExecutor) { proxy -> analyzeFrame(scanner, proxy) }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Log.e("ScanQrActivity", "No se pudo iniciar la cámara", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeFrame(
        scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (handled) return@addOnSuccessListener
                val raw = barcodes.firstOrNull()?.rawValue?.trim().orEmpty()
                val token = extractToken(raw)

                if (!raw.isNullOrEmpty() && token != null) {
                    handled = true
                    // Mandamos SOLO el token
                    startActivity(
                        Intent(this, TicketInfoActivity::class.java)
                            .putExtra("token", token)
                            .putExtra("qr_raw", raw)

                    // opcional, por si lo quieres mostrar/debug
                    )
                    Log.d("ScanQrActivity", "QR raw = $raw, token = $token")

                    finish()
                }
            }
            .addOnFailureListener { e -> Log.e("ScanQrActivity", "Error analizando imagen", e) }
            .addOnCompleteListener { imageProxy.close() }
    }

    /**
     * Extrae el token desde distintos formatos:
     *  - {"t":"..."}  ó {"token":"..."}
     *  - URL con ?t= o ?token=
     *  - Valor plano (UUID/JWT/slug)
     */
    private fun extractToken(raw: String): String? {
        if (raw.isBlank()) return null

        // JSON del tipo {"t":"..."} o {"token":"..."}
        if (raw.trim().startsWith("{") && raw.trim().endsWith("}")) {
            runCatching {
                val obj = JSONObject(raw)
                return obj.optString("t")
                    .ifEmpty { obj.optString("token") }
                    .ifEmpty { null }
            }.onFailure { /* ignore */ }
        }

        // URL con query param ?t= o ?token=
        runCatching {
            val uri = Uri.parse(raw)
            val qp = uri.getQueryParameter("t")
                ?: uri.getQueryParameter("token")
            if (!qp.isNullOrBlank()) return qp
        }

        // Si ya es el token en crudo (UUID/slug largo)
        val looksLikeToken = raw.matches(Regex("[A-Za-z0-9._\\-]{16,}"))
        if (looksLikeToken) return raw

        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
