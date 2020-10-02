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
import android.view.MenuItem
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.first.fooderie.R
import com.first.fooderie.util.ConnectionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tiName: TextInputLayout
    private lateinit var tiEtName: TextInputEditText
    private lateinit var tiEmail: TextInputLayout
    private lateinit var tiEtEmail: TextInputEditText
    private lateinit var tiMobile: TextInputLayout
    private lateinit var tiEtMobile: TextInputEditText
    private lateinit var tiAddress: TextInputLayout
    private lateinit var tiEtAddress: TextInputEditText
    private lateinit var tiPassword: TextInputLayout
    private lateinit var tiEtPassword: TextInputEditText
    private lateinit var tiConfirmPassword: TextInputLayout
    private lateinit var tiEtConfirmPassword: TextInputEditText
    private lateinit var regForm: NestedScrollView
    private lateinit var btnRegister: Button
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        toolbar = findViewById(R.id.regToolbar)
        regForm = findViewById(R.id.regForm)
        tiName = regForm.findViewById(R.id.tiName)
        tiEtName = regForm.findViewById(R.id.tiEtName)
        tiEmail = regForm.findViewById(R.id.tiEmail)
        tiEtEmail = regForm.findViewById(R.id.tiEtEmail)
        tiMobile = regForm.findViewById(R.id.tiMobile)
        tiEtMobile = regForm.findViewById(R.id.tiEtMobile)
        tiAddress = regForm.findViewById(R.id.tiAddress)
        tiEtAddress = regForm.findViewById(R.id.tiEtAddress)
        tiPassword = regForm.findViewById(R.id.tiPassword)
        tiEtPassword = regForm.findViewById(R.id.tiEtPassword)
        tiConfirmPassword = regForm.findViewById(R.id.tiConfirmPassword)
        tiEtConfirmPassword = regForm.findViewById(R.id.tiEtConfirmPassword)
        btnRegister = regForm.findViewById(R.id.btnRegister)
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        initUI()
    }

    private fun initUI() {
        initToolbar()
        initInputName()
        initInputEmail()
        initInputMobile()
        initInputDelivery()
        initInputPassword()
        initInputConfirmPassword()
        initInputRegister()
    }

    private fun register() {
        updateStatusName()
        updateStatusEmail()
        updateStatusMobile()
        updateStatusDelivery()
        updateStatusPassword()
        updateStatusConfirmPassword()
        var name = tiEtName.text.toString()
        var email = tiEtEmail.text.toString()
        var phone = tiEtMobile.text.toString()
        var location = tiEtAddress.text.toString()
        var password = tiEtPassword.text.toString()
        var confirm = tiEtConfirmPassword.text.toString()
        if (isValidName(name) && isValidEmail(email) && isValidMobile(phone) && isValidDelivery(
                location
            ) && isValidPassword(password) && isValidConfirmPassword(confirm)
        ) {
            if(ConnectionManager().isNetworkAvailable(this@RegisterActivity)){

            signUp(name, email, phone, location, password)
            }
            else{
                alertNoInternetError()
            }
        }
    }
    private fun alertNoInternetError(){
        MaterialAlertDialogBuilder(this@RegisterActivity).setTitle("No Internet!")
            .setMessage("Looks like you are not connected to the Internet")
            .setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this@RegisterActivity as Activity)
            }.setPositiveButton("Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }.show()
    }
    private fun initInputRegister() {
        btnRegister.setOnClickListener {
            register()
        }
    }

    private fun redirect() {
        val intent = Intent(this@RegisterActivity, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveData(user: JSONObject) {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
        sharedPreferences.edit().putString("user_id", user.getString("user_id")).apply()
        sharedPreferences.edit().putString("name", user.getString("name")).apply()
        sharedPreferences.edit().putString("email", user.getString("email")).apply()
        sharedPreferences.edit().putString("mobile_number", user.getString("mobile_number")).apply()
        sharedPreferences.edit().putString("address", user.getString("address")).apply()
    }

    private fun signUp(
        name: String,
        email: String,
        phone: String,
        location: String,
        password: String
    ) {
        val queue = Volley.newRequestQueue(this@RegisterActivity)
        val params = JSONObject()
        val url = "http://13.235.250.119/v2/register/fetch_result"
        params.put("name", name)
        params.put("mobile_number", phone)
        params.put("password", password)
        params.put("address", location)
        params.put("email", email)
        val request =
            object : JsonObjectRequest(Request.Method.POST, url, params, Response.Listener {
                try {
                    println(it)
                    val data = it.getJSONObject("data")
                    if (data.getBoolean("success")) {
                        saveData(data.getJSONObject("data"))
                        redirect()
                    } else {

                        if (data.getString("errorMessage") == "Mobile number OR Email Id is already registered") {
                            Snackbar.make(
                                btnRegister,
                                "User already exists!! Try login",
                                Snackbar.LENGTH_LONG
                            ).setAction("LogIn") {
                                onNavigateUp()
                            }.show()
                        } else {
                            Snackbar.make(btnRegister, "Request fails", Snackbar.LENGTH_LONG)
                                .setAction("Retry") {
                                    register()
                                }.show()
                        }

                    }
                } catch (e: JSONException) {
                    Snackbar.make(btnRegister, "unexpected error ouccurs", Snackbar.LENGTH_LONG)
                        .setAction("Retry") {
                            register()
                        }.show()
                }

            }, Response.ErrorListener {
                println(it)
                Snackbar.make(btnRegister, "unable to connect to server", Snackbar.LENGTH_LONG)
                    .setAction("Retry") {
                        register()
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


    private fun initInputConfirmPassword() {
        tiEtConfirmPassword.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusConfirmPassword()
            } else {
                updateStatusName()
                updateStatusEmail()
                updateStatusMobile()
                updateStatusDelivery()
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
                updateStatusName()
                updateStatusEmail()
                updateStatusMobile()
                updateStatusDelivery()
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

    private fun initInputDelivery() {
        tiEtAddress.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusDelivery()
            } else {
                updateStatusName()
                updateStatusEmail()
                updateStatusMobile()
            }
        }
        tiAddress.setErrorIconOnClickListener {
            tiEtAddress.text = null
            tiEtAddress.requestFocus()
        }
        tiEtAddress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (tiAddress.isErrorEnabled) {
                    updateStatusDelivery()
                }
            }

        })
    }

    private fun isValidDelivery(address: String?): Boolean {
        return (address != null && address != "")
    }

    private fun updateStatusDelivery() {
        if (isValidDelivery(tiEtAddress.text.toString())) {
            tiAddress.isErrorEnabled = false
        } else {
            tiAddress.error = "Error: address field is empty"
            tiAddress.isErrorEnabled = true
        }
    }

    private fun initInputEmail() {
        tiEtEmail.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusEmail()
            } else {
                updateStatusName()
            }
        }
        tiEmail.setErrorIconOnClickListener {
            tiEtEmail.text = null
            tiEtEmail.requestFocus()
        }
        tiEtEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (tiEmail.isErrorEnabled) {
                    updateStatusEmail()
                }
            }

        })
    }

    private fun isValidEmail(email: String?): Boolean {
        if (email != null && email != "") {
            return true
        }
        return false
    }

    private fun updateStatusEmail() {
        if (isValidEmail(tiEtEmail.text.toString())) {
            tiEmail.isErrorEnabled = false
        } else {
            tiEmail.error = "Error: email field is empty!!"
            tiEmail.isErrorEnabled = true
        }
    }

    private fun initInputMobile() {
        tiEtMobile.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusMobile()
            } else {
                updateStatusName()
                updateStatusEmail()
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

    private fun isValidMobile(mobile: String?): Boolean {
        if (mobile != null) {
            return mobile.length == 10
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

    private fun initInputName() {
        tiEtName.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusName()
            }
        }
        tiName.setErrorIconOnClickListener {
            tiEtName.text = null
            tiEtName.requestFocus()
        }
        tiEtName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (tiName.isErrorEnabled) {
                    updateStatusName()
                }
            }

        })
    }

    private fun isValidName(name: String?): Boolean {
        if (name != null && name.length >= 3) {
            return true
        }
        return false
    }

    private fun updateStatusName() {
        if (isValidName(tiEtName.text.toString())) {
            tiName.isErrorEnabled = false
        } else {
            tiName.error = "Error: name has less than 3 characters"
            tiName.isErrorEnabled = true
        }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Sign Up"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onNavigateUp(): Boolean {
        println("Back Navigation tracked")
        finish()
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onNavigateUp()
        }
        return super.onOptionsItemSelected(item)
    }
}