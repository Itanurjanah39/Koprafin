package com.masfitatech.koprafin.page.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.masfitatech.koprafin.MainActivity
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.databinding.BottomsheetAddBannerBinding
import com.masfitatech.koprafin.databinding.FragmentSettingBinding
import com.masfitatech.koprafin.page.main.adapter.BannerAdapter
import com.masfitatech.koprafin.page.main.adapter.CategoryAdapter
import com.masfitatech.koprafin.page.main.viewmodel.BannerViewModel
import com.masfitatech.koprafin.page.main.viewmodel.SettingViewModel
import com.masfitatech.koprafin.utils.DialogUtils
import com.masfitatech.koprafin.utils.ImageUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: SettingViewModel by activityViewModels()
    private val bannerViewModel: BannerViewModel by activityViewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var token: String

    private var showingBanner = false
    private var categoryLoading = false
    private var bannerLoading = false

    // ===== state bottom sheet tambah banner =====
    private var addSheet: BottomSheetDialog? = null
    private var sheetBinding: BottomsheetAddBannerBinding? = null
    private var preparedPart: MultipartBody.Part? = null
    private var prepareJob: Job? = null

    private val pickBannerImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { onImagePicked(it) }
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
        showingBanner = savedInstanceState?.getBoolean(KEY_TAB_BANNER) ?: false

        handleBackPress()
        setupTabs()
        setupCategoryRecycler()
        setupBannerRecycler()
        setupSwipeRefresh()
        setupListeners()
        observeCategory()
        observeBanner()

        categoryViewModel.getCategories(token)
        bannerViewModel.getBanners(token)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_TAB_BANNER, showingBanner)
    }

    // =====================================================
    // SETUP
    // =====================================================
    private fun setupTabs() {
        binding.tabCategory.setOnClickListener { selectTab(banner = false) }
        binding.tabBanner.setOnClickListener { selectTab(banner = true) }
        selectTab(showingBanner)
    }

    private fun selectTab(banner: Boolean) {
        showingBanner = banner
        binding.tabCategory.isSelected = !banner
        binding.tabBanner.isSelected = banner
        binding.sectionCategory.visibility = if (banner) View.GONE else View.VISIBLE
        binding.sectionBanner.visibility = if (banner) View.VISIBLE else View.GONE
    }

    private fun setupListeners() {
        binding.btnAddCategory.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditCategoryActivity::class.java))
        }
        binding.btnAddBanner.setOnClickListener { showAddBannerSheet() }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.brandPrimary, R.color.brandGreen)
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
            onDelete = { category ->
                DialogUtils.showConfirmDialog(
                    requireContext(),
                    "Hapus Kategori",
                    "Yakin ingin menghapus kategori ${category.name}?",
                    positiveText = "Hapus",
                    onConfirm = { category.id?.let { categoryViewModel.deleteCategory(token, it) } }
                )
            }
        )
        binding.rvCategory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategory.adapter = categoryAdapter
    }

    private fun setupBannerRecycler() {
        bannerAdapter = BannerAdapter(
            onToggle = { banner, isChecked ->
                banner.id?.let { bannerViewModel.updateStatus(token, it, if (isChecked) 1 else 0) }
            },
            onDelete = { banner ->
                DialogUtils.showConfirmDialog(
                    requireContext(),
                    "Hapus Banner",
                    "Banner ini akan dihapus permanen dari Beranda. Lanjutkan?",
                    positiveText = "Hapus",
                    onConfirm = { banner.id?.let { bannerViewModel.deleteBanner(token, it) } }
                )
            }
        )
        binding.rvBanner.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBanner.itemAnimator?.changeDuration = 0
        binding.rvBanner.adapter = bannerAdapter
    }

    // =====================================================
    // OBSERVER
    // =====================================================
    private fun observeCategory() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { list ->
            categoryAdapter.updateData(list)
            val count = list.size
            binding.tvCategoryCount.text = "$count kategori"
            binding.tabCategory.text = "Kategori ($count)"
            binding.llEmptyCategory.visibility = if (count == 0) View.VISIBLE else View.GONE
            binding.rvCategory.visibility = if (count == 0) View.GONE else View.VISIBLE
        }

        categoryViewModel.actionSuccess.observe(viewLifecycleOwner) {
            if (it) {
                categoryViewModel.getCategories(token)
                categoryViewModel.resetEvent()
            }
        }

        categoryViewModel.isLoading.observe(viewLifecycleOwner) {
            categoryLoading = it == true
            updateRefreshing()
        }

        categoryViewModel.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                toast(it)
                categoryViewModel.errorMessage.value = null
            }
        }
    }

    private fun observeBanner() {
        bannerViewModel.banners.observe(viewLifecycleOwner) { list ->
            bannerAdapter.submitList(list)
            val total = list.size
            val active = list.count { it.status == 1 }
            binding.tvBannerCount.text = "$total banner · $active aktif"
            binding.tabBanner.text = "Banner ($total)"
            binding.llEmptyBanner.visibility = if (total == 0) View.VISIBLE else View.GONE
            binding.rvBanner.visibility = if (total == 0) View.GONE else View.VISIBLE
        }

        bannerViewModel.pendingIds.observe(viewLifecycleOwner) {
            bannerAdapter.setPendingIds(it)
        }

        bannerViewModel.isLoading.observe(viewLifecycleOwner) {
            bannerLoading = it == true
            updateRefreshing()
        }

        bannerViewModel.isUploading.observe(viewLifecycleOwner) { uploading ->
            renderUploadState(uploading == true)
        }

        bannerViewModel.uploadSuccess.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { addSheet?.dismiss() }
        }

        bannerViewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { toast(it) }
        }
    }

    private fun updateRefreshing() {
        _binding?.swipeRefresh?.isRefreshing = categoryLoading || bannerLoading
    }

    // =====================================================
    // TAMBAH BANNER (bottom sheet)
    // =====================================================
    private fun showAddBannerSheet() {
        if (addSheet?.isShowing == true) return

        val sheet = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        val sb = BottomsheetAddBannerBinding.inflate(layoutInflater)
        sheet.setContentView(sb.root)

        preparedPart = null
        sb.framePicker.setOnClickListener { pickBannerImage.launch("image/*") }
        sb.tvChangeImage.setOnClickListener { pickBannerImage.launch("image/*") }

        sb.btnUpload.setOnClickListener {
            val part = preparedPart
            when {
                prepareJob?.isActive == true -> toast("Gambar masih diproses, tunggu sebentar")
                part == null -> toast("Pilih gambar banner terlebih dahulu")
                else -> bannerViewModel.createBanner(
                    token,
                    if (sb.switchStatus.isChecked) 1 else 0,
                    part
                )
            }
        }

        sheet.setOnDismissListener {
            prepareJob?.cancel()
            preparedPart = null
            sheetBinding = null
            addSheet = null
        }

        addSheet = sheet
        sheetBinding = sb
        renderUploadState(bannerViewModel.isUploading.value == true)
        sheet.show()
    }

    private fun onImagePicked(uri: Uri) {
        val sb = sheetBinding ?: return
        sb.llPickPlaceholder.visibility = View.GONE
        sb.cardPreview.visibility = View.VISIBLE
        sb.tvChangeImage.visibility = View.VISIBLE
        sb.imgBannerPreview.setImageURI(uri)
        sb.tvFileInfo.visibility = View.VISIBLE
        sb.tvFileInfo.text = "Memproses gambar…"
        preparedPart = null

        prepareJob?.cancel()
        prepareJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val prepared = ImageUtils.prepareImagePart(requireContext(), uri)
                preparedPart = prepared.part
                sheetBinding?.tvFileInfo?.text =
                    "${prepared.width}×${prepared.height} px · ${ImageUtils.readableSize(prepared.sizeBytes)} · siap diunggah"
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                preparedPart = null
                sheetBinding?.tvFileInfo?.text = "Gagal membaca gambar"
                toast(e.message ?: "Gagal membaca gambar")
            }
        }
    }

    private fun renderUploadState(uploading: Boolean) {
        val sb = sheetBinding ?: return
        sb.btnUpload.isEnabled = !uploading
        sb.btnUpload.text = if (uploading) "" else "Unggah Banner"
        sb.progressUpload.visibility = if (uploading) View.VISIBLE else View.GONE
        sb.framePicker.isEnabled = !uploading
        sb.switchStatus.isEnabled = !uploading
        addSheet?.setCancelable(!uploading)
    }

    // =====================================================
    // UTIL
    // =====================================================
    private fun toast(msg: String) {
        context?.let { Toast.makeText(it, msg, Toast.LENGTH_SHORT).show() }
    }

    private fun handleBackPress() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroyView() {
        addSheet?.dismiss()
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val KEY_TAB_BANNER = "tab_banner"
    }
}
