package com.first.fooderie.util

class BodyItem(
    val foodName: String,
    val cost: String
): ListItem {
    override fun isHeader(): Boolean {
        return false
    }
}