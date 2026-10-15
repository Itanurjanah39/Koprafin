package com.masfitatech.koprafin.page.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.masfitatech.koprafin.MainActivity
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.data.model.DataDetail
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.FragmentDetailProductBinding
import com.masfitatech.koprafin.page.main.viewmodel.CreditViewModel
import com.masfitatech.koprafin.page.main.viewmodel.DetailProductViewModel
import com.masfitatech.koprafin.page.product.AddProductActivity
import com.masfitatech.koprafin.utils.DialogUtils
import com.masfitatech.koprafin.utils.RupiahUtils

class DetailProduct : Fragment() {

    private lateinit var binding: FragmentDetailProductBinding
    private val detailViewModel: DetailProductViewModel by viewModels()
    private val creditViewModel: CreditViewModel by viewModels()

    private var totalPinjamanGlobal: Double = 0.0
    private var productId: Int = 0

    // launcher untuk edit (AddProductActivity)
    private val editProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Jika dari edit/delete kembali OK, maka tutup halaman detail
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getInt("product_id") ?: 0
        val token = AuthToken.tokenLogin(requireContext()).toString()
        val mainActivity = activity as MainActivity

        mainActivity.setToolbarTitle("Detail")
        mainActivity.hideToolbarEdit()

        val role = AuthToken.roleUser(requireContext())
        if (role == "admin") {
            binding.btnTambahKredit.visibility = View.GONE
            mainActivity.setToolbarEditVisibiliti()
        }

        observeViewModel()
        detailViewModel.fetchDetail(token, productId)
    }

    private fun observeViewModel() {

        val mainActivity = activity as MainActivity

        detailViewModel.detailData.observe(viewLifecycleOwner) { detail ->
            val pic = detail.picture?.firstOrNull()

            Glide.with(requireContext())
                .load(ApiClient.BASE_URL_IMAGE + (pic ?: ""))
                .placeholder(R.drawable.empty_product)
                .into(binding.imgMain)

            binding.tvName.text = detail.name
            binding.tvCategory.text = detail.category?.name ?: "-"
            binding.tvPrice.text = RupiahUtils.toRupiah(detail.price)
            binding.tvDescription.text = detail.description ?: "-"

            // Thumbnail
            renderThumbnails(detail.picture)

            binding.btnTambahKredit.setOnClickListener {
                showBottomSheetKredit(detail)
            }

            // === Registrasi aksi tombol edit toolbar ===
            val role = AuthToken.roleUser(requireContext())
            if (role == "admin") {
                mainActivity.setOnEditClickListener {
                    val intent = Intent(requireContext(), AddProductActivity::class.java)
                    intent.putExtra("product_id", detail.id)
                    editProductLauncher.launch(intent)
                }
            }
        }

        detailViewModel.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        creditViewModel.successApply.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                showSuccessDialog(totalPinjamanGlobal)
            }
        }

        creditViewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                DialogUtils.showErrorDialog(
                    context = requireContext(),
                    title = "Pengajuan Gagal",
                    message = msg
                )
            }
        }
    }

    private fun renderThumbnails(pictures: List<String>?) {
        binding.thumbContainer.removeAllViews()

        if (pictures.isNullOrEmpty()) return

        pictures.forEach { img ->
            val thumb = ImageView(requireContext())

            val params = LinearLayout.LayoutParams(70.dp, 70.dp)
            params.setMargins(8, 0, 8, 0)
            thumb.layoutParams = params
            thumb.scaleType = ImageView.ScaleType.CENTER_CROP
            thumb.background = ContextCompat.getDrawable(requireContext(), R.drawable.thumb_shape)
            thumb.clipToOutline = true

            Glide.with(requireContext())
                .load(ApiClient.BASE_URL_IMAGE + img)
                .placeholder(R.drawable.empty_product)
                .into(thumb)

            thumb.setOnClickListener {
                Glide.with(requireContext())
                    .load(ApiClient.BASE_URL_IMAGE + img)
                    .placeholder(R.drawable.empty_product)
                    .into(binding.imgMain)
            }

            binding.thumbContainer.addView(thumb)
        }
    }

    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()

    private fun showBottomSheetKredit(detail: DataDetail) {

        val view = layoutInflater.inflate(R.layout.bottomsheet_kredit, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        dialog.setContentView(view)

        val tvHargaValue = view.findViewById<TextView>(R.id.tvHargaValue)
        val tvTenor = view.findViewById<TextView>(R.id.tvTenor)
        val seekTenor = view.findViewById<SeekBar>(R.id.seekTenor)
        val tvAngsuran = view.findViewById<TextView>(R.id.tvAngsuran)
        val btnAjukan = view.findViewById<AppCompatButton>(R.id.btnAjukan)

        val price = detail.price?.toDoubleOrNull() ?: 0.0
        tvHargaValue.text = RupiahUtils.toRupiah(price)

        val minTenor = detail.category?.minTenor ?: 3
        val maxTenor = detail.category?.maksTenor ?: 12

        seekTenor.max = maxTenor
        seekTenor.progress = minTenor

        var tenor = minTenor
        tvTenor.text = tenor.toString()

        val scaleAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_in)

        fun hitungAngsuran() {
            val categoryId = detail.category?.id
            val angsuran: Double

            if (categoryId == 3 || categoryId == 4) {
                totalPinjamanGlobal = price
                angsuran = totalPinjamanGlobal / tenor
            } else {
                val margin = price * 0.10
                totalPinjamanGlobal = price + margin
                angsuran = totalPinjamanGlobal / tenor
            }

            tvAngsuran.text = RupiahUtils.toRupiah(angsuran)
        }

        hitungAngsuran()

        seekTenor.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?, value: Int, fromUser: Boolean
            ) {
                if (value >= minTenor) {
                    tenor = value
                    tvTenor.text = tenor.toString()
                    tvTenor.startAnimation(scaleAnim)
                    tvAngsuran.startAnimation(scaleAnim)
                    hitungAngsuran()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        btnAjukan.setOnClickListener {
            dialog.dismiss()

            val token = AuthToken.tokenLogin(requireContext()).toString()
            val productId = detail.id ?: 0

            creditViewModel.applyCredit(
                token = token,
                productId = productId,
                amount = price.toInt(),
                tenor = tenor
            )
        }

        dialog.show()
    }

    private fun showSuccessDialog(total: Double) {
        val view = layoutInflater.inflate(R.layout.dialog_credit_success, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        dialog.setContentView(view)

        val tvPinjaman = view.findViewById<TextView>(R.id.tvPinjaman)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotal)
        val btnKembali = view.findViewById<AppCompatButton>(R.id.btnKembali)

        tvPinjaman.text = RupiahUtils.toRupiah(total)
        tvTotal.text = RupiahUtils.toRupiah(total)

        btnKembali.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.hideToolbarEdit()
    }
}
