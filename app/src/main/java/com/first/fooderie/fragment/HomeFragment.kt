package com.first.fooderie.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.first.fooderie.R
import com.first.fooderie.adapter.RestaurantAdapter
import com.first.fooderie.model.Restaurant
import com.first.fooderie.util.ConnectionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap


class HomeFragment : Fragment() {
    private lateinit var rvRestaurants: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private var restaurantList = arrayListOf<Restaurant>()
    private lateinit var progressBar: ProgressBar
    private lateinit var rlProgress: RelativeLayout
    lateinit var message: String

    private var costComparator = Comparator<Restaurant> { res1, res2 ->
        if (res1.cost == res2.cost) {
            res1.name.compareTo(res2.name, true)
        } else {
            res1.cost.compareTo(res2.cost)
        }
    }

    private var ratingComparator = Comparator<Restaurant> { res1, res2 ->
        if (res1.rating.compareTo(res2.rating, true) == 0) {
            res1.name.compareTo(res2.name, true)
        } else {
            res1.rating.compareTo(res2.rating, true)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        progressBar = view?.findViewById(R.id.progressBar) as ProgressBar
        rlProgress = view.findViewById(R.id.rlProgress) as RelativeLayout
        rlProgress.visibility = View.VISIBLE
        rvRestaurants = view.findViewById(R.id.rvRestaurants)
        initUI()

        // Inflate the layout for this fragment
        return view
    }

    private fun initUI() {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result"
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                Response.Listener<JSONObject> { response ->
                    rlProgress.visibility = View.GONE

                    /*Once response is obtained, parse the JSON accordingly*/
                    try {
                        val data = response.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {

                            val resArray = data.getJSONArray("data")
                            for (i in 0 until resArray.length()) {
                                val resObject = resArray.getJSONObject(i)
                                val restaurant = Restaurant(
                                    resObject.getString("id").toInt(),
                                    resObject.getString("name"),
                                    resObject.getString("rating"),
                                    resObject.getString("cost_for_one").toInt(),
                                    resObject.getString("image_url")
                                )
                                restaurantList.add(restaurant)
                                if (activity != null) {
                                    restaurantAdapter =
                                        RestaurantAdapter(restaurantList, activity as Context,false)
                                    val mLayoutManager = LinearLayoutManager(activity)
                                    rvRestaurants.layoutManager = mLayoutManager
                                    rvRestaurants.itemAnimator = DefaultItemAnimator()
                                    rvRestaurants.adapter = restaurantAdapter
                                    rvRestaurants.setHasFixedSize(true)
                                }

                            }
                        } else {
                            message = data.getString("errorMessage")

                            Snackbar.make(rvRestaurants, message, Snackbar.LENGTH_LONG)
                                .setAction("Try Again") {
                                    initRecyclerView()
                                }.show()

                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener {
                    Snackbar.make(rvRestaurants, "unable to fetch data", Snackbar.LENGTH_LONG)
                        .setAction("Try Again") {
                            initRecyclerView()
                        }.show()
                }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "90e84348679949"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        } else {
            alertNoInternetError()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_options, menu)

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id= item.itemId
        var n=4
        if(id==R.id.action_sort){
            val listItem=arrayOf("Cost(Low to High)","Cost(High to Low)","Rating")
            MaterialAlertDialogBuilder(activity as Context)
                .setTitle("Sort By?")
                .setSingleChoiceItems(listItem,-1){_,i->
                    n=i
                }
                .setPositiveButton("Ok"){dialog,_->
                    when(n){
                        0 -> {
                            //Sorts the list according to cost(low to High)
                            Collections.sort(restaurantList, costComparator)
                            restaurantAdapter.notifyDataSetChanged()
                        }

                        1 ->{
                            //Sorts the list according to cost(High to Low)
                            Collections.sort(restaurantList,costComparator)
                            restaurantList.reverse()
                            restaurantAdapter.notifyDataSetChanged()
                        }
                        2 ->{
                            //Sorts the list according to rating(High to Low)
                            Collections.sort(restaurantList,ratingComparator)
                            restaurantList.reverse()
                            restaurantAdapter.notifyDataSetChanged()
                        }
                        else -> dialog.cancel()

                    }
                }
                .setNeutralButton("Cancel"){dialog,_->
                    dialog.cancel()
                }
                .show()

        }

        return super.onOptionsItemSelected(item)
    }
    private fun alertNoInternetError(){
        MaterialAlertDialogBuilder(activity as Context).setTitle("No Internet!")
            .setMessage("Looks like you are not connected to the Internet")
            .setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }.setPositiveButton("Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }.show()
    }

}