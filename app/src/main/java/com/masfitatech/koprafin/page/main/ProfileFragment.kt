package com.masfitatech.koprafin.page.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.MainActivity
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.FragmentProfileBinding
import com.masfitatech.koprafin.page.main.viewmodel.AccountViewModel
import com.masfitatech.koprafin.utils.DateUtils

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: AccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleBackPress()

        val token = AuthToken.tokenLogin(requireContext()).toString()

        (activity as MainActivity).setToolbarTitle("Profil")
        observeViewModel()
        viewModel.getAccount(token)
    }

    private fun handleBackPress() {

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callback
        )
    }

    private fun observeViewModel() {

        viewModel.accountData.observe(viewLifecycleOwner) { data ->
            if (data != null) {

                Glide.with(requireContext())
                    .load(ApiClient.BASE_URL_IMAGE + data.employee?.avatar)
                    .placeholder(R.drawable.user)
                    .into(binding.profileImage)

                val role = AuthToken.roleUser(requireContext())

                if (role == "admin") {
                    binding.tvNama.text = data.employee?.name ?: "-"
                } else {
                    binding.tvNama.text =
                        "${data.employee?.name} - ${data.employee?.nik}" ?: "-"
                }

                binding.tvJabatan.text = data.employee?.role ?: "-"

                binding.etHp.setText(data.employee?.phone ?: "-")
                binding.etCompany.setText(data.employee?.company ?: "-")
                binding.etDivisi.setText(data.employee?.division ?: "-")

                binding.etJoinDate.setText(
                    DateUtils.formatDate(data.employee?.workEntryDate)
                )
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
