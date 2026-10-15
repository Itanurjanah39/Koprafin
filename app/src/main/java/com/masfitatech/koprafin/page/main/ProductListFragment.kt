package com.masfitatech.koprafin.page.main

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.data.auth.SessionManager
import com.masfitatech.koprafin.data.model.DataProduct
import com.masfitatech.koprafin.databinding.FragmentProductListBinding
import com.masfitatech.koprafin.page.main.adapter.ProductAdapter
import com.masfitatech.koprafin.page.main.viewmodel.KatalogViewModel
import com.masfitatech.koprafin.page.product.AddProductActivity
import androidx.core.widget.addTextChangedListener


class ProductListFragment : Fragment() {

    private lateinit var viewModel: KatalogViewModel
    private lateinit var binding: FragmentProductListBinding
    private lateinit var adapter: ProductAdapter
    private lateinit var token: String

    private var categoryId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoryId = arguments?.getInt("categoryId", -1)
            ?.takeIf { it != -1 }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        token = AuthToken.tokenLogin(requireContext()).orEmpty()

        viewModel = ViewModelProvider(this)[KatalogViewModel::class.java]

        setupRecycler()
        setupFAB()
        observeProducts()

        viewModel.fetchProducts(token, categoryId)

        binding.etSearch.addTextChangedListener { text ->
            adapter.filter(text.toString())
            updateEmptyState(adapter.currentList)
        }

    }

    private fun setupRecycler() {
        adapter = ProductAdapter(mutableListOf()) {
            openDetail(it.id ?: 0)
        }

        binding.recyclerViewProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewProducts.adapter = adapter
    }

    private fun setupFAB() {
        val isAdmin = SessionManager(requireContext()).getRole() == "admin"
        binding.FABAddProduct.visibility = if (isAdmin) View.VISIBLE else View.GONE

        binding.FABAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductActivity::class.java))
        }
    }

    private fun observeProducts() {
        viewModel.products.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            updateEmptyState(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    private fun updateEmptyState(list: List<DataProduct>) {
        binding.llEmptyData.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerViewProducts.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun openDetail(productId: Int) {
        val fragment = DetailProduct().apply {
            arguments = Bundle().apply { putInt("product_id", productId) }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun newInstance(categoryId: Int?) = ProductListFragment().apply {
            arguments = Bundle().apply {
                categoryId?.let { putInt("categoryId", it) }
            }
        }
    }
}
