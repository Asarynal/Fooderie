package com.first.fooderie.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.first.fooderie.R
import com.google.android.material.button.MaterialButton

class OrderActivity : AppCompatActivity() {

    private lateinit var btnOrder : MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        btnOrder = findViewById(R.id.btnOrder)

        btnOrder.setOnClickListener {
            startActivity(Intent(this@OrderActivity , DashboardActivity::class.java))
            finish()
        }

    }

    override fun onBackPressed() {
        //Do Nothing Press ok
    }
}