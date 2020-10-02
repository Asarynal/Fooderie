package com.first.fooderie.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.first.fooderie.R
import com.first.fooderie.adapter.CartAdapter
import com.first.fooderie.adapter.FoodAdapter
import com.first.fooderie.adapter.FoodAdapter.Companion.isCartEmpty
import com.first.fooderie.adapter.RestaurantAdapter
import com.first.fooderie.database.OrderEntity
import com.first.fooderie.database.RestaurantDatabase
import com.first.fooderie.model.ResMenu
import com.first.fooderie.util.ConnectionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var coordinateLayout: CoordinatorLayout
    lateinit var toolbar: MaterialToolbar
    // private val orders=ArrayList<ResMenu>()
    private lateinit var rlProgress: RelativeLayout
    private lateinit var progressBar: ProgressBar
    lateinit var rlCart: RelativeLayout
    private lateinit var txtResName: TextView
    private lateinit var recyclerAdapter: CartAdapter
    private lateinit var frameLayout: FrameLayout
    lateinit var message:String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnOrder: Button
    lateinit var orders:List<ResMenu>
    var resId:Int=100000
    private var resName:String?="Cart"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        println("Start")
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(this@CartActivity)
        coordinateLayout = findViewById(R.id.coordinateLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frameLayout)
        txtResName = findViewById(R.id.txtResName)

        progressBar = findViewById(R.id.progressBar)
        rlCart = findViewById(R.id.rlCart)
        rlProgress = findViewById(R.id.rlProgress)
        rlProgress.visibility = View.GONE
        btnOrder=findViewById(R.id.btnOrder)
        btnOrder.visibility = View.VISIBLE

        setUpToolbar()

        if(intent!=null){
            resId=intent.getIntExtra("resId",100000)
            resName=intent.getStringExtra("resName")
            var str=intent.getStringExtra("order")
            orders=Gson().fromJson(str,Array<ResMenu>::class.java).asList()
            supportActionBar?.title=resName

        } else {
            finish()
            Toast.makeText(this@CartActivity,"Code 16:Some unexpected error occurred!", Toast.LENGTH_SHORT).show()
        }
        if(resId==100000){
            finish()
            Toast.makeText(this@CartActivity,"code 17:Some unexpected error occurred!", Toast.LENGTH_SHORT).show()
        }


        cartList()
        placeOrder()
    }



    class ClearDBAsync(context: Context, private val resId:Int): AsyncTask<Void, Void, Boolean>(){
        val db= Room.databaseBuilder(context, RestaurantDatabase::class.java,"res-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            db.orderDao().deleteOrders(resId)
            db.close()
            return true
        }
    }

    class GetItemsDBAsync(context:Context): AsyncTask<Void, Void, List<OrderEntity>>()
    {
        val db= Room.databaseBuilder( context, RestaurantDatabase::class.java,"restaurants-db").build()
        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            return db.orderDao().getAllOrders()
        }
    }


    private  fun cartList() {

        /* val list=GetItemsDBAsync(applicationContext).execute().get()
         for(element in list){

             orders.addAll(Gson().fromJson(element.foodItems,Array<ResMenu>::class.java).asList())
         } */
        if(orders.isEmpty())
        {
            rlCart.visibility=View.GONE
            rlProgress.visibility=View.VISIBLE
        }
        else{
            rlCart.visibility=View.VISIBLE
            rlProgress.visibility=View.GONE
        }

        recyclerAdapter= CartAdapter(orders,this@CartActivity)
        layoutManager = LinearLayoutManager(this@CartActivity)
        recyclerView.layoutManager=layoutManager
        recyclerView.itemAnimator= DefaultItemAnimator()
        recyclerView.adapter=recyclerAdapter
    }

    private fun placeOrder() {
        var sum = 0
        for (element in orders) {
            sum += element.cost.toInt()
        }
        val total = "Place Order(Total: Rs. $sum)"
        btnOrder.text = total
        btnOrder.setOnClickListener {
            rlProgress.visibility = View.VISIBLE
            sendRequest()
        }
    }

    private  fun sendRequest() {

        val queue = Volley.newRequestQueue(this@CartActivity)
        val url = "http://13.235.250.119/v2/place_order/fetch_result"
        val jsonParams = JSONObject()

        jsonParams.put("user_id", sharedPreferences.getString("user_id","user_id"))

        jsonParams.put("restaurant_id", resId.toString())
        var total = 0
        for (element in orders) {
            total += element.cost.toInt()
        }
        jsonParams.put("total_cost", total.toString())
        val dishArray = JSONArray()
        for (i in orders.indices) {
            val dishId = JSONObject()
            dishId.put("food_item_id", orders[i].id)
            dishArray.put(i, dishId)
        }
        jsonParams.put("food", dishArray)

        if (ConnectionManager().isNetworkAvailable(this@CartActivity)) {
            try {
                rlProgress.visibility = View.GONE

                val jsonObjectRequest =
                    object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {

                        val obj = it.getJSONObject("data")
                        val success = obj.getBoolean("success")
                        if (success) {
                            ClearDBAsync(applicationContext, resId).execute().get()
                            FoodAdapter.isCartEmpty = true
                            Toast.makeText(this@CartActivity,"Order Placed",Toast.LENGTH_SHORT).show()
                            val intent= Intent(this,OrderActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            rlCart.visibility = View.VISIBLE

                            message=obj.getString("errorMessage")
                            Toast.makeText(this@CartActivity, message, Toast.LENGTH_SHORT).show()


                        }
                    }, Response.ErrorListener {
                        rlCart.visibility = View.VISIBLE
                        Toast.makeText(
                            this@CartActivity,
                            "UNFORTUNATELY, Volley Error Occurred",
                            Toast.LENGTH_LONG
                        ).show()
                    }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-type"] = "application/json"
                            headers["token"] = "90e84348679949"
                            return headers
                        }
                    }
                queue.add(jsonObjectRequest)
            } catch (e: Exception) {
                rlCart.visibility = View.VISIBLE
                e.printStackTrace()
            }
        } else {

            alertNoInternetError()
        }
    }


    private  fun setUpToolbar() {

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        MaterialAlertDialogBuilder(this@CartActivity)
            .setTitle("Alert!")
            .setMessage("Going back will remove everything from cart")
            .setNegativeButton("No"){dialog,_->
                dialog.dismiss()
            }
            .setPositiveButton("Okay"){_,_->
                ClearDBAsync(applicationContext, resId).execute().get()
                FoodAdapter.isCartEmpty=true
                super.onBackPressed()
            }
            .show()


    }

    override fun onStop() {
        ClearDBAsync(applicationContext, resId).execute().get()
        FoodAdapter.isCartEmpty=true
        super.onStop()
    }

    private fun alertNoInternetError(){
        MaterialAlertDialogBuilder(this@CartActivity).setTitle("No Internet!")
            .setMessage("Looks like you are not connected to the Internet")
            .setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this@CartActivity as Activity)
            }.setPositiveButton("Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }.show()
    }
}