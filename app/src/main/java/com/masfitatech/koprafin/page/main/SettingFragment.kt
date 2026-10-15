package com.masfitatech.koprafin.page.main

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.databinding.FragmentSettingBinding
import com.masfitatech.koprafin.databinding.ItemImageBannerAddBinding
import com.masfitatech.koprafin.page.main.adapter.BannerAdapter
import com.masfitatech.koprafin.page.main.adapter.CategoryAdapter
import com.masfitatech.koprafin.page.main.viewmodel.BannerViewModel
import com.masfitatech.koprafin.page.main.viewmodel.SettingViewModel
import com.masfitatech.koprafin.utils.DialogUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.activity.OnBackPressedCallback
import com.masfitatech.koprafin.MainActivity


class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: SettingViewModel by activityViewModels()
    private val bannerViewModel: BannerViewModel by activityViewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var bannerAdapter: BannerAdapter

    private lateinit var token: String

    // dialog state
    private var bannerImagePart: MultipartBody.Part? = null
    private var onBannerImagePicked: ((Uri) -> Unit)? = null

    // image picker
    private val pickBannerImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                bannerImagePart = uriToMultipart(requireContext(), it, "image")
                onBannerImagePicked?.invoke(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        token = AuthToken.tokenLogin(requireContext()).orEmpty()
        (activity as MainActivity).setToolbarTitle("Pengaturan")
        handleBackPress()
        setupCategoryRecycler()
        setupBannerRecycler()
        setupSwipeRefresh()
        setupListeners()
        observeViewModels()

        categoryViewModel.getCategories(token)
        bannerViewModel.getBanners(token)
    }

    private fun setupListeners() {
        binding.btnAddCategory.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditCategoryActivity::class.java))
        }

        binding.btnAddBanner.setOnClickListener {
            showAddBannerDialog(requireContext()) { status, imagePart ->
                bannerViewModel.createBanner(token, status, imagePart)
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            categoryViewModel.getCategories(token)
            bannerViewModel.getBanners(token)
        }
    }

    private fun setupCategoryRecycler() {
        categoryAdapter = CategoryAdapter(
            list = emptyList(),
            onEdit = {
                startActivity(
                    Intent(requireContext(), AddEditCategoryActivity::class.java)
                        .putExtra("id", it.id)
                )
            },
            onDelete = {
                DialogUtils.showConfirmDialog(
                    requireContext(),
                    "Hapus Kategori",
                    "Yakin ingin menghapus ${it.name}?",
                    onConfirm = { categoryViewModel.deleteCategory(token, it.id!!) }
                )
            }
        )

        binding.rvCategory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategory.adapter = categoryAdapter
    }

    private fun setupBannerRecycler() {
        bannerAdapter = BannerAdapter(
            list = emptyList(),
            onToggle = { banner, isChecked ->
                bannerViewModel.updateBanner(
                    token,
                    banner.id!!,
                    if (isChecked) 1 else 0
                )
            },
            onDelete = { banner ->
                DialogUtils.showConfirmDialog(
                    requireContext(),
                    "Hapus Banner",
                    "Yakin ingin menghapus banner?",
                    onConfirm = { bannerViewModel.deleteBanner(token, banner.id!!) }
                )
            }
        )

        binding.rvBanner.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBanner.adapter = bannerAdapter
    }

    private fun observeViewModels() {

        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categoryAdapter.updateData(it)
            binding.swipeRefresh.isRefreshing = false
        }

        categoryViewModel.actionSuccess.observe(viewLifecycleOwner) {
            if (it) {
                categoryViewModel.getCategories(token)
                categoryViewModel.resetEvent()
            }
        }



        bannerViewModel.banners.observe(viewLifecycleOwner) {
            bannerAdapter.updateData(it)
            binding.swipeRefresh.isRefreshing = false
        }

        bannerViewModel.success.observe(viewLifecycleOwner) {
            if (it) {
                bannerViewModel.getBanners(token)
                bannerViewModel.resetEvent()
            }
        }

        bannerViewModel.deleteSuccess.observe(viewLifecycleOwner) {
            if (it) {
                bannerViewModel.getBanners(token)
                bannerViewModel.resetEvent()
            }
        }

        bannerViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }

        categoryViewModel.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        bannerViewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                bannerViewModel.resetEvent()
            }
        }

    }

    private fun showAddBannerDialog(
        context: Context,
        onSubmit: (Int, MultipartBody.Part) -> Unit
    ) {
        val dialog = Dialog(context)
        val binding = ItemImageBannerAddBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        // RESET STATE
        bannerImagePart = null
        onBannerImagePicked = null

        // ====== MODE DIALOG ======
        binding.cvAddImage.visibility = View.VISIBLE
        binding.btnUpload.visibility = View.VISIBLE

        onBannerImagePicked = { uri ->
            binding.imgBanner.setImageURI(uri)
            binding.cvAddImage.visibility = View.GONE
        }

        binding.cvAddImage.setOnClickListener {
            pickBannerImageLauncher.launch("image/*")
        }

        binding.imgBanner.setOnClickListener {
            pickBannerImageLauncher.launch("image/*")
        }

        binding.btnUpload.setOnClickListener {
            val imagePart = bannerImagePart
            if (imagePart == null) {
                Toast.makeText(context, "Pilih gambar dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val status = if (binding.switchStatus.isChecked) 1 else 0
            onSubmit(status, imagePart)
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            onBannerImagePicked = null
        }

        dialog.show()
    }


    private fun uriToMultipart(
        context: Context,
        uri: Uri,
        fieldName: String
    ): MultipartBody.Part {

        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "image/*"
        val bytes = resolver.openInputStream(uri)?.readBytes()
            ?: throw IllegalArgumentException("Cannot read file")

        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val fileName = "banner_${System.currentTimeMillis()}.jpg"

        return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
    }

    private fun handleBackPress() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, callback)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
