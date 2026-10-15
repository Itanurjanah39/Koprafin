package com.masfitatech.koprafin

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
import com.masfitatech.koprafin.utils.StatusStyle

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
            binding.tvTenor.text = "${detail.tenor} bulan × ${RupiahUtils.toRupiah(installment)}"

            val total = detail.total?.toDoubleOrNull() ?: 0.0
            binding.tvTotal.text = RupiahUtils.toRupiah(total)

            binding.tvCreatedAt.text = "Diajukan: ${DateUtils.formatDate(detail.createdAt)}"

            binding.tvApplicantName.text = "${detail.employee?.name ?: "-"}"
            binding.tvApplicantNIK.text = "${detail.employee?.nik ?: "-"}"
            binding.tvApplicantPhone.text = "${detail.employee?.phone ?: "-"}"
            binding.tvApplicantCompany.text = "${detail.employee?.company ?: "-"}"
            binding.tvApplicantDivision.text = "${detail.employee?.division ?: "-"}"
            binding.tvApplicantWorkEntry.text = "${DateUtils.formatDate(detail.employee?.workEntryDate )}"

            setStatusBadge(detail.status)

            binding.timelineList.removeAllViews()

            detail.histories.forEachIndexed { index, history ->
                binding.timelineList.addView(
                    buildTimelineRow(
                        status = history.status,
                        date = DateUtils.formatDate(history.createdAt),
                        remarks = history.remarks,
                        isLast = index == detail.histories.lastIndex
                    )
                )
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

    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()

    /** Satu baris timeline: titik + garis di kiri, status/tanggal/catatan di kanan. */
    private fun buildTimelineRow(status: String?, date: String?, remarks: String?, isLast: Boolean): View {
        val ctx = requireContext()
        val colors = StatusStyle.colorsFor(status)

        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val rail = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(20.dp, LinearLayout.LayoutParams.MATCH_PARENT)
        }
        val dot = View(ctx).apply {
            background = ContextCompat.getDrawable(ctx, R.drawable.timeline_circle)
            layoutParams = LinearLayout.LayoutParams(14.dp, 14.dp).apply { topMargin = 3.dp }
        }
        rail.addView(dot)
        if (!isLast) {
            rail.addView(View(ctx).apply {
                setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorOutline))
                layoutParams = LinearLayout.LayoutParams(2.dp, 0, 1f).apply { topMargin = 2.dp }
            })
        }

        val content = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 12.dp
            }
            setPadding(0, 0, 0, if (isLast) 0 else 16.dp)
        }
        content.addView(TextView(ctx).apply {
            text = status?.replaceFirstChar { it.uppercase() } ?: "-"
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, colors.text))
        })
        content.addView(TextView(ctx).apply {
            text = date ?: "-"
            textSize = 12f
            setTextColor(ContextCompat.getColor(ctx, R.color.colorTextSecondary))
        })
        if (!remarks.isNullOrEmpty()) {
            content.addView(TextView(ctx).apply {
                text = remarks
                textSize = 13f
                setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary))
                background = ContextCompat.getDrawable(ctx, R.drawable.bg_card_mint)
                setPadding(12.dp, 8.dp, 12.dp, 8.dp)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 6.dp }
            })
        }

        row.addView(rail)
        row.addView(content)
        return row
    }

    private fun setStatusBadge(status: String?) {
        StatusStyle.apply(binding.tvStatus, status)
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