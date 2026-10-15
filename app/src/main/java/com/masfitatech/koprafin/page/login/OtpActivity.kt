package com.masfitatech.koprafin.page.login

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.databinding.ActivityOtpBinding
import com.masfitatech.koprafin.page.login.viewmodel.OtpViewModel

class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var inputs: List<EditText>
    private val viewModel: OtpViewModel by viewModels()

    private var timer: CountDownTimer? = null
    private var canResend = false

    private lateinit var email: String
    private var savedOtp: String = "" // OTP disimpan sebelum kirim password

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email") ?: "-"
        binding.tvEmail.text = email
        binding.tvEmailInfo.text = "Kode verifikasi dikirim ke"

        setupOtpInputs()
        setupActions()
        startResendTimer()
        observeViewModel()
    }

    // ---------------------------------------------------------------------
    // OTP INPUT BEHAVIOR
    // ---------------------------------------------------------------------
    private fun setupOtpInputs() {
        inputs = listOf(
            binding.otp1, binding.otp2, binding.otp3,
            binding.otp4, binding.otp5, binding.otp6
        )

        for (i in inputs.indices) {
            inputs[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val text = s.toString()
                    if (text.length == 1 && i < inputs.size - 1) inputs[i + 1].requestFocus()
                    if (text.isEmpty() && i > 0) inputs[i - 1].requestFocus()
                }
            })
        }
    }

    // ---------------------------------------------------------------------
    // BUTTON ACTIONS
    // ---------------------------------------------------------------------
    private fun setupActions() {
        binding.btnVerify.setOnClickListener {
            val otp = inputs.joinToString("") { it.text.toString() }

            if (otp.length != 6) {
                Toast.makeText(this, "Kode OTP harus 6 digit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            savedOtp = otp
            showPasswordDialog()
        }

        binding.tvResend.setOnClickListener {
            if (canResend) {
                Toast.makeText(this, "Kode OTP dikirim ulang", Toast.LENGTH_SHORT).show()
                startResendTimer()
                // TODO: Jika backend punya endpoint resend_otp, panggil di sini.
            }
        }
    }

    // ---------------------------------------------------------------------
    // POPUP PASSWORD INPUT
    // ---------------------------------------------------------------------
    private fun showPasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_input_passnew, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialog)
            .setView(view)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etNew = view.findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = view.findViewById<EditText>(R.id.etConfirmPassword)
        val ivShowNew = view.findViewById<View>(R.id.ivShowNewPass)
        val ivShowConfirm = view.findViewById<View>(R.id.ivShowConfirmPass)
        val tvCancel = view.findViewById<View>(R.id.tvCancel)
        val btnUpdate = view.findViewById<View>(R.id.btnUpdate)

        // Toggle password NEW
        ivShowNew.setOnClickListener {
            togglePasswordVisibility(etNew, ivShowNew)
        }

        // Toggle password CONFIRM
        ivShowConfirm.setOnClickListener {
            togglePasswordVisibility(etConfirm, ivShowConfirm)
        }

        tvCancel.setOnClickListener { dialog.dismiss() }

        btnUpdate.setOnClickListener {
            val pass = etNew.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()

            if (pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass != confirm) {
                Toast.makeText(this, "Konfirmasi password tidak sama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()

            // CALL API RESET PASSWORD
            viewModel.resetPassword(
                email = email,
                otp = savedOtp,
                newPass = pass,
                newPassConfirm = confirm
            )
        }

        dialog.show()
    }

    private fun togglePasswordVisibility(editText: EditText, icon: View) {
        val isVisible = editText.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        if (isVisible) {
            editText.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            (icon as? android.widget.ImageView)?.setImageResource(R.drawable.eye)
        } else {
            editText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            (icon as? android.widget.ImageView)?.setImageResource(R.drawable.eye_crossed)
        }
        editText.setSelection(editText.text.length)
    }

    // ---------------------------------------------------------------------
    // TIMER
    // ---------------------------------------------------------------------
    private fun startResendTimer() {
        canResend = false
        timer?.cancel()

        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millis: Long) {
                val sec = millis / 1000
                binding.tvResend.text = "Kirim ulang dalam $sec detik"
                binding.tvResend.setTextColor(getColor(R.color.greyPrimary))
            }

            override fun onFinish() {
                canResend = true
                binding.tvResend.text = "Kirim Ulang Kode"
                binding.tvResend.setTextColor(getColor(R.color.colorPrimary))
            }
        }.start()
    }

    // ---------------------------------------------------------------------
    // OBSERVER VIEWMODEL
    // ---------------------------------------------------------------------
    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.btnVerify.isEnabled = !loading
        }

        viewModel.error.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        viewModel.success.observe(this) { ok ->
            if (ok == true) {
                Toast.makeText(this, "Password berhasil diperbarui", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
