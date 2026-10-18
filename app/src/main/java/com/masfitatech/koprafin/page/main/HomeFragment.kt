package com.masfitatech.koprafin.page.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.masfitatech.koprafin.MainActivity
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.auth.AuthToken
import com.masfitatech.koprafin.data.auth.SessionManager
import com.masfitatech.koprafin.databinding.FragmentHomeBinding
import com.masfitatech.koprafin.page.login.LoginActivity
import com.masfitatech.koprafin.page.main.adapter.HeroCarouselAdapter
import com.masfitatech.koprafin.page.main.adapter.HomeProductAdapter
import com.masfitatech.koprafin.page.main.adapter.ShimmerAdapter
import com.masfitatech.koprafin.page.main.viewmodel.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var token: String
    private var katalogLoaded = false
    private lateinit var productAdapter: HomeProductAdapter
    private lateinit var shimmerAdapter: ShimmerAdapter
    private lateinit var heroAdapter: HeroCarouselAdapter

    // viewmodel
    private val bannerViewModel: BannerViewModel by activityViewModels()
    private val katalogViewModel: KatalogViewModel by activityViewModels()
    private val creditStatusViewModel: CreditStatusViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()

    // autoscroll carousel hero
    private val bannerHandler = Handler(Looper.getMainLooper())

    private val bannerRunnable = object : Runnable {
        override fun run() {
            val b = _binding ?: return
            if (!::heroAdapter.isInitialized) return

            val count = heroAdapter.itemCount
            if (count > 1) {
                b.vpBanner.currentItem = (b.vpBanner.currentItem + 1) % count
            }
            bannerHandler.postDelayed(this, 5000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val main = activity as MainActivity
        main.setToolbarTitle("Beranda")
        main.showProfileShortcut(true)

        sessionManager = SessionManager(requireContext())
        token = AuthToken.tokenLogin(requireContext()).orEmpty()
        setGreeting(sessionManager.getName())

        setupHeroCarousel()
        setupRecycler()
        setupActions()
        setupSwipeRefresh()
        observeBanner()
        observeAccount()
        observeViewModel()

        bannerViewModel.getActiveBanners(token)
        creditStatusViewModel.fetchStatus(token)
        accountViewModel.getAccount(token)

        if (!katalogLoaded) {
            katalogLoaded = true
            katalogViewModel.fetchProducts(token, perPage = 10)
        }
    }

    override fun onResume() {
        super.onResume()
        bannerHandler.removeCallbacks(bannerRunnable)
        bannerHandler.postDelayed(bannerRunnable, 5000)
    }

    override fun onPause() {
        super.onPause()
        bannerHandler.removeCallbacks(bannerRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerHandler.removeCallbacks(bannerRunnable)
        (activity as? MainActivity)?.showProfileShortcut(false)
        _binding = null
    }

    // =====================================================
    // SETUP
    // =====================================================
    private fun setGreeting(fullName: String?) {
        val name = fullName?.trim().orEmpty()
        binding.tvGreetingName.text = if (name.isEmpty()) "Halo 👋" else "Halo, $name 👋"
    }

    private fun setupHeroCarousel() {
        heroAdapter = HeroCarouselAdapter()

        binding.vpBanner.apply {
            adapter = heroAdapter
            offscreenPageLimit = 3
        }

        TabLayoutMediator(binding.tabIndicator, binding.vpBanner) { _, _ -> }.attach()
        updateIndicatorVisibility()
    }

    private fun setupRecycler() {
        shimmerAdapter = ShimmerAdapter(4)
        binding.recyclerShimmer.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = shimmerAdapter
        }

        productAdapter = HomeProductAdapter { product ->
            openDetail(product.id ?: 0)
        }
        binding.recyclerProductsHome.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun setupActions() {
        val main = activity as MainActivity
        binding.btnSeeAllCredit.setOnClickListener { main.navigateTo(R.id.nav_history) }
        binding.btnSeeAllProduct.setOnClickListener { main.navigateTo(R.id.nav_catalog) }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.brandPrimary, R.color.brandGreen)
        binding.swipeRefresh.setOnRefreshListener {
            creditStatusViewModel.fetchStatus(token)
            katalogViewModel.fetchProducts(token, perPage = 10)
            accountViewModel.getAccount(token)
            bannerViewModel.getActiveBanners(token)
        }

        katalogViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefresh.isRefreshing = loading
        }
    }

    private fun updateIndicatorVisibility() {
        binding.tabIndicator.visibility =
            if (heroAdapter.itemCount > 1) View.VISIBLE else View.INVISIBLE
    }

    // =====================================================
    // OBSERVER
    // =====================================================
    private fun observeBanner() {
        bannerViewModel.bannersActive.observe(viewLifecycleOwner) { banners ->
            // Slide brand selalu ada; banner aktif ditambahkan setelahnya
            heroAdapter.updateData(banners.orEmpty())
            updateIndicatorVisibility()
        }
    }

    private fun observeAccount() {
        accountViewModel.accountData.observe(viewLifecycleOwner) { data ->
            data?.employee?.name?.let { setGreeting(it) }
        }

        accountViewModel.errorMessage.observe(viewLifecycleOwner) {
            showToast(it)
        }

        accountViewModel.sessionExpired.observe(viewLifecycleOwner) { expired ->
            if (expired == true) {
                onSessionExpired()
            }
        }
    }

    private fun onSessionExpired() {
        Toast.makeText(
            requireContext(),
            "Session habis, silakan login kembali",
            Toast.LENGTH_LONG
        ).show()

        sessionManager.clearSession()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finishAffinity()
    }

    private fun observeViewModel() {
        katalogViewModel.products.observe(viewLifecycleOwner) { products ->
            if (products.isNullOrEmpty()) {
                binding.recyclerProductsHome.visibility = View.GONE
                binding.recyclerShimmer.visibility = View.GONE
                binding.llEmptyData.visibility = View.VISIBLE
            } else {
                binding.recyclerProductsHome.visibility = View.VISIBLE
                binding.recyclerShimmer.visibility = View.GONE
                binding.llEmptyData.visibility = View.GONE
                productAdapter.updateData(products)
            }
        }

        katalogViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.recyclerShimmer.visibility = if (loading) View.VISIBLE else View.GONE
        }

        katalogViewModel.errorMessage.observe(viewLifecycleOwner) {
            showToast(it)
        }

        creditStatusViewModel.statusData.observe(viewLifecycleOwner) { data ->
            data?.let {
                binding.tvCountStatus.text = (it.pengajuan ?: 0).toString()
                binding.tvCountStatus1.text = (it.berjalan ?: 0).toString()
                binding.tvCountStatus2.text = (it.selesai ?: 0).toString()
                binding.tvCountStatus3.text = (it.ditolak ?: 0).toString()
            }
        }

        creditStatusViewModel.errorMessage.observe(viewLifecycleOwner) {
            showToast(it)
        }
    }

    // =====================================================
    // NAVIGATION
    // =====================================================
    private fun openDetail(productId: Int) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DetailProduct().apply {
                arguments = Bundle().apply {
                    putInt("product_id", productId)
                }
            })
            .addToBackStack(null)
            .commit()
    }

    // =====================================================
    // UTIL
    // =====================================================
    private fun showToast(message: String?) {
        if (!message.isNullOrEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
