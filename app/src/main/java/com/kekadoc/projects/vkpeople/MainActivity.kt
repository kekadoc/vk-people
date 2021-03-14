package com.kekadoc.projects.vkpeople

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import coil.load
import com.kekadoc.tools.android.ThemeColor
import com.kekadoc.tools.android.drawable
import com.kekadoc.tools.android.view.themeColor
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.VKDefaultValidationHandler
import okhttp3.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG: String = "MainActivity-TAG"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: ViewModelActivity by viewModels()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, viewModel.vkAuthCallback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = VKApiConfig(
                context = this,
                appId = resources.getIdentifier("com_vk_sdk_AppId", "integer", packageName),
                validationHandler = VKDefaultValidationHandler(this),
                lang = "ru")
        VK.setConfig(config)

        VK.addTokenExpiredHandler(viewModel.tokenTracker)

        viewModel.requestCurrentUser()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById<NavigationView>(R.id.nav_view).apply {

            menu.findItem(R.id.menuItem_login).setOnMenuItemClickListener {
                viewModel.logIn(this@MainActivity)
                return@setOnMenuItemClickListener true
            }
            menu.findItem(R.id.menuItem_logout).setOnMenuItemClickListener {
                viewModel.logOut()
                return@setOnMenuItemClickListener true
            }

            val linearLayout: LinearLayout = this.getHeaderView(0).findViewById(R.id.linearLayout_navHeader)
            val imageView: ImageView = this.getHeaderView(0).findViewById(R.id.imageView)
            val textViewFirst: TextView = this.getHeaderView(0).findViewById(R.id.textView_first)
            val textViewSecond: TextView = this.getHeaderView(0).findViewById(R.id.textView_second)

            linearLayout.background = RippleDrawable(ColorStateList.valueOf(themeColor(ThemeColor.RIPPLE)), drawable(R.drawable.side_nav_bar), null)
            linearLayout.setOnClickListener {
                viewModel.currentUser.value?.let { user ->
                    user.id.let { startVKUserProfile(it) }
                }
            }

            viewModel.currentUser.observe(this@MainActivity) {
                if (it == null) {
                    imageView.setImageDrawable(null)
                    textViewFirst.text = null
                    textViewSecond.text = null
                } else {
                    imageView.load(it.photo)
                    textViewFirst.text = it.domain
                    textViewSecond.text = it.getFullName()
                }
            }

        }

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.destination_service_randomUsers), drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        viewModel.currentUser.observe(this) {
            if (it == null) {
                navView.menu.findItem(R.id.menuItem_login).apply {
                    this.isVisible = true
                }
                navView.menu.findItem(R.id.menuItem_logout).apply {
                    this.isVisible = false
                }
            } else {
                navView.menu.findItem(R.id.menuItem_login).apply {
                    this.isVisible = false
                }
                navView.menu.findItem(R.id.menuItem_logout).apply {
                    this.isVisible = true
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}