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

        // Ocultar elementos al inicio
        binding.progressBar.isVisible = false
        binding.errorTextView.isVisible = false

        // Botón de iniciar sesión
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
                    // Petición de login
                    val request = LoginRequest(email, password, "android")
                    val api = RetrofitClient.getInstance(this@LoginActivity)
                    val response = api.login(request)

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && !body.token.isNullOrEmpty()) {

                            // ✅ Guardar token completo (con Bearer)
                            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                            sharedPref.edit().apply {
                                putString("auth_token", "${body.tokenType} ${body.token}")
                                apply()
                            }

                            // Ir al MainActivity
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()

                        } else {
                            binding.errorTextView.text = getString(R.string.error_login_failed)
                            binding.errorTextView.isVisible = true
                        }
                    } else {
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

        // Enlaces (sin implementar aún, solo muestran un Toast)
        binding.forgotPasswordTextView.setOnClickListener {
            Toast.makeText(
                this,
                "Funcionalidad no implementada todavía",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.createAccountTextView.setOnClickListener {
            Toast.makeText(
                this,
                "Funcionalidad no implementada todavía",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
