package com.eric.proyectoqr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.eric.proyectoqr.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón principal para escanear
        binding.scanButton.setOnClickListener {
            startActivity(Intent(this, ScanQrActivity::class.java))
        }

        // Tips desplegables
        var tipsExpanded = false
        binding.tipsHeader.setOnClickListener {
            tipsExpanded = !tipsExpanded
            binding.tipsContent.isVisible = tipsExpanded
            binding.tipsArrow.animate()
                .rotation(if (tipsExpanded) 180f else 0f)
                .setDuration(150)
                .start()
        }
    }
}
