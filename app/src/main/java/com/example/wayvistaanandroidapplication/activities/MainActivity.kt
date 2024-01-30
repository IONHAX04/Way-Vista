package com.example.wayvistaanandroidapplication.activities

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.wayvistaanandroidapplication.R
import com.example.wayvistaanandroidapplication.databinding.ActivityMainBinding
import com.example.wayvistaanandroidapplication.utilities.Actions
import com.example.wayvistaanandroidapplication.utilities.UserDetails
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.makeramen.roundedimageview.RoundedImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.animation.BounceInterpolator
import com.example.wayvistaanandroidapplication.utilities.MapLocation

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnMapReadyCallback {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actions: Actions
    private lateinit var userName: TextView
    private lateinit var userImg: RoundedImageView
    private var userDetails: List<UserDetails> = emptyList()

    private var mGoogleMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var currentLocation: LatLng? = null
    private val locationUpdateInterval = 5 * 60 * 1000L

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        var userId: Int = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)

        loadUserDetails()

        drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val toolbar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

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
            navigationView.setCheckedItem(R.id.home)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapsFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mGoogleMap?.isMyLocationEnabled = true

            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MainActivity.LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = LatLng(location.latitude, location.longitude)

                val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.pin)
                val desiredWidth = resources.dpToPx(35)
                val desiredHeight = resources.dpToPx(35)
                val resizedBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    desiredWidth,
                    desiredHeight,
                    false
                )
                val markerIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap)

                val marker = mGoogleMap?.addMarker(
                    com.google.android.gms.maps.model.MarkerOptions()
                        .position(currentLocation!!)
                        .icon(markerIcon)
                )

                val animator = ObjectAnimator.ofFloat(marker, "translationY", -50f)
                animator.apply {
                    duration = 1000
                    interpolator = BounceInterpolator()
                    repeatMode = ObjectAnimator.REVERSE
                    repeatCount = ObjectAnimator.INFINITE
                }
                animator.start()

                mGoogleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLocation!!,
                        15f
                    )
                )
                mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        }

        val handler = Handler()
        handler.postDelayed(object : Runnable {

            @SuppressLint("SimpleDateFormat")
            override fun run() {
                currentLocation?.let {
                    val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                    val address: List<Address> = geocoder.getFromLocation(it.latitude, it.longitude, 1) as List<Address>
                    val state = address[0].adminArea
                    val city = address[0].locality
                    val code = address[0].postalCode

                    val time = SimpleDateFormat("dd MM yyyy HH:mm aaa")
                    val timeUpd = time.format(Date())

//                    showToast("${state}, ${city}, ${code}")
                    actions.createLocation(
                        userId,
                        it.latitude,
                        it.longitude,
                        timeUpd,
                        city,
                        state,
                        code
                    ).let { actions.getLocation(userId) }
                }
                handler.postDelayed(this, locationUpdateInterval)
            }
        }, locationUpdateInterval)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun Resources.dpToPx(dp: Int): Int {
        return (dp * this.displayMetrics.density).toInt()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainActivity.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            }
        }
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
            actions.updateStatus(userId, false)
            finish()
        } else {
            logoutFun()
            actions.loginUserDet()
            val updteUserList = actions.userData.value
            if (!updteUserList.isNullOrEmpty()) {
                userId = updteUserList[0].id
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
                actions.updateStatus(userId, false)
                finish()
            }
        }
    }

    private fun logoutFun() {
        actions.updateStatus(userId, false)
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