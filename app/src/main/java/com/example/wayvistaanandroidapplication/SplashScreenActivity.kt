package com.example.wayvistaanandroidapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.example.wayvistaanandroidapplication.activities.SignInActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val backgroudnImg: ImageView = findViewById(R.id.logo)
        val sideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide)
        backgroudnImg.startAnimation(sideAnimation)

        Handler().postDelayed({
            var intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.layout_transition_enter, R.anim.layout_transition_exit)
            finish()
        }, 2500)
    }
}