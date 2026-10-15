package com.masfitatech.koprafin.page.main

import android.os.Bundle
import android.widget.Toast
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.masfitatech.koprafin.MainActivity
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.data.model.DataCategory
import com.masfitatech.koprafin.databinding.FragmentCatalogBinding
import com.masfitatech.koprafin.page.main.viewmodel.CategoryViewModel

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private lateinit var token: String

    private var pagerAdapter: FragmentStateAdapter? = null
    private var fragments = mutableListOf<Fragment>()
    private var titles = mutableListOf<String>()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).setToolbarTitle("Katalog")

        token = AuthToken.tokenLogin(requireContext()).orEmpty()

        setupSwipeRefresh()
        observeCategory()
        loadCategories()
        handleBackPress()
    }
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            categoryViewModel.fetchCategories(token)
        }
    }

    private fun loadCategories() {
        categoryViewModel.fetchCategories(token)
    }

    private fun observeCategory() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->

            binding.swipeRefresh.isRefreshing = false

            if (categories.isNullOrEmpty()) {
                binding.llEmptyCatalog.visibility = View.VISIBLE
                binding.tabCategories.visibility = View.GONE
                binding.viewPager.visibility = View.GONE
                return@observe
            }

            binding.llEmptyCatalog.visibility = View.GONE
            binding.tabCategories.visibility = View.VISIBLE
            binding.viewPager.visibility = View.VISIBLE

            setupViewPager(categories)
        }

        categoryViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            binding.swipeRefresh.isRefreshing = false
            if (!error.isNullOrEmpty())
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    // ====================================================
    //  REBUILD VIEWPAGER2 WHEN REFRESHED
    // ====================================================
    private fun setupViewPager(categories: List<DataCategory>) {

        fragments.clear()
        titles.clear()

        // Tab ALL
        fragments.add(ProductListFragment.newInstance(null))
        titles.add("Semua")

        // Tab per category
        categories.forEach { cat ->
            fragments.add(ProductListFragment.newInstance(cat.id))
            titles.add(cat.name ?: "-")
        }

        pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabCategories, binding.viewPager) { tab, pos ->
            tab.text = titles[pos]
        }.attach()
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
        pagerAdapter = null
    }
}
