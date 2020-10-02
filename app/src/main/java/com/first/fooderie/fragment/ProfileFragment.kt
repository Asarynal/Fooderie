package com.first.fooderie.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.first.fooderie.R


class ProfileFragment : Fragment() {
    lateinit var txtUserName: TextView
    lateinit var txtPhone: TextView
    lateinit var txtEmail: TextView
    lateinit var txtAddress: TextView
    lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        sharedPreferences = context!!.getSharedPreferences(
            getString(R.string.preference_file_name),
            Context.MODE_PRIVATE
        )
        txtUserName = view.findViewById(R.id.txtUserName)
        txtPhone = view.findViewById(R.id.txtPhone)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtAddress = view.findViewById(R.id.txtAddress)
        txtUserName.text = sharedPreferences.getString("name", "name")
        txtPhone.text = "+91-${sharedPreferences.getString("mobile_number", "mobile_number")}"
        txtEmail.text = sharedPreferences.getString("email", "email")
        txtAddress.text = sharedPreferences.getString("address", "address")



        return view


    }
}