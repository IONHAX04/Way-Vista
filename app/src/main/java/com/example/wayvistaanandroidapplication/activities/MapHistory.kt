package com.example.wayvistaanandroidapplication.activities

import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayvistaanandroidapplication.R
import com.example.wayvistaanandroidapplication.databinding.FragmentMapHistoryBinding
import com.example.wayvistaanandroidapplication.service.LocationRecView
import com.example.wayvistaanandroidapplication.utilities.Actions
import com.example.wayvistaanandroidapplication.utilities.MapLocation
import com.example.wayvistaanandroidapplication.utilities.UserDetails
import com.google.android.material.navigation.NavigationView
import com.makeramen.roundedimageview.RoundedImageView

class MapHistory : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mainBinding: FragmentMapHistoryBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actions: Actions
    private lateinit var userName: TextView
    private lateinit var userImg: RoundedImageView

    private lateinit var data: List<MapLocation>
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationLat: TextView
    private lateinit var locationLong: TextView
    private lateinit var time: TextView
    private lateinit var handler: Handler
    private var userDetails: List<UserDetails> = emptyList()

    private lateinit var sharedPreferences: SharedPreferences


    companion object {
        var currentData: Int = 0
        var userId: Int = MainActivity.userId
        lateinit var locationData: List<MapLocation>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = FragmentMapHistoryBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val toolbar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)

        navigationView.bringToFront()
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            (R.string.open_navigation_drawer),
            (R.string.close_navigation_drawer)
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            MapHistory()
            navigationView.setCheckedItem(R.id.maps)
        }
        loadUserDetails()
    }

    private fun loadUserDetails() {
        val headerBinding = mainBinding.navigationView.getHeaderView(0)
        userName = headerBinding.findViewById<TextView>(R.id.userName)
        userImg = headerBinding.findViewById<RoundedImageView>(R.id.userImg)
        actions = ViewModelProvider(this)[Actions::class.java]

        userName.text = sharedPreferences.getString("USERNAME", "User")
        val imgString = sharedPreferences.getString("USER_IMAGE", "")
        if (imgString != null && imgString.isNotEmpty()) {
            try {
                val bytes = Base64.decode(imgString, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                userImg.setImageBitmap(bitmap)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "No image data available", Toast.LENGTH_SHORT).show()
        }

        handler = Handler(Looper.getMainLooper())

        recyclerView = findViewById(R.id.locationHistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        actions.getLocation(userId)

        actions.locationData.observe(this) {
            locationData = it
            if (locationData.isNotEmpty()) {
                recyclerView.adapter = LocationRecView(this, locationData) { index ->
                    currentData = index
                    startActivity(Intent(this, MapViewActivity::class.java))
                }
            } else {
                Toast.makeText(this, "No location history available", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> startMainActivity()
            R.id.maps -> startMapHistory()
            R.id.user -> startProfile()
            R.id.rate -> showReviewDialog()
            R.id.about -> replaceFragment(AboutUsFragment())
            R.id.logout -> logout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        if (userDetails.size == 1) {
            logoutFun()
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putInt("USER_ID", 0)
            editor.putString("USERNAME", "")
            editor.putString("USER_IMG", "")
            editor.putString("USER_EMAIL", "")
            editor.apply()
            editor.commit()
            startActivity(Intent(this, SignUpActivity::class.java))
            actions.updateStatus(MainActivity.userId, false)
            finish()
        } else {
            logoutFun()
            actions.loginUserDet()
            val updteUserList = actions.userData.value
            if (!updteUserList.isNullOrEmpty()) {
                MainActivity.userId = updteUserList[0].id
                MapHistory.userId = updteUserList[0].id
                ProfileActivity.userId = updteUserList[0].id
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putInt("USER_ID", updteUserList[0].id)
                editor.putString("USERNAME", updteUserList[0].name)
                editor.putString("USER_IMG", updteUserList[0].image.toString())
                editor.putString("USER_EMAIL", updteUserList[0].email)
                editor.apply()
                editor.commit()
                startActivity(Intent(this, SignUpActivity::class.java))
                actions.updateStatus(MainActivity.userId, false)
                finish()
            }
        }
    }

    private fun logoutFun() {
        actions.updateStatus(MainActivity.userId, false)
        actions.updateStatus(MapHistory.userId, false)
        actions.updateStatus(ProfileActivity.userId, false)
    }

    private fun startProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    private fun startMapHistory() {
        startActivity(Intent(this, MapHistory::class.java))
        finish()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }


    private fun showReviewDialog() {
        val dialogView = layoutInflater.inflate(R.layout.review, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        dialogView.findViewById<Button>(R.id.btn_rate_now)?.setOnClickListener {
            try {
                val uri = Uri.parse("market://details?id=com.example.wayvistaanandroidapplication")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                alertDialog.dismiss()
            } catch (e: ActivityNotFoundException) {
                val uri =
                    Uri.parse("http://play.google.com/store/apps/details?id=com.example.wayvistaanandroidapplication")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
                alertDialog.dismiss()
            }
        }

        dialogView.findViewById<Button>(R.id.btn_later)?.setOnClickListener {
            alertDialog.dismiss()
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}