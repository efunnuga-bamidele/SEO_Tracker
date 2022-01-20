package com.bjtmtech.seotracker.ui

import LoadingDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bjtmtech.seotracker.R
import com.bjtmtech.seotracker.adapter.MyAdapterDashboard
import com.bjtmtech.seotracker.data.JobHistoryData
import com.bjtmtech.seotracker.data.ServiceEngineerData
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.dashboard_items.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.io.IOException
import java.lang.Exception
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class dashboardFragment : Fragment() {

    val db = Firebase.firestore
    private var dataSize: Int = 0
    private lateinit var recyclerViewData: RecyclerView
    private lateinit var engineerDataList: MutableList<ServiceEngineerData>
    private lateinit var myAdapterEngineer: MyAdapterDashboard

    var userCountry : String ?= null
    var userEmail : String ?= null

//
    private val calendar = Calendar.getInstance()
    var engineerEmailQuery: String ?= null
    var yearQuery: Int ?= calendar.get(Calendar.YEAR)

    private lateinit var sharedPref: SharedPreferences
    private var PRIVATE_MODE = 0
    val loading = LoadingDialog(this)
    val handle = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
//        val sharedPref: SharedPreferences =
//            requireContext().getSharedPreferences("myProfile", PRIVATE_MODE)
//        val engineerEmailQuery = sharedPref.getString("email", "defaultemail@mail.com").toString()



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref: SharedPreferences =
            requireContext().getSharedPreferences("myProfile", PRIVATE_MODE)
        userEmail = sharedPref.getString("email", "defaultemail@mail.com").toString()
        recyclerViewData = dbRecyclerView

        recyclerViewData.layoutManager = LinearLayoutManager(context)
        recyclerViewData.setHasFixedSize(true)

        engineerDataList = arrayListOf()

        myAdapterEngineer = MyAdapterDashboard(engineerDataList)

        recyclerViewData.adapter = myAdapterEngineer

        myAdapterEngineer.setOnItemClickListener(object : MyAdapterDashboard.onItemClickListener {
            override fun onItemClick(position: Int) {
//                Toast.makeText(context, "You clicked item " + position, Toast.LENGTH_SHORT).show()
            }

        })


        //Add Divider
        recyclerViewData.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        EventChangeListener()

        isOnline(requireContext())



    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    private fun EventChangeListener() {
        try {
            loading.startLoading()
            db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for(document in result){
                    if (document.data["email"] == userEmail){
                        userCountry = document.data["country"].toString()
                        FancyToast.makeText(
                            context, "List of engineers for $userCountry",
                            FancyToast.LENGTH_SHORT
                            ,FancyToast.INFO,
                        true
                        ).show()

                        db.collection("users")
                            .whereEqualTo("country", userCountry)
                            .whereEqualTo("level","User")
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?
                                ) {
                                    if (error != null) {
                                        Log.e("Firebase Error: ", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {

                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            engineerDataList.add(dc.document.toObject(ServiceEngineerData::class.java))

                                        }



                                    }

                                    myAdapterEngineer.notifyDataSetChanged()

//                        searchArrayList.addAll(jobsHistoryList)
                                }

                            })

                    }

                }
                handle.postDelayed({
                    loading.isDismiss()
                }, 1000)
            }

        } catch (e: IOException) {
            handle.postDelayed({
                loading.isDismiss()
            }, 1000)
            FancyToast.makeText(
                context,
                "Error while fetching data from database",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        }

    }


    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
//                    FancyToast.makeText(context, "NetworkCapabilities.TRANSPORT_CELLULAR!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()

                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        FancyToast.makeText(
            context,
            "Error checking internet connection!",
            FancyToast.LENGTH_SHORT,
            FancyToast.ERROR,
            true
        ).show()
        return false
    }

}