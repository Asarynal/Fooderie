package com.first.fooderie.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.first.fooderie.R
import com.first.fooderie.model.ResMenu
import com.google.android.material.button.MaterialButton

class FoodAdapter (private var menuItems: ArrayList<ResMenu>, val context: Context, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {


    companion object {
        var isCartEmpty = true
    }

    class FoodViewHolder(view: View):RecyclerView.ViewHolder(view){
        val txtSrNumber: TextView =view.findViewById(R.id.txtSrNumber)
        val txtDishName:TextView=view.findViewById(R.id.txtDishName)
        val txtCost:TextView=view.findViewById(R.id.txtCost)
        val btnAdd:MaterialButton=view.findViewById(R.id.btnAdd)
        val btnRemove:MaterialButton=view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view= LayoutInflater.from(parent.context)
            .inflate(R.layout.food_item,parent,false)
        return FoodViewHolder(view)
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    interface OnItemClickListener{
        fun onAddItemClick(dishObject:ResMenu)
        fun onRemoveItemClick(dishObject:ResMenu)

    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food=menuItems[position]
        holder.txtSrNumber.text="${position+1}"
        holder.txtDishName.text=food.name
        holder.txtCost.text="${food.cost}"
        holder.btnAdd.setOnClickListener {
            holder.btnRemove.visibility = View.VISIBLE
            holder.btnAdd.visibility = View.GONE
            listener.onAddItemClick(food)
        }

        holder.btnRemove.setOnClickListener {
            holder.btnRemove.visibility = View.GONE
            holder.btnAdd.visibility = View.VISIBLE
            listener.onRemoveItemClick(food)
        }
    }

}