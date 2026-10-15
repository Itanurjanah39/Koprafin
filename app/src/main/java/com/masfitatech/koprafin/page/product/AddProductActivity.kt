package com.masfitatech.koprafin.page.product

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.data.model.DataDetail
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.ActivityAddProductBinding
import com.masfitatech.koprafin.page.main.viewmodel.CategoryViewModel
import com.masfitatech.koprafin.page.product.adapter.ImagePickerAdapter
import com.masfitatech.koprafin.page.product.viewmodel.AddProductViewModel
import com.masfitatech.koprafin.utils.DialogUtils
import java.io.File

class AddProductActivity : AppCompatActivity() {

    private var isEditMode = false
    private var editingProductId: Int? = null
    private lateinit var binding: ActivityAddProductBinding
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val addProductViewModel: AddProductViewModel by viewModels()

    private lateinit var token: String
    private var selectedCategoryId: Int = 0

    private val imageUris = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImagePickerAdapter

    private var isCategoryLoaded = false
    private var isDetailLoaded = false
    private var pendingDetailProduct: DataDetail? = null


    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                if (imageUris.size >= 6) break
                imageUris.add(uri)
            }
            imageAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = AuthToken.tokenLogin(this).toString()

        setupImagePicker()
        setupListeners()
        observeCategoryViewModel()
        observeAddProductViewModel()

        categoryViewModel.fetchCategories(token)

        editingProductId = intent.getIntExtra("product_id", 0)
        if (editingProductId != 0) {
            isEditMode = true
            binding.tvTitle.text = "Edit Produk"
            binding.btnSaveProduct.text = "Perbarui Produk"
            fetchProductDetailForEdit(editingProductId!!)
        }
    }

    private fun setupImagePicker() {
        imageAdapter = ImagePickerAdapter(
            images = imageUris,
            onAddClick = { pickImages() },
            onDelete = { uri ->
                imageUris.remove(uri)
                imageAdapter.notifyDataSetChanged()
            }
        )

        binding.rvImages.layoutManager = GridLayoutManager(this, 3)
        binding.rvImages.adapter = imageAdapter
    }

    private fun pickImages() {
        pickImagesLauncher.launch("image/*")
    }

    private fun setupListeners() {
        binding.btnSaveProduct.setOnClickListener {
            saveProduct()
        }

        binding.ivMenuDelete.setOnClickListener {
            if (!isEditMode) {
                Toast.makeText(this, "Tidak ada produk untuk dihapus", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showDeleteMenu()
        }
    }

    private fun showDeleteMenu() {
        val popup = PopupMenu(this, binding.ivMenuDelete)
        popup.menuInflater.inflate(R.menu.menu_hapus, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    confirmDelete()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun confirmDelete() {
        DialogUtils.showConfirmDialog(
            context = this,
            title = "Hapus Produk",
            message = "Apakah Anda yakin ingin menghapus produk ini?",
            positiveText = "Hapus",
            cancelText = "Batal"
        ) {
            deleteProduct()
        }
    }

    private fun deleteProduct() {
        addProductViewModel.deleteProduct(token, editingProductId!!)
    }

    private fun observeCategoryViewModel() {

        categoryViewModel.categories.observe(this) { list ->
            if (list.isNullOrEmpty()) return@observe

            val names = list.map { it.name }
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
            binding.spCategory.adapter = adapter

            isCategoryLoaded = true

            // LISTENER — FIX UTAMA
            binding.spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedCategoryId = list[position].id ?: 0
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            // Jika edit dan detail sudah loaded → isi form
            if (isDetailLoaded && pendingDetailProduct != null) {
                applyDetailToForm(pendingDetailProduct!!)
            }
        }

        categoryViewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeAddProductViewModel() {

        addProductViewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        addProductViewModel.success.observe(this) {
            if (it == true) {
                val msg = when {
                    isEditMode && addProductViewModel.isDeleteProcess -> "Produk berhasil dihapus"
                    isEditMode -> "Produk berhasil diperbarui"
                    else -> "Produk berhasil ditambahkan"
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

                setResult(RESULT_OK)
                finish()
            }
        }

        addProductViewModel.error.observe(this) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchProductDetailForEdit(id: Int) {
        addProductViewModel.getProductDetail(token, id)

        addProductViewModel.productDetail.observe(this) { product ->
            if (product == null) return@observe

            pendingDetailProduct = product
            isDetailLoaded = true

            // Jika kategori sudah loaded → langsung isi form
            if (isCategoryLoaded) {
                applyDetailToForm(product)
            }
        }
    }

    private fun applyDetailToForm(product: DataDetail) {

        binding.etName.setText(product.name)
        binding.etPrice.setText(product.price?.toInt()?.toString() ?: "")
        binding.etDescription.setText(product.description)

        val categories = categoryViewModel.categories.value

        val index = categories?.indexOfFirst { it.id == product.category?.id } ?: -1

        if (index != -1) {
            binding.spCategory.setSelection(index)
            selectedCategoryId = categories!![index].id!!
        }

        // Load gambar lama
        imageUris.clear()
        product.picture?.forEach { picUrl ->
            imageUris.add(Uri.parse(ApiClient.BASE_URL_IMAGE + picUrl))
        }

        imageAdapter.notifyDataSetChanged()
    }


    private fun saveProduct() {
        val name = binding.etName.text.toString()
        val price = binding.etPrice.text.toString()
        val desc = binding.etDescription.text.toString()

        if (selectedCategoryId == 0) {
            Toast.makeText(this, "Pilih kategori", Toast.LENGTH_SHORT).show()
            return
        }
        if (name.isEmpty() || price.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode) updateProduct()
        else uploadProduct()
    }

    private fun uploadProduct() {
        addProductViewModel.uploadProduct(
            token = token,
            categoryId = selectedCategoryId.toString(),
            name = binding.etName.text.toString(),
            price = binding.etPrice.text.toString(),
            description = binding.etDescription.text.toString(),
            images = imageUris
        )
    }

    private fun updateProduct() {


        addProductViewModel.updateProduct(
            token = token,
            id = editingProductId!!,
            categoryId = selectedCategoryId.toString(),
            name = binding.etName.text.toString(),
            price = binding.etPrice.text.toString(),
            description = binding.etDescription.text.toString(),
            images = imageUris
        )
    }


}
