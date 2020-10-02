package com.first.fooderie.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.first.fooderie.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
            val startAct = Intent(this@SplashActivity,
                LoginActivity::class.java)
            startActivity(startAct)
            finish()
        },2000)
    }
}