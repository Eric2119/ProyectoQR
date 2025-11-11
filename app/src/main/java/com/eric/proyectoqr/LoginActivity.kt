package com.eric.proyectoqr

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.eric.proyectoqr.databinding.ActivityLoginBinding
import com.eric.proyectoqr.network.LoginRequest
import com.eric.proyectoqr.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.isVisible = false
        binding.errorTextView.isVisible = false

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text?.toString()?.trim().orEmpty()
            val password = binding.passwordEditText.text?.toString()?.trim().orEmpty()

            if (email.isEmpty() || password.isEmpty()) {
                binding.errorTextView.text = getString(R.string.error_empty_fields)
                binding.errorTextView.isVisible = true
                return@setOnClickListener
            }

            binding.progressBar.isVisible = true
            binding.errorTextView.isVisible = false

            lifecycleScope.launch {
                try {
                    // Construir la petición con email, contraseña y el nombre del dispositivo
                    val request = LoginRequest(email, password, "android")
                    val response = RetrofitClient.apiService.login(request)

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && !body.token.isNullOrEmpty()) {
                            // Aquí podrías guardar body.token para futuras llamadas
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            binding.errorTextView.text = getString(R.string.error_login_failed)
                            binding.errorTextView.isVisible = true
                        }
                    } else {
                        // Intentamos leer el mensaje de error del servidor
                        val errorMsg = try {
                            response.errorBody()?.string() ?: getString(R.string.error_login_failed)
                        } catch (_: Exception) {
                            getString(R.string.error_login_failed)
                        }
                        binding.errorTextView.text = errorMsg
                        binding.errorTextView.isVisible = true
                    }
                } catch (e: Exception) {
                    binding.errorTextView.text = getString(R.string.error_network)
                    binding.errorTextView.isVisible = true
                } finally {
                    binding.progressBar.isVisible = false
                }
            }
        }

        // Listeners para los enlaces (p. ej. recuperación de contraseña)
        binding.forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Funcionalidad no implementada todavía", Toast.LENGTH_SHORT).show()
        }
        binding.createAccountText.setOnClickListener {
            Toast.makeText(this, "Funcionalidad no implementada todavía", Toast.LENGTH_SHORT).show()
        }
    }
}
