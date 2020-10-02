package com.first.fooderie.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.first.fooderie.R
import com.first.fooderie.activity.FoodActivity
import com.first.fooderie.database.RestaurantDatabase
import com.first.fooderie.database.RestaurantEntity
import com.first.fooderie.model.Restaurant
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

class RestaurantAdapter(private var restaurants: ArrayList<Restaurant>, val context: Context, val dataRemovable :Boolean) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RestaurantViewHolder {
        val itemView = LayoutInflater.from(p0.context)
            .inflate(R.layout.restaurant_card, p0, false)

        return RestaurantViewHolder(itemView)
    }
    override fun getItemCount(): Int {
        return restaurants.size
    }
    override fun getItemViewType(position: Int): Int {
        return position
    }
    override fun onBindViewHolder(p0: RestaurantViewHolder, p1: Int) {
        val restaurant = restaurants[p1]
        p0.imgThumbnail.clipToOutline = true
        p0.txtName.text = restaurant.name
        p0.txtRating.text = restaurant.rating
        val cost = "${restaurant.cost.toString()}/person"
        p0.txtCost.text = cost
        Picasso.get().load(restaurant.imageUrl).error(R.drawable.logo).into(p0.imgThumbnail)
        if(p0.sharedPreferences.getBoolean(restaurant.id.toString(),false)){
            p0.btnFav.setIconResource(R.drawable.ic_favourites_button_inactive)
        }
        else{
            p0.btnFav.setIconResource(R.drawable.ic_favourites_button_active)
        }

        p0.btnFav.setOnClickListener {
            val restaurantEntity = RestaurantEntity(
                restaurant.id,
                restaurant.name,
                restaurant.rating,
                restaurant.cost.toString(),
                restaurant.imageUrl
            )

            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val async =
                    DBAsyncTask(context, restaurantEntity, 2).execute()
                val result = async.get()
                if (result) {
                    p0.sharedPreferences.edit().putBoolean(restaurant.id.toString(),true).apply()
                    p0.btnFav.setIconResource(R.drawable.ic_favourites_button_inactive)
                }
            } else {
                val async = DBAsyncTask(context, restaurantEntity, 3).execute()
                val result = async.get()

                if (result) {
                    p0.sharedPreferences.edit().putBoolean(restaurant.id.toString(),false).apply()
                    p0.btnFav.setIconResource(R.drawable.ic_favourites_button_active)
                    if(dataRemovable){
                        restaurants.remove(restaurant)
                        this.notifyDataSetChanged()
                    }

                }
            }
        }

        p0.restaurantCard.setOnClickListener {
            val intent= Intent(context, FoodActivity::class.java)
            intent.putExtra("res_id",restaurant.id)
            intent.putExtra("res_name",restaurant.name)
            context.startActivity(intent)
        }

    }
    class DBAsyncTask(context: Context, private val restaurantEntity: RestaurantEntity, private val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            /*
            Mode 1 -> Check DB if the book is favourite or not
            Mode 2 -> Save the book into DB as favourite
            Mode 3 -> Remove the favourite book
            */

            when (mode) {

                1 -> {
                    val res: RestaurantEntity? =
                        db.restaurantDao().getRestaurantById(restaurantEntity.id.toString())
                    db.close()
                    return res != null
                }

                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }

                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }

            return false
        }

    }

    class RestaurantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgThumbnail = view.findViewById(R.id.imgThumbnail) as ImageView
        val txtName = view.findViewById(R.id.txtName) as TextView
        val txtRating = view.findViewById(R.id.txtRating) as TextView
        val txtCost = view.findViewById(R.id.txtCost) as TextView
        val restaurantCard = view.findViewById(R.id.restaurantCard) as MaterialCardView
        val btnFav = view.findViewById(R.id.btnFav) as MaterialButton
        val sharedPreferences : SharedPreferences = view.context.getSharedPreferences("Fooderie Preferences",Context.MODE_PRIVATE)
    }

}