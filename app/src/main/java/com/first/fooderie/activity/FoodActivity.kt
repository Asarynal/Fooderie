package com.first.fooderie.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.first.fooderie.R
import com.first.fooderie.adapter.FoodAdapter
import com.first.fooderie.database.OrderEntity
import com.first.fooderie.database.RestaurantDatabase
import com.first.fooderie.model.ResMenu
import com.first.fooderie.util.ConnectionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject

class FoodActivity : AppCompatActivity() {
    private lateinit var rvMenu: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: FoodAdapter
    private lateinit var progressBar: ProgressBar
    lateinit var rlProgress: RelativeLayout
    private lateinit var toolbar: MaterialToolbar
    var resid:Int=100000
    private lateinit var btnCart: MaterialButton
    lateinit var rlMenu : RelativeLayout
    lateinit var rlButton: RelativeLayout
    private var resname:String?="Menu"
    val menuList= arrayListOf<ResMenu>()
    val orderList= arrayListOf<ResMenu>()
    lateinit var message:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food)
        println("Start")
        rvMenu=findViewById(R.id.rvMenu)
        layoutManager= LinearLayoutManager(this@FoodActivity)
        btnCart=findViewById(R.id.btnCart)
        progressBar = findViewById(R.id.progressBar)
        rlProgress = findViewById(R.id.rlProgress)
        rlMenu = findViewById(R.id.rlMenu)
        rlButton = findViewById(R.id.rlButton)
        toolbar=findViewById(R.id.toolbar)
        if(intent!=null){
            resid=intent.getIntExtra("res_id",100000)
            resname=intent.getStringExtra("res_name")

        }
        else{
            finish()
            Snackbar.make(rvMenu, "unexpected error occurs", Snackbar.LENGTH_LONG)
                .setAction("Back") {
                    onBackPressed()
                }.show()
        }
        if(resid==100000){
            finish()
            Snackbar.make(rvMenu, "unexpected error occurs", Snackbar.LENGTH_LONG)
                .setAction("Back") {
                    onBackPressed()
                }.show()
        }
        initUI()
    }

    private fun initUI() {

        rlProgress.visibility = View.VISIBLE
        initToolbar()
        initRecyclerView()
        btnCart.setOnClickListener{
            println(orderList)
            proceedToCart()

        }
    }

    private fun proceedToCart() {
        val gson= Gson()
        println(orderList)
        val foodItems=gson.toJson(orderList)
//        val async = CartItems(this@FoodActivity, resid , foodItems, 1).execute()
//        val result = async.get()
        if (true) {
            println(foodItems)
            val intent=Intent(this@FoodActivity,CartActivity::class.java)
            intent.putExtra("resId", resid )
            intent.putExtra("resName", resname)
            intent.putExtra("order",foodItems)
            startActivity(intent)

        } else {
            Snackbar.make(rvMenu, "unexpected error occurs", Snackbar.LENGTH_LONG)
                .setAction("Try again") {
                    proceedToCart()
                }.show()
        }
    }
    class CartItems(context: Context, private val resid:Int, private val foodItems:String, val mode:Int):
        AsyncTask<Void, Void, Boolean>(){
        val db= Room.databaseBuilder(context, RestaurantDatabase::class.java,"res-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {
                1 -> {
                    db.orderDao().insertOrder(OrderEntity(resid, foodItems))
                    db.close()
                    return true
                }

                2 -> {
                    db.orderDao().deleteOrder(OrderEntity(resid, foodItems))
                    db.close()
                    return true
                }
            }
            return false
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onBackPressed() {
        if(orderList.size > 0) {
            MaterialAlertDialogBuilder(this@FoodActivity).setTitle("Back Alert!")
                .setMessage("Going back will remove everything from cart")
                .setNegativeButton("No") { _, _ ->

                }.setPositiveButton("Okay") { _, _ ->
                    finish()
                    super.onBackPressed()
                }.show()

        }else{
            finish()
            super.onBackPressed()
        }

    }
    private fun initRecyclerView() {
        val queue= Volley.newRequestQueue(this@FoodActivity)
        val jsonParams= JSONObject()
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/${resid}"
        if(ConnectionManager().isNetworkAvailable(this@FoodActivity)) {
            val jsonRequest = object : JsonObjectRequest(
                Method.GET,url,
                jsonParams, Response.Listener {
                    rlProgress.visibility = View.GONE
                    try{
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        println(data)
                        if (success){
                            orderList.clear()
                            //Receiving data from the server
                            val resArray = data.getJSONArray("data")
                            for (i in 0 until resArray.length()) {
                                val resObject = resArray.getJSONObject(i)
                                val menuItems = ResMenu(
                                    resObject.getString("id"),
                                    resObject.getString("name"),
                                    resObject.getString("cost_for_one"),
                                    resObject.getString("restaurant_id")
                                )
                                menuList.add(menuItems)
                            }
                                recyclerAdapter =
                                    FoodAdapter(menuList, this@FoodActivity, object:
                                        FoodAdapter.OnItemClickListener{
                                        override fun onAddItemClick(dishObject: ResMenu) {
                                            orderList.add(dishObject)
                                            if(orderList.size>0){
                                                proceedButtonShow()
                                                FoodAdapter.isCartEmpty=false
                                            }
                                        }

                                        override fun onRemoveItemClick(dishObject: ResMenu) {
                                            orderList.remove(dishObject)
                                            if(orderList.isEmpty()){
                                                proceedButtonHide()
                                                FoodAdapter.isCartEmpty=true
                                            }
                                        }
                                    }

                                    )


                                rvMenu.adapter = recyclerAdapter
                                rvMenu.layoutManager = layoutManager
                                rvMenu.itemAnimator = DefaultItemAnimator()






                        }else{
                            message=data.getString("errorMessage")
                            Snackbar.make(rvMenu, message, Snackbar.LENGTH_LONG)
                                .setAction("Back") {
                                    onBackPressed()
                                }.show()

                        }

                    }catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener {
                    Snackbar.make(rvMenu, "unexpected error occurs", Snackbar.LENGTH_LONG)
                        .setAction("Back") {
                            onBackPressed()
                        }.show()
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "90e84348679949"
                    return headers
                }
            }
            queue.add(jsonRequest)
        }else{
            alertNoInternetError()
        }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=resname
    }
    private fun alertNoInternetError(){
        MaterialAlertDialogBuilder(this@FoodActivity).setTitle("No Internet!")
            .setMessage("Looks like you are not connected to the Internet")
            .setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this@FoodActivity as Activity)
            }.setPositiveButton("Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }.show()
    }
    override fun onPause() {
        super.onPause()
        finish()
    }
    private fun proceedButtonShow(){
        val param1 : LinearLayout.LayoutParams = rlMenu.layoutParams as LinearLayout.LayoutParams
        val param2 : LinearLayout.LayoutParams = rlButton.layoutParams as LinearLayout.LayoutParams
        param1.weight = 9.3f
        param2.weight = 0.7f
        rlMenu.layoutParams = param1
        rlButton.layoutParams = param2
    }
    private fun proceedButtonHide(){
        val param1 : LinearLayout.LayoutParams = rlMenu.layoutParams as LinearLayout.LayoutParams
        val param2 : LinearLayout.LayoutParams = rlButton.layoutParams as LinearLayout.LayoutParams
        param1.weight = 10f
        param2.weight = 0f
        rlMenu.layoutParams = param1
        rlButton.layoutParams = param2
    }
}