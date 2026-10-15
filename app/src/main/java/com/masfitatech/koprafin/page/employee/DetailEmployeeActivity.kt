package com.masfitatech.koprafin.page.employee

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.ActivityDetailEmployeeBinding
import com.masfitatech.koprafin.page.employee.viewmodel.DetailEmployeeViewModel
import java.util.Calendar

class DetailEmployeeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailEmployeeBinding
    private val viewModel: DetailEmployeeViewModel by viewModels()

    private var employeeId = 0
    private lateinit var token: String

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = AuthToken.tokenLogin(this).orEmpty()
        employeeId = intent.getIntExtra("id", 0)

        setupRoleDropdown()
        setupDatePicker()
        setupImagePicker()
        setupPasswordToggle()
        observeViewModel()

        if (employeeId == 0) setModeCreate()
        else {
            setModeEdit()
            viewModel.getEmployeeDetail(token, employeeId)
        }

        binding.btnSimpan.setOnClickListener {
            if (!validate()) return@setOnClickListener
            submitForm()
        }
    }

    // =========================
    // UI MODE
    // =========================

    private fun setModeCreate() {
        binding.tvPasswordLabel.visibility = View.VISIBLE
        binding.etPassword.visibility = View.VISIBLE
        binding.ivShowPass.visibility = View.VISIBLE
    }

    private fun setModeEdit() {
        binding.tvPasswordLabel.visibility = View.GONE
        binding.etPassword.visibility = View.GONE
        binding.ivShowPass.visibility = View.GONE
    }

    // =========================
    // DROPDOWN ROLE
    // =========================

    private fun setupRoleDropdown() {
        val roles = listOf("karyawan", "manager")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spRole.adapter = adapter
    }

    private fun setupSpinnerRole(role: String?) {
        val roles = listOf("karyawan", "manager")
        val index = roles.indexOf(role)
        if (index >= 0) binding.spRole.setSelection(index)
    }

    // =========================
    // DATE PICKER
    // =========================

    private fun setupDatePicker() {
        binding.etJoinDate.inputType = InputType.TYPE_NULL
        binding.etJoinDate.setOnClickListener {
            val c = Calendar.getInstance()
            val dialog = DatePickerDialog(
                this,
                { _, y, m, d ->
                    binding.etJoinDate.setText("%04d-%02d-%02d".format(y, m + 1, d))
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            )
            dialog.show()
        }
    }

    // =========================
    // IMAGE PICKER
    // =========================

    private fun setupImagePicker() {
        binding.profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            selectedImageUri = data?.data ?: return

            // tampilkan dulu
            Glide.with(this)
                .load(selectedImageUri)
                .placeholder(R.drawable.user)
                .into(binding.profileImage)

            // AUTO UPDATE AVATAR
            if (employeeId != 0) {
                viewModel.updateAvatarEmployee(token, employeeId, selectedImageUri)
            }
        }
    }

    // =========================
    // PASSWORD SHOW / HIDE
    // =========================

    private fun setupPasswordToggle() {
        binding.ivShowPass.setOnClickListener {
            val isVisible = binding.etPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.etPassword.inputType =
                if (isVisible) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                else InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }

    // =========================
    // VALIDATION
    // =========================

    private fun validate(): Boolean {
        if (binding.etNama.text.isNullOrBlank()) return showError("Nama tidak boleh kosong")
        if (binding.etEmail.text.isNullOrBlank()) return showError("Email tidak boleh kosong")
        if (binding.etNIK.text.isNullOrBlank()) return showError("NIK wajib diisi")
        if (employeeId == 0 && binding.etPassword.text.isNullOrBlank())
            return showError("Password wajib diisi saat menambah karyawan")

        return true
    }

    private fun showError(msg: String): Boolean {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        return false
    }

    // =========================
    // SUBMIT FORM
    // =========================

    private fun submitForm() {
        val name = binding.etNama.text.toString()
        val email = binding.etEmail.text.toString()
        val nik = binding.etNIK.text.toString()
        val pass = binding.etPassword.text.toString()
        val hp = binding.etHp.text.toString()
        val role = binding.spRole.selectedItem.toString()
        val company = binding.etCompany.text.toString()
        val divisi = binding.etDivisi.text.toString()
        val join = binding.etJoinDate.text.toString()

        if (employeeId == 0) {
            viewModel.createEmployee(
                token, name, nik, email, pass, hp,
                role, company, divisi, join, selectedImageUri
            )
        } else {
            viewModel.updateEmployee(
                token, employeeId, name, nik, email,
                hp, role, company, divisi, join
            )
        }
    }

    // =========================
    // OBSERVE VIEW MODEL
    // =========================

    private fun observeViewModel() {

        viewModel.detail.observe(this) { emp ->
            emp ?: return@observe

            binding.etNama.setText(emp.name)
            binding.etEmail.setText(emp.user?.email ?: "")
            binding.etNIK.setText(emp.nik ?: "")
            binding.etHp.setText(emp.phone ?: "")
            binding.etCompany.setText(emp.company ?: "")
            binding.etDivisi.setText(emp.division ?: "")
            binding.etJoinDate.setText(emp.workEntryDate ?: "")

            val url = emp.avatar?.let { ApiClient.BASE_URL_IMAGE + it }
            Glide.with(this)
                .load(url ?: R.drawable.user)
                .placeholder(R.drawable.user)
                .into(binding.profileImage)

            setupSpinnerRole(emp.role)
        }

        viewModel.success.observe(this) {
            if (it == true) {
                Toast.makeText(this, "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }

        viewModel.error.observe(this) {
            if (!it.isNullOrEmpty()) Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }


    }
}
