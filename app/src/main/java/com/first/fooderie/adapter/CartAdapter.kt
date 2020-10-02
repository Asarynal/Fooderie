package com.first.fooderie.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.first.fooderie.R
import com.first.fooderie.model.ResMenu

class CartAdapter (private val cartArray: List<ResMenu>, val context: Context)
    : RecyclerView.Adapter<CartAdapter.CartViewHolder>(){

    class CartViewHolder(view: View):RecyclerView.ViewHolder(view){
        val txtDishName: TextView =view.findViewById(R.id.txtDishName)
        val txtCost: TextView =view.findViewById(R.id.txtCost)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartAdapter.CartViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.cart_item,parent,false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cartArray.size
    }

    override fun onBindViewHolder(holder: CartAdapter.CartViewHolder, position: Int) {
        val cartObj=cartArray[position]
        holder.txtDishName.text=cartObj.name
        val price="${cartObj.cost}"
        holder.txtCost.text=price
    }
}