package com.first.fooderie.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.first.fooderie.R
import com.first.fooderie.adapter.OrderHistoryAdapter
import com.first.fooderie.util.BodyItem
import com.first.fooderie.util.HeaderItem
import com.first.fooderie.util.ListItem
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class OrderFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var rlOrder: RelativeLayout
    private lateinit var rlNoOrder: RelativeLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressLayout: RelativeLayout
    lateinit var message:String
    private var userId=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_order, container, false)
        rlOrder = view.findViewById(R.id.rlOrder)
        rlNoOrder = view.findViewById(R.id.rlNoOrder)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressLayout= view?.findViewById(R.id.rlProgress) as RelativeLayout
        progressLayout.visibility = View.VISIBLE
        sharedPreferences = context!!.getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", "user_id").toString()
        sendRequest(userId)
        return view
    }

    private fun sendRequest(userId: String) {
        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/orders/fetch_result/${userId}"
        val jsonObjectRequest = object :
            JsonObjectRequest(Method.GET, url, null, Response.Listener {
                try {
                    progressLayout.visibility = View.GONE
                    println("data received from internet $it")
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val resArray = data.getJSONArray("data")
                        println("Successful Request. Restaurant Array = $resArray")
                        if (resArray.length() == 0) {
                            rlOrder.visibility = View.GONE
                            rlNoOrder.visibility = View.VISIBLE
                        } else {
                            val mItems = arrayListOf<ListItem>()
//                            val content = TreeMap<HeaderItem,List<BodyItem>>()
                            for (i in 0 until resArray.length()) {
                                val orderObject = resArray.getJSONObject(i)
                                val resName = orderObject.getString("restaurant_name")
                                val date: String =orderObject.getString("order_placed_at")
                                val header = HeaderItem(resName, date)
                                val foodItems = orderObject.getJSONArray("food_items")
                                val body = arrayListOf<BodyItem>()

                                mItems.add(header)
                                for (i in 0 until foodItems.length()) {

                                    val bodyItem = BodyItem(
                                        foodItems.getJSONObject(i).getString("name"),
                                        foodItems.getJSONObject(i).getString("cost")
                                    )
                                    mItems.add(bodyItem)
                                }

                            }

                                if (mItems.isEmpty()) {
                                    rlOrder.visibility = View.GONE
                                    rlNoOrder.visibility = View.VISIBLE
                                } else {

                                    rlOrder.visibility = View.VISIBLE
                                    rlNoOrder.visibility = View.GONE
                                    if (activity != null) {
                                        val orderHistoryAdapter = OrderHistoryAdapter(
                                            mItems,
                                            activity as Context
                                        )
                                        val layoutManager =
                                            LinearLayoutManager(activity as Context)
                                        recyclerView.layoutManager = layoutManager
                                        recyclerView.itemAnimator = DefaultItemAnimator()
                                        recyclerView.adapter = orderHistoryAdapter
                                    } else {
                                        queue.cancelAll(this::class.java.simpleName)
                                    }
                                }

                        }
                    }else{
                        message=data.getString("errorMessage")

                        Snackbar.make(recyclerView,message,Snackbar.LENGTH_LONG).setAction("Try Again"){
                            sendRequest(userId)
                        }.show()

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
                Response.ErrorListener {
                    Snackbar.make(recyclerView,"Failed to fetch data",Snackbar.LENGTH_LONG).setAction("Try Again"){
                        sendRequest(userId)
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
    }


}