package com.first.fooderie.util

import android.view.View

class HeaderItem(val resName: String,val date: String): ListItem {
    override fun isHeader(): Boolean {
        return true
    }

}