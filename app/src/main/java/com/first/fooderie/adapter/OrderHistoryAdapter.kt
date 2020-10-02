package com.first.fooderie.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.first.fooderie.R
import com.first.fooderie.util.BodyItem
import com.first.fooderie.util.HeaderItem
import com.first.fooderie.util.ListItem

class OrderHistoryAdapter(val mItems: ArrayList<ListItem>,
                          val context: Context
                          ) : RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> (){

    abstract class OrderHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view){

    }
    class HeaderViewHolder(view: View) : OrderHistoryAdapter.OrderHistoryViewHolder(view){
        val txtHeaderName : TextView = view.findViewById(R.id.txtHeaderName)
        val txtHeaderDate : TextView = view.findViewById(R.id.txtHeaderDate)
    }
    class BodyViewHolder(view: View) : OrderHistoryAdapter.OrderHistoryViewHolder(view){
        val txtDishName : TextView = view.findViewById(R.id.txtDishName)
        val txtCost : TextView = view.findViewById(R.id.txtCost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        if(viewType == 0){
            val view  = LayoutInflater.from(parent.context).inflate(R.layout.header_history,parent,false)
            return HeaderViewHolder(view)
        }
        else{
            val view  = LayoutInflater.from(parent.context).inflate(R.layout.body_history,parent,false)
            return BodyViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(mItems[position].isHeader()){
            return 0
        }
        else{
            return 1
        }
    }
    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val type = getItemViewType(position)
        if(type==0){
            val header = mItems[position] as HeaderItem
            val headerHolder = holder as HeaderViewHolder
            headerHolder.txtHeaderDate.text = header.date
            headerHolder.txtHeaderName.text = header.resName
        }
        else{
            val body = mItems[position] as BodyItem
            val bodyHolder = holder as BodyViewHolder
            bodyHolder.txtDishName.text = body.foodName
            bodyHolder.txtCost.text = body.cost
        }
    }

}