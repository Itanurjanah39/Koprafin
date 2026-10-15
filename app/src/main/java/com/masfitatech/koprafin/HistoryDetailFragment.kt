package com.masfitatech.koprafin

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.FragmentHistoryDetailBinding
import com.masfitatech.koprafin.page.main.viewmodel.HistoryViewModel
import com.masfitatech.koprafin.utils.DateUtils
import com.masfitatech.koprafin.utils.DialogUtils
import com.masfitatech.koprafin.utils.RupiahUtils

class HistoryDetailFragment : Fragment() {

    private lateinit var binding: FragmentHistoryDetailBinding
    private val viewModel: HistoryViewModel by activityViewModels()
    private var creditId = 0
    private var role: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHistoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setToolbarTitle("Detail Kredit")
        creditId = arguments?.getInt("credit_id") ?: 0
        role = AuthToken.roleUser(requireContext()).toString()
        observeDetail()
        viewModel.getHistoryDetail(AuthToken.tokenLogin(requireContext()).toString(), creditId)
    }

    private fun observeDetail() {

        viewModel.historyDetail.observe(viewLifecycleOwner) { detail ->
            binding.progressBar.visibility = View.GONE
            if (detail == null) return@observe

            Glide.with(requireContext())
                .load(ApiClient.BASE_URL_IMAGE + detail.product?.picture?.firstOrNull())
                .placeholder(R.drawable.empty_product)
                .into(binding.imgProduct)

            binding.tvProductName.text = detail.product?.name ?: "-"
            binding.tvProductDescription.text = detail.product?.description ?: "-"
            binding.tvCategory.text = detail.product?.category?.name ?: "-"

            val installment = detail.installment?.toDoubleOrNull() ?: 0.0
            binding.tvTenor.text = "${detail.tenor} x ${RupiahUtils.toRupiah(installment)}"

            val total = detail.total?.toDoubleOrNull() ?: 0.0
            binding.tvTotal.text = "Total: ${RupiahUtils.toRupiah(total)}"

            binding.tvCreatedAt.text = "Diajukan: ${DateUtils.formatDate(detail.createdAt)}"

            binding.tvApplicantName.text = "${detail.employee?.name ?: "-"}"
            binding.tvApplicantNIK.text = "${detail.employee?.nik ?: "-"}"
            binding.tvApplicantPhone.text = "${detail.employee?.phone ?: "-"}"
            binding.tvApplicantCompany.text = "${detail.employee?.company ?: "-"}"
            binding.tvApplicantDivision.text = "${detail.employee?.division ?: "-"}"
            binding.tvApplicantWorkEntry.text = "${DateUtils.formatDate(detail.employee?.workEntryDate )}"

            setStatusBadge(detail.status)

            binding.timelineList.removeAllViews()

            detail.histories.forEach { history ->

                val textView = TextView(requireContext()).apply {
                    textSize = 14f
                    setPadding(0, 6, 0, 6)
                }

                val statusText = "${history.status}  • ${DateUtils.formatDate(history.createdAt)}"

                if (history.remarks.isNullOrEmpty()) {
                    // Jika tidak ada remarks → tampilkan biasa
                    textView.text = statusText
                } else {
                    val remarksText = "\n${history.remarks}"
                    val fullText = statusText + remarksText

                    val spannable = SpannableString(fullText)

                    // Warnai remarks
                    val remarksColor = ContextCompat.getColor(requireContext(), R.color.colorTextSecondary)

                    spannable.setSpan(
                        ForegroundColorSpan(remarksColor),
                        statusText.length,           // mulai dari setelah statusText
                        fullText.length,             // sampai akhir
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    textView.text = spannable
                }

                binding.timelineList.addView(textView)
            }

            if (role == "admin" && detail.status == "pengajuan") {
                binding.llAdminAction.visibility = View.VISIBLE

                binding.btnApprove.setOnClickListener { confirmApprove(detail.id!!) }
                binding.btnReject.setOnClickListener { confirmReject(detail.id!!) }

            } else {
                binding.llAdminAction.visibility = View.GONE
            }
            if (role == "admin" && detail.status == "berjalan") {
                binding.btnFinish.visibility = View.VISIBLE

                binding.btnFinish.setOnClickListener { confirmFinish(detail.id!!, detail.employee?.name.toString()) }

            } else {
                binding.btnFinish.visibility = View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setStatusBadge(status: String?) {
        val st = status?.lowercase() ?: ""

        binding.tvStatus.text = st.replaceFirstChar { it.uppercase() }

        val ctx = requireContext()
        val colorRes = when (st) {
            "pengajuan" -> R.color.statusPengajuan
            "berjalan"  -> R.color.statusBerjalan
            "selesai"   -> R.color.colorPrimary
            "ditolak"   -> R.color.statusDitolak
            else        -> R.color.statusDefault
        }

        binding.tvStatus.backgroundTintList =
            ContextCompat.getColorStateList(ctx, colorRes)
    }


    private fun confirmApprove(id: Int) {
        DialogUtils.showConfirmDialog(
            requireContext(),
            "Terima Pengajuan",
            "Apakah Anda yakin ingin menyetujui pengajuan ini?"
        ) {
            val token = AuthToken.tokenLogin(requireContext()).toString()
            viewModel.creditAction(token, id, "approve")
            Toast.makeText(requireContext(), "Pengajuan diterima", Toast.LENGTH_SHORT).show()
            parentFragmentManager.setFragmentResult("history_update", Bundle())
            requireActivity().onBackPressedDispatcher.onBackPressed()

        }
    }

    private fun confirmFinish(id: Int, name : String) {
        DialogUtils.showConfirmDialog(
            requireContext(),
            "Kredit Selesai",
            "Apakah kredit atas nama $name benar-benar sudah selesai ?"
        ) {
            val token = AuthToken.tokenLogin(requireContext()).toString()
            viewModel.creditFinishing(token, id, "Credit fully paid")
            Toast.makeText(requireContext(), "Kredit telah diterima", Toast.LENGTH_SHORT).show()
            parentFragmentManager.setFragmentResult("history_update", Bundle())
            requireActivity().onBackPressedDispatcher.onBackPressed()

        }
    }

    private fun confirmReject(id: Int) {
        DialogUtils.showInputDialog(
            context = requireContext(),
            title = "Penolakan Pengajuan",
            titleInput = "Keterangan",
            positiveText = "Kirim",
            cancelText = "Batal"
        ) { remarks ->

            if (remarks.isEmpty()) {
                Toast.makeText(requireContext(), "Keterangan wajib diisi", Toast.LENGTH_SHORT).show()
                return@showInputDialog
            }

            val token = AuthToken.tokenLogin(requireContext()).toString()

            viewModel.creditAction(
                token = token,
                id = id,
                action = "reject",
                remarks = remarks
            )

            Toast.makeText(requireContext(), "Pengajuan ditolak", Toast.LENGTH_SHORT).show()
            parentFragmentManager.setFragmentResult("history_update", Bundle())
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

}