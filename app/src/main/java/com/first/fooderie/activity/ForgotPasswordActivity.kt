package com.first.fooderie.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.first.fooderie.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_forgot_password.*
import org.json.JSONException
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tiEmail: TextInputLayout
    private lateinit var tiEtEmail: TextInputEditText
    private lateinit var tiMobile: TextInputLayout
    private lateinit var tiEtMobile: TextInputEditText
    private lateinit var btnFrogotPassword: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        toolbar = findViewById(R.id.toolbar)
        tiEmail = findViewById(R.id.tiEmail)
        tiEtEmail = findViewById(R.id.tiEtEmail)
        tiMobile = findViewById(R.id.tiMobile)
        tiEtMobile = findViewById(R.id.tiEtMobile)
        btnFrogotPassword = findViewById(R.id.btnForgotPassword)
        initUI()
    }

    private fun initUI() {
        initToolbar()
        initInputMobile()
        initInputEmail()
        initInputAction()
    }

    private fun initInputAction() {
        btnFrogotPassword.setOnClickListener {
            updateStatusMobile()
            updateStatusEmail()
            val email = tiEtEmail.text.toString()
            val mobile = tiEtMobile.text.toString()
            if (isValidEmail(email) && isValidMobile(mobile)) {
                forgotPasswordRequest(email, mobile)
            }
        }
    }

    private fun forgotPasswordRequest(email: String, mobile: String) {
        val queue = Volley.newRequestQueue(this@ForgotPasswordActivity)
        val params = JSONObject()
        val url = "http://13.235.250.119/v2/forgot_password/fetch_result"
        params.put("mobile_number", mobile)
        params.put("email", email)
        val request =
            object : JsonObjectRequest(Request.Method.POST, url, params, Response.Listener {
                try {
                    println(it)
                    val data = it.getJSONObject("data")
                    if (data.getBoolean("success")) {
                        val firstTry = data.getBoolean("first_try")
                        //Receiving data from the server
                        if (firstTry) {
                            Snackbar.make(
                                btnFrogotPassword,
                                "The OTP has been sent to your registered email ID",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        else{
                            Snackbar.make(
                                btnFrogotPassword,
                                "Please refer to the OTP sent before",
                                Snackbar.LENGTH_SHORT
                            ).show()

                        }

                        redirectToOtp(mobile,email)
                    } else {
                        tiMobile.isErrorEnabled = true
                        tiMobile.error = "Invalid Mobile Number"
                        tiEmail.isErrorEnabled = true
                        tiEmail.error = "Invalid Email"

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }, Response.ErrorListener {
                println(it)
                Snackbar.make(btnForgotPassword, "unable to connect to server", Snackbar.LENGTH_LONG)
                    .setAction("Retry") {
                        forgotPasswordRequest(email, mobile)
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

    private fun redirectToOtp(mobile: String, email: String) {
        val intent = Intent(this@ForgotPasswordActivity, OtpActivity::class.java)
        intent.putExtra("mobile", mobile)
        intent.putExtra("email", email)
        startActivity(intent)
    }

    private fun initInputEmail() {
        tiEtEmail.setOnFocusChangeListener { _, b ->
            if (!b) {
                updateStatusEmail()
            } else {
                updateStatusMobile()
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

    private fun updateStatusEmail() {
        if (isValidEmail(tiEtEmail.text.toString())) {
            tiEmail.isErrorEnabled = false
        } else {
            tiEmail.error = "Error: email field is empty!!"
            tiEmail.isErrorEnabled = true
        }
    }

    private fun isValidEmail(email: String?): Boolean {
        if (email != null && email != "") {
            return true
        }
        return false
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

    private fun updateStatusMobile() {
        if (isValidMobile(tiEtMobile.text.toString())) {
            tiMobile.isErrorEnabled = false
        } else {
            tiMobile.error = "Error: mobile number has less than 10 digits!"
            tiMobile.isErrorEnabled = true
        }
    }

    private fun isValidMobile(mobile: String?): Boolean {
        if (mobile != null) {
            return mobile.length == 10
        }
        return false
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Forgot Password"
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