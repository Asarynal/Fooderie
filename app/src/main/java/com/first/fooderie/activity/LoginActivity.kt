package com.first.fooderie.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.first.fooderie.R
import com.first.fooderie.util.ConnectionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var tiMobile: TextInputLayout
    private lateinit var tiEtMobile: TextInputEditText
    private lateinit var tiPassword: TextInputLayout
    private lateinit var tiEtPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var txtForgotPassword: TextView
    private lateinit var txtSignupReq: TextView
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        if (isLoggedIn()) {
            redirect()
        }
        btnLogin = findViewById(R.id.btnLogin)
        tiMobile = findViewById(R.id.tiMobile)
        tiEtMobile = findViewById(R.id.tiEtMobile)
        tiPassword = findViewById(R.id.tiPassword)
        tiEtPassword = findViewById(R.id.tiEtPassword)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtSignupReq = findViewById(R.id.txtSignupReq)
        initUI()


    }

    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }



    private fun redirect() {
        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun redirectToRegister(){
        val intent = Intent(this@LoginActivity,
            RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun redirectToForgotPassword(){
        val intent = Intent(this@LoginActivity,
            ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun initUI() {
        initInputMobile()
        initInputPassword()
        initInputRegister()
        initInputForgotPassword()
        initInputLoginReq()
    }

    private fun initInputForgotPassword() {
        txtForgotPassword.setOnClickListener {
            redirectToForgotPassword()
        }
    }

    private fun initInputLoginReq() {
        btnLogin.setOnClickListener {
            updateStatusMobile()
            updateStatusPassword()
            val mobile = tiEtMobile.text.toString()
            val password = tiEtPassword.text.toString()
            if(isValidMobile(mobile) && isValidPassword(password)){
                if(ConnectionManager().isNetworkAvailable(this@LoginActivity)){

                login(mobile,password)
                }
                else{
                    alertNoInternetError()
                }
            }
        }
    }
    private fun alertNoInternetError(){
        MaterialAlertDialogBuilder(this@LoginActivity).setTitle("No Internet!")
            .setMessage("Looks like you are not connected to the Internet")
            .setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this@LoginActivity as Activity)
            }.setPositiveButton("Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }.show()
    }
    private fun login(mobile: String, password: String)
    {
        val queue = Volley.newRequestQueue(this@LoginActivity)
        val params = JSONObject()
        val url = "http://13.235.250.119/v2/login/fetch_result"
        params.put("mobile_number", mobile)
        params.put("password", password)
        val request =
            object : JsonObjectRequest(Request.Method.POST, url, params, Response.Listener {
                try {
                    println(it)
                    val data = it.getJSONObject("data")
                    if (data.getBoolean("success")) {
                        saveData(data.getJSONObject("data"))
                        redirect()
                    } else {
                         tiMobile.isErrorEnabled = true
                        tiMobile.error = "Invalid Credentials"
                        tiPassword.isErrorEnabled = true
                        tiPassword.error = "Invalid Credentials"

                    }
                } catch (e: JSONException) {
                    Snackbar.make(btnLogin, "unexpected error occurs", Snackbar.LENGTH_LONG)
                        .setAction("Retry") {
                            login(mobile,password)
                        }.show()
                }

            }, Response.ErrorListener {
                println(it)
                Snackbar.make(btnLogin, "unable to connect to server", Snackbar.LENGTH_LONG)
                    .setAction("Retry") {
                        login(mobile,password)
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

    private fun saveData(user: JSONObject) {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
        sharedPreferences.edit().putString("user_id", user.getString("user_id")).apply()
        sharedPreferences.edit().putString("name", user.getString("name")).apply()
        sharedPreferences.edit().putString("email", user.getString("email")).apply()
        sharedPreferences.edit().putString("mobile_number", user.getString("mobile_number")).apply()
        sharedPreferences.edit().putString("address", user.getString("address")).apply()
    }

    private fun isValidMobile(mobile: String?): Boolean {
        if (mobile != null) {
            return mobile.length == 10
        }
        return false
    }

    private fun isValidPassword(password: String?): Boolean {
        if(password!=null && password.length>=4){
            return true
        }
        return false
    }

    private fun updateStatusMobile() {
        if (isValidMobile(tiEtMobile.text.toString())) {
            tiMobile.isErrorEnabled = false
        } else {
            tiMobile.error = "Error: mobile number has less than 10 digits!"
            tiMobile.isErrorEnabled = true
        }
    }

    private fun updateStatusPassword() {
        if (isValidPassword(tiEtPassword.text.toString())) {
            tiPassword.isErrorEnabled = false
        } else {
            tiPassword.error = "Error: password has less than 4 characters!"
            tiPassword.isErrorEnabled = true
        }
    }

    private fun initInputMobile() {
        tiEtMobile.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusMobile()
            }
        }
        tiMobile.setErrorIconOnClickListener {
            tiEtMobile.text = null
            tiEtMobile.requestFocus()
        }
        tiEtMobile.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (tiMobile.isErrorEnabled) {
                    updateStatusMobile()
                }
            }

        })
    }
    private fun initInputPassword() {
        tiEtPassword.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusPassword()
            }
            else{
                updateStatusMobile()
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
    private fun initInputRegister(){
        txtSignupReq.setOnClickListener {
            redirectToRegister()
        }
    }
}