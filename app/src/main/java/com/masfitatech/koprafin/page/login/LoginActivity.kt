package com.masfitatech.koprafin.page.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.masfitatech.koprafin.MainActivity
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.SessionManager
import com.masfitatech.koprafin.databinding.ActivityLoginBinding
import com.masfitatech.koprafin.page.login.viewmodel.LoginViewModel
import com.masfitatech.koprafin.utils.DialogUtils
import com.masfitatech.koprafin.utils.LoadingDialog
import com.masfitatech.koprafin.utils.Resource

class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUIActions()
        authCheck()
        observeLoginResult()
        observeResetPassword()
    }

    private fun  authCheck(){
        sessionManager.getToken()?.let {
            if (it.isNotEmpty()){
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finishAffinity()
            }else{
                return
            }
        }
    }
    private fun initUIActions(){
        loadingDialog = LoadingDialog(this)
        sessionManager = SessionManager(this)

        var isPasswordVisible = false

        binding.ivShowPass.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivShowPass.setImageResource(R.drawable.eye_crossed)
            } else {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.ivShowPass.setImageResource(R.drawable.eye)
            }
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
        }


        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            DialogUtils.showInputDialog(
                this,
                title = "Reset Password",
                hint = "Masukkan email",
                positiveText = "Kirim"
            ) { email ->
                viewModel.resetPassword(email)
            }

        }

    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Loading -> {
                    loadingDialog.show("Login...")
                }
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    sessionManager.saveToken(result.data.data!!.token)
                    sessionManager.saveRole(result.data.data!!.role)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finishAffinity()
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    Log.e("LoginActivity", "Login error: ${result.message}")
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun observeResetPassword() {
        viewModel.resetResult.observe(this) { result ->
            when (result) {

                is Resource.Loading -> {
                    loadingDialog.show("Mengirim reset password...")
                }

                is Resource.Success -> {
                    loadingDialog.dismiss()

                    val intent = Intent(this, OtpActivity::class.java)
                    intent.putExtra("email", viewModel.resetEmail)
                    startActivity(intent)

                }

                is Resource.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, result.message ?: "Gagal reset password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



}