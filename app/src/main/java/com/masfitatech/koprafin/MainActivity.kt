package com.masfitatech.koprafin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.masfitatech.koprafin.data.auth.SessionManager
import com.masfitatech.koprafin.databinding.ActivityMainBinding
import com.masfitatech.koprafin.page.employee.EmployeeFragment
import com.masfitatech.koprafin.page.login.LoginActivity
import com.masfitatech.koprafin.page.main.CatalogFragment
import com.masfitatech.koprafin.page.main.HistoryFragment
import com.masfitatech.koprafin.page.main.HomeFragment
import com.masfitatech.koprafin.page.main.ProfileFragment
import com.masfitatech.koprafin.page.main.SettingFragment
import com.masfitatech.koprafin.utils.DialogUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private var role: String = ""

    private var editListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        role = sessionManager.getRole().toString()

        setupDrawer()
        setupMenuButton()
        setupEditButton()
        replaceFragment(HomeFragment())
    }

    private fun setupDrawer() {
        val menu = binding.navigationView.menu
        val employeeItem = menu.findItem(R.id.nav_employee)
        val settingItem = menu.findItem(R.id.nav_setting)
        employeeItem.isVisible = role == "admin"
        settingItem.isVisible = role == "admin"

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_catalog -> replaceFragment(CatalogFragment())
                R.id.nav_history -> replaceFragment(HistoryFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
                R.id.nav_employee -> replaceFragment(EmployeeFragment())
                R.id.nav_setting -> replaceFragment(SettingFragment())
                R.id.nav_logout -> DialogUtils.showConfirmDialog(
                    context = this,
                    title = "Konfirmasi Logout",
                    message = "Apakah anda yakin ingin keluar?",
                    onConfirm = {
                        sessionManager.clearSession()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                    }
                )
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupMenuButton() {
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupEditButton() {
        binding.btnEdit.visibility = View.GONE
        binding.btnEdit.setOnClickListener {
            editListener?.invoke()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }


    fun setToolbarTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun setToolbarEditVisibiliti() {
        binding.btnEdit.visibility = View.VISIBLE
    }

    fun hideToolbarEdit() {
        binding.btnEdit.visibility = View.GONE
        editListener = null
    }

    fun setOnEditClickListener(listener: () -> Unit) {
        editListener = listener
        binding.btnEdit.setOnClickListener {
            editListener?.invoke()
        }
    }

    override fun onBackPressed() {

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // Jika bukan HomeFragment, kembali dulu ke Home
        if (currentFragment !is HomeFragment) {
            replaceFragment(HomeFragment())
            return
        }

        // Jika sudah di Home → tekan back dua kali untuk keluar
        if (backPressedOnce) {
            super.onBackPressed()
            return
        }

        backPressedOnce = true
        Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            backPressedOnce = false
        }, 2000)
    }

    private var backPressedOnce = false

}
