package com.first.fooderie.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.first.fooderie.R
import com.first.fooderie.adapter.RestaurantAdapter
import com.first.fooderie.database.RestaurantDatabase
import com.first.fooderie.database.RestaurantEntity
import com.first.fooderie.model.Restaurant


class FavouriteFragment : Fragment() {


    private lateinit var rvRestaurant: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private var restaurantList = arrayListOf<Restaurant>()
    private lateinit var rlProgress: RelativeLayout
    private lateinit var rlFav: RelativeLayout
    private lateinit var rlNoFav: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourite, container, false)
        rlFav = view.findViewById(R.id.rlFavorites)
        rlNoFav = view.findViewById(R.id.rlNoFavorites)
        rlProgress = view.findViewById(R.id.rlProgress)
        rlProgress.visibility = View.VISIBLE
        setUpRecycler(view)
        return view
    }

    private fun setUpRecycler(view: View) {
        rvRestaurant = view.findViewById(R.id.rvRestaurants)


        /*In case of favourites, simply extract all the data from the DB and send to the adapter.
        * Here we can reuse the adapter created for the home fragment. This will save our time and optimize our app as well*/
        val backgroundList = FavouritesAsync(activity as Context).execute().get()
        if (backgroundList.isEmpty()) {
            rlProgress.visibility = View.GONE
            rlFav.visibility = View.GONE
            rlNoFav.visibility = View.VISIBLE
        } else {
            rlFav.visibility = View.VISIBLE
            rlProgress.visibility = View.GONE
            rlNoFav.visibility = View.GONE
            for (i in backgroundList) {
                restaurantList.add(
                    Restaurant(
                        i.id,
                        i.name,
                        i.rating,
                        i.cost.toInt(),
                        i.imageUrl
                    )
                )
            }

            restaurantAdapter = RestaurantAdapter(restaurantList, activity as Context,true)
            val mLayoutManager = LinearLayoutManager(activity)
            rvRestaurant.layoutManager = mLayoutManager
            rvRestaurant.itemAnimator = DefaultItemAnimator()
            rvRestaurant.adapter = restaurantAdapter
            rvRestaurant.setHasFixedSize(true)
        }

    }


    /*A new async class for fetching the data from the DB*/
    class FavouritesAsync(val context: Context) : AsyncTask<Void, Void, List<RestaurantEntity>>() {

        override fun doInBackground(vararg params: Void?): List<RestaurantEntity> {

            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()

            return db.restaurantDao().getAllRestaurants()
        }

    }

    
}