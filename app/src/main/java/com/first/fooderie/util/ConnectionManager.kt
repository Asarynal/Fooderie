package com.first.fooderie.util

import android.content.Context
import android.net.NetworkInfo
import android.net.ConnectivityManager

class ConnectionManager {

    fun isNetworkAvailable(context: Context) : Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting)
    }
}