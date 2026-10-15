package com.masfitatech.koprafin.page.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.data.model.DataCategory
import com.masfitatech.koprafin.databinding.ActivityAddEditCategoryBinding
import com.masfitatech.koprafin.page.main.viewmodel.SettingViewModel

class AddEditCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditCategoryBinding
    private val viewModel: SettingViewModel by viewModels()

    private lateinit var token: String
    private var categoryId: Int? = null

    private var categoryList: List<DataCategory> = emptyList()
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    // untuk handle race condition
    private var pendingDetailName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        setupMode()
        setupListener()
        observeViewModel()

        if (isEditMode()) {
            viewModel.getCategories(token)
            viewModel.getCategoryDetail(token, categoryId!!)
        }
    }

    private fun initData() {
        token = AuthToken.tokenLogin(this).orEmpty()
        categoryId = intent.getIntExtra("id", -1).takeIf { it != -1 }
    }

    private fun isEditMode(): Boolean = categoryId != null

    private fun setupMode() {
        if (isEditMode()) {
            binding.tvTitle.text = "Edit Kategori"
        } else {
            binding.tvTitle.text = "Tambah Kategori"
        }
    }


    private fun setupListener() {
        binding.btnSaveCategory.setOnClickListener {
            submit()
        }
    }

    private fun submit() {

        val categoryName = binding.etCategory.text.toString().trim()
        val tenorMin = binding.etTenorMinimal.text.toString()
        val tenorMax = binding.etTenorMaksimal.text.toString()
        val margin = binding.etMargin.text.toString()

        if (categoryName.isEmpty() || tenorMin.isEmpty() || tenorMax.isEmpty() || margin.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        if (tenorMin.toInt() > tenorMax.toInt()) {
            Toast.makeText(this, "Tenor minimal tidak boleh lebih besar", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode()) {
            viewModel.updateCategory(
                token = token,
                id = categoryId!!,
                name = categoryName,
                minTenor = tenorMin.toInt(),
                maxTenor = tenorMax.toInt(),
                margin = margin.toInt()
            )
        } else {
            viewModel.createCategory(
                token = token,
                name = categoryName,
                minTenor = tenorMin.toInt(),
                maxTenor = tenorMax.toInt(),
                margin = margin.toInt()
            )
        }
    }

    // =====================================================
    // OBSERVER
    // =====================================================
    private fun observeViewModel() {

        viewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }



        // DETAIL CATEGORY
        viewModel.categoryDetail.observe(this) { detail ->

            pendingDetailName = detail.name
            binding.etCategory.setText(detail.name?.toString())
            binding.etTenorMinimal.setText(detail.minTenor?.toString())
            binding.etTenorMaksimal.setText(detail.maksTenor?.toString())
            binding.etMargin.setText(detail.margin?.toString())

        }

        viewModel.success.observe(this) {
            if (it) {
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


}
