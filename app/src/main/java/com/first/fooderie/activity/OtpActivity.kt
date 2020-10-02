package com.first.fooderie.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import androidx.core.widget.NestedScrollView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.first.fooderie.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_otp.*
import org.json.JSONException
import org.json.JSONObject

class OtpActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar


    private lateinit var tiOtp: TextInputLayout
    private lateinit var tiEtOtp: TextInputEditText
    private lateinit var tiPassword: TextInputLayout
    private lateinit var tiEtPassword: TextInputEditText
    private lateinit var tiConfirmPassword: TextInputLayout
    private lateinit var tiEtConfirmPassword: TextInputEditText
    private lateinit var btnOtp: Button
    private var mobile: String? = null
    private var email: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
//        println("start")
        toolbar = findViewById(R.id.toolbar)

        tiOtp= findViewById(R.id.tiOtp)
        tiEtOtp= findViewById(R.id.tiEtOtp)
        tiPassword = findViewById(R.id.tiPassword)
        tiEtPassword = findViewById(R.id.tiEtPassword)
        tiConfirmPassword = findViewById(R.id.tiConfirmPassword)
        tiEtConfirmPassword = findViewById(R.id.tiEtConfirmPassword)
        btnOtp = findViewById(R.id.btnOtp)
        if(intent != null){
            mobile = intent.getStringExtra("mobile")
            email = intent.getStringExtra("email")
        }
        else{
            Snackbar.make(btnOtp,"unexpected error occurs",Snackbar.LENGTH_LONG).setAction("Back"){
                onNavigateUp()
            }
        }
        initUI()
    }

    private fun initUI() {
        initToolbar()
//        println("Toolbar initialized")
        initInputOtp()
//        println("Otp initialized")
        initInputPassword()
//        println("Password initialized")
        initInputConfirmPassword()
//        println("Confirm Password initialized")
        initInputAction()
    }

    private fun initInputAction() {
        btnOtp.setOnClickListener{
            fetchOtp()
        }
    }

    private fun fetchOtp() {
        updateStatusOtp()
        updateStatusPassword()
        updateStatusConfirmPassword()
        val otp = tiEtOtp.text.toString()
        val passWord = tiEtPassword.text.toString()
        val confirmPassword = tiEtConfirmPassword.text.toString()
        if(isValidOtp(otp) && isValidPassword(passWord) && isValidConfirmPassword(confirmPassword)){
            otpRequest(otp,passWord,confirmPassword)
        }
    }

    private fun otpRequest(otp: String, password: String, confirmPassword: String)
    {
        val queue = Volley.newRequestQueue(this@OtpActivity)
        val params = JSONObject()
        val url = "http://13.235.250.119/v2/reset_password/fetch_result"
        params.put("mobile_number", mobile)
        params.put("password", password)
        params.put("otp",otp)
        val request =
            object : JsonObjectRequest(Request.Method.POST, url, params, Response.Listener {
                try {
                    println(it)
                    val data = it.getJSONObject("data")
                    if (data.getBoolean("success")) {
                        redirectToLogin()
                    } else {
                        tiOtp.isErrorEnabled = true
                        tiOtp.error = "Invalid Credentials"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }, Response.ErrorListener {
                println(it)
                Snackbar.make(btnOtp, "unable to connect to server", Snackbar.LENGTH_LONG)
                    .setAction("Retry") {
                        fetchOtp()
                    }.show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "90e84348679949"
                    return headers
                }

            }
        queue.add(request)
    }

    private fun isValidOtp(otp: String?): Boolean {
        if (otp != null) {
            return otp.length == 4
        }
        return false
    }
    private fun initInputOtp() {
        tiEtOtp.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusOtp()
            }
        }
        tiOtp.setErrorIconOnClickListener {
            tiEtOtp.text = null
            tiEtOtp.requestFocus()
        }
        tiEtOtp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (tiOtp.isErrorEnabled) {
                    updateStatusOtp()
                }
            }

        })
    }

    private fun updateStatusOtp() {
        if (isValidOtp(tiEtOtp.text.toString())) {
            tiOtp.isErrorEnabled = false
        } else {
            tiOtp.error = "Error: otp has less than 4 characters"
            tiOtp.isErrorEnabled = true
        }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "OTP Verification"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onNavigateUp(): Boolean {
        println("Back Navigation tracked")
        finish()
        return true;
    }
    private fun redirectToLogin() {
        val intent = Intent(this@OtpActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onNavigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initInputConfirmPassword() {
        tiEtConfirmPassword.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusConfirmPassword()
            } else {
                updateStatusOtp()
                updateStatusPassword()
            }
        }
        tiConfirmPassword.setErrorIconOnClickListener {
            tiEtConfirmPassword.text = null
            tiEtConfirmPassword.requestFocus()
        }
        tiEtConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateStatusConfirmPassword()
            }

        })
    }

    private fun isValidConfirmPassword(password: String?): Boolean {
        if (isValidPassword(tiEtPassword.text.toString()) && (tiEtConfirmPassword.text.toString() == tiEtPassword.text.toString())) {
            return true
        }
        return false
    }

    private fun updateStatusConfirmPassword() {
        if (isValidConfirmPassword(tiEtConfirmPassword.text.toString())) {
            tiConfirmPassword.isErrorEnabled = false
        } else {
            tiConfirmPassword.error = "Error: invalid confirmation"
            tiConfirmPassword.isErrorEnabled = true
        }
    }

    private fun initInputPassword() {
        tiEtPassword.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusPassword()
            } else {
                updateStatusOtp()
            }
        }
        tiPassword.setErrorIconOnClickListener {
            tiEtPassword.text = null
            tiEtPassword.requestFocus()
        }
        tiEtPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (tiPassword.isErrorEnabled) {
                    updateStatusPassword()
                }
            }

        })
    }

    private fun isValidPassword(password: String?): Boolean {
        if (password != null && password.length >= 4) {
            return true
        }
        return false
    }

    private fun updateStatusPassword() {
        if (isValidPassword(tiEtPassword.text.toString())) {
            tiPassword.isErrorEnabled = false
        } else {
            tiPassword.error = "Error: too short password"
            tiPassword.isErrorEnabled = true
        }
    }
}