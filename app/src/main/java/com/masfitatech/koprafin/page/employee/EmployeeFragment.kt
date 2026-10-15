package com.masfitatech.koprafin.page.employee

import androidx.core.widget.addTextChangedListener
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.masfitatech.koprafin.MainActivity
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.databinding.FragmentEmployeeBinding
import com.masfitatech.koprafin.page.employee.viewmodel.EmployeeViewModel
import com.masfitatech.koprafin.ui.employee.adapter.EmployeeAdapter
import com.masfitatech.koprafin.utils.DialogUtils
import com.masfitatech.koprafin.page.main.HomeFragment

class EmployeeFragment : Fragment() {

    private var _binding: FragmentEmployeeBinding? = null
    private val binding get() = _binding!!

    private val employeeViewModel: EmployeeViewModel by activityViewModels()
    private lateinit var adapter: EmployeeAdapter
    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).setToolbarTitle("Data Karyawan")
        token = AuthToken.tokenLogin(requireContext()).orEmpty()

        handleBackPress()
        setupRecycler()
        observeViewModel()
        setupSwipeRefresh()

        binding.FABAdd.setOnClickListener {
            startActivity(Intent(requireContext(), DetailEmployeeActivity::class.java))
        }

        binding.etSearch.addTextChangedListener { text ->
            adapter.filter(text.toString())
            updateEmptyState(adapter.currentList)
        }

        employeeViewModel.getEmployees(token)
    }

    private fun setupRecycler() {
        adapter = EmployeeAdapter(
            employeeList = emptyList(),
            onClick = { item ->
                val intent = Intent(requireContext(), DetailEmployeeActivity::class.java)
                    .putExtra("id", item.id)
                startActivity(intent)
            },
            onDelete = { item ->
                DialogUtils.showConfirmDialog(
                    context = requireContext(),
                    title = "Hapus Karyawan",
                    message = "Yakin ingin menghapus ${item.name} ?",
                    onConfirm = {
                        employeeViewModel.deleteEmployee(token, item.id)
                    }
                )
            }
        )

        binding.rvEmployee.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEmployee.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            employeeViewModel.getEmployees(token)
        }
    }

    private fun observeViewModel() {

        // Update list employee
        employeeViewModel.employees.observe(viewLifecycleOwner) { list ->
            binding.swipeRefresh.isRefreshing = false

            adapter.updateData(list)
            adapter.filter(binding.etSearch.text.toString())

            updateEmptyState(adapter.currentList)
        }

        employeeViewModel.deleteSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                employeeViewModel.getEmployees(token)
                Toast.makeText(requireContext(), "Karyawan dihapus!", Toast.LENGTH_SHORT).show()
            }
        }

        employeeViewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            binding.swipeRefresh.isRefreshing = false
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        employeeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefresh.isRefreshing = loading
        }
    }

    private fun updateEmptyState(list: List<*>) {
        if (list.isEmpty()) {
            binding.llEmptyList.visibility = View.VISIBLE
            binding.rvEmployee.visibility = View.GONE
        } else {
            binding.llEmptyList.visibility = View.GONE
            binding.rvEmployee.visibility = View.VISIBLE
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

    override fun onResume() {
        super.onResume()
        employeeViewModel.getEmployees(token)
    }
}
