package com.first.fooderie.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.first.fooderie.R
import com.first.fooderie.fragment.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.navigation_header.view.*

class DashboardActivity : AppCompatActivity() {
    private lateinit var toolbar : MaterialToolbar
    private lateinit var dlDrawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var previousMenuItem: MenuItem? = null
    private lateinit var frame: FrameLayout
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        toolbar = findViewById(R.id.toolbar)
        dlDrawer = findViewById(R.id.dlDrawer)
        navigationView = findViewById(R.id.navigationView)
        frame = findViewById(R.id.frame)
        initUI()
        openFragment(HomeFragment(),"Home")
    }

    private fun initUI() {
        initNavigation()
        initNavigationHeader()
        initToolbar()
        initToggle()
    }

    private fun initNavigation() {
        navigationView.setCheckedItem(R.id.miHome)
        navigationView.setNavigationItemSelectedListener {
            check(it)
            when (it.itemId) {
                R.id.miHome -> openFragment(
                    HomeFragment(), "Home")
                R.id.miProfile -> openFragment(
                    ProfileFragment(), "Profile")
                R.id.miFavourite -> openFragment(
                    FavouriteFragment(),"Favourites")
                R.id.miOrder -> openFragment(
                    OrderFragment(),"Order History")
                R.id.miFAQ -> openFragment(
                    FaqFragment(),"Frequently Asked Questions")
                R.id.miLogout -> logout()
            }
            dlDrawer.closeDrawers()
            return@setNavigationItemSelectedListener true
        }
    }
    private fun redirectToLogin(){
        val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun logout() {
        MaterialAlertDialogBuilder(this@DashboardActivity)
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to exit?")
            .setNegativeButton("No"){dialog,_->
                dialog.dismiss()
            }
            .setPositiveButton("Yes"){_,_->
                sharedPreferences.edit().clear().apply()
                redirectToLogin()
            }
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            dlDrawer.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onBackPressed() {
        when(supportFragmentManager.findFragmentById(R.id.frame)){
            !is HomeFragment -> openFragment(
                HomeFragment(), "Dashboard")
            else -> super.onBackPressed()
        }
    }
    private fun initToggle() {

        val action = ActionBarDrawerToggle(
            this@DashboardActivity,
            dlDrawer,
            R.string.open_drawer,
            R.string.close_drawer
        )
        dlDrawer.addDrawerListener(action)
        action.syncState()
    }
    private fun openFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction().replace(R.id.frame, fragment).commit()
        supportActionBar?.title = title
    }
    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun initNavigationHeader() {
        val navigationHeader = navigationView.getHeaderView(0)
        navigationHeader.txtName.text = sharedPreferences.getString("name","Anonymous")
        navigationHeader.txtPhone.text = "+91-${sharedPreferences.getString("mobile_number","no number")}"
        navigationHeader.btnBackIcon.setOnClickListener {
            dlDrawer.closeDrawers()
        }
    }

    private fun check(item: MenuItem) {
        item.isCheckable = true
        item.isChecked = true
    }
}