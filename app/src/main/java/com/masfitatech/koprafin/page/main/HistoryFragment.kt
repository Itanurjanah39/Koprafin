package com.masfitatech.koprafin.page.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.masfitatech.koprafin.HistoryDetailFragment
import com.masfitatech.koprafin.MainActivity
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.databinding.FragmentHistoryBinding
import com.masfitatech.koprafin.page.main.viewmodel.HistoryViewModel
import com.masfitatech.koprafin.ui.main.adapter.HistoryAdapter
import kotlin.getValue
import androidx.activity.OnBackPressedCallback


class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val historyViewModel: HistoryViewModel by activityViewModels()
    private lateinit var adapter: HistoryAdapter
    private lateinit var token: String

    private var historyLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        token = AuthToken.tokenLogin(requireContext()).toString()
        (activity as MainActivity).setToolbarTitle("Riwayat")
        handleBackPress()


        parentFragmentManager.setFragmentResultListener("history_update", viewLifecycleOwner) { _, _ ->
            historyViewModel.getHistory(token)
        }


        setupRecyclerView()
        observeViewModel()
        setupSwipeRefresh()


        if (!historyLoaded) {
            historyLoaded = true
            historyViewModel.getHistory(token)
        }
    }

    private fun setupRecyclerView() {

        adapter = HistoryAdapter(emptyList()) { item ->
            openDetail(item.id ?: 0)
        }


        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }
    }


    private fun openDetail(creditId: Int) {
        val fragment = HistoryDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("credit_id", creditId)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            historyViewModel.getHistory(token)
        }
    }

    private fun observeViewModel() {

        historyViewModel.history.observe(viewLifecycleOwner) { list ->
            binding.swipeRefresh.isRefreshing = false

            if (list.isNullOrEmpty()) {
                binding.llEmptyList.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
            } else {
                binding.llEmptyList.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
                adapter.updateData(list)
            }
        }

        // ERROR
        historyViewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            binding.swipeRefresh.isRefreshing = false

            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        historyViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefresh.isRefreshing = loading
        }
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
        super.onDestroyView()
        _binding = null
    }
}
