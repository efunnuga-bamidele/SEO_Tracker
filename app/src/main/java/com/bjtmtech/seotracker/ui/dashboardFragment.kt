package com.bjtmtech.seotracker.ui

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
import com.bjtmtech.seotracker.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat

class dashboardFragment : Fragment() {

    private var PRIVATE_MODE = 0
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("myProfile", PRIVATE_MODE)
        val engineerEmailQuery = sharedPref.getString("email" , "defaultemail@mail.com" ).toString()

        val sdf = SimpleDateFormat("MMMM dd, yyyy")
        val sdfy = SimpleDateFormat("yyyy")
        val sdfm = SimpleDateFormat("MM") //Number representations
        val timestamp = Timestamp(System.currentTimeMillis())
        val currentYear = sdfy.format(timestamp).toString()
        val currentDate = sdf.format(timestamp).toString()

//        Active Jobs
        try {
            db.collection("createdJobs")
                .whereEqualTo("engineerEmail", engineerEmailQuery.toString()).whereEqualTo("jobStatus", "ACTIVE")
                .whereEqualTo("createdYear", currentYear)

                .get()
                .addOnSuccessListener { documents ->

                    dbActiveCounter.setText(documents.size().toString())



                }
                .addOnFailureListener { exception ->
                    dbActiveCounter.setText("0")
//                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                }
        }catch (e:IOException){

        }
        activeJobLayout.setOnClickListener {
//            Toast.makeText(context, "Total Active Jobs : ${dbActiveCounter.text.toString()} \n Job Year :  $currentYear" +
//                    "\n Current date : $currentDate", Toast.LENGTH_LONG).show()
            FancyToast.makeText(context,"Total Active Jobs : ${dbActiveCounter.text.toString()} \n Job Year :  $currentYear" +
                    "\n Current date : $currentDate",FancyToast.LENGTH_LONG,FancyToast.DEFAULT,true).show()
        }

        //        Completed Jobs
        try {
            db.collection("createdJobs")
                .whereEqualTo("engineerEmail", engineerEmailQuery.toString()).whereEqualTo("jobStatus", "COMPLETED",)
                .whereEqualTo("createdYear", currentYear,)
                .get()
                .addOnSuccessListener { documents ->

                    dbCompletedCounter.setText(documents.size().toString())


                }
                .addOnFailureListener { exception ->
                    dbCompletedCounter.setText("0")
                }
        }catch (e:IOException){

        }


        completedJobsLayout.setOnClickListener {
//            Toast.makeText(context, "Total Completed Jobs : ${dbCompletedCounter.text.toString()} \n Job Year :  $currentYear" +
//                    "\n Current date : $currentDate", Toast.LENGTH_LONG).show()
            FancyToast.makeText(context,"Total Completed Jobs : ${dbCompletedCounter.text.toString()} \n Job Year :  $currentYear" +
                    "\n Current date : $currentDate",FancyToast.LENGTH_LONG,FancyToast.DEFAULT,true).show()
        }

        //        Canceled Jobs
        try {
            db.collection("createdJobs")
                .whereEqualTo("engineerEmail", engineerEmailQuery.toString()).whereEqualTo("jobStatus", "CANCELED",)
                .whereEqualTo("createdYear", currentYear,)

                .get()
                .addOnSuccessListener { documents ->

                    dbCanceledCounter.setText(documents.size().toString())



                }
                .addOnFailureListener { exception ->
                    dbCanceledCounter.setText("0")
                }
        }catch (e:IOException){

        }

        canceledJobsLayout.setOnClickListener {
//            Toast.makeText(context, "Total Canceled Jobs : ${dbCanceledCounter.text.toString()} \n Job Year :  $currentYear" +
//                    "\n Current date : $currentDate", Toast.LENGTH_LONG).show()
            FancyToast.makeText(context,"Total Canceled Jobs : ${dbCanceledCounter.text.toString()} \n Job Year :  $currentYear" +
                    "\n Current date : $currentDate",FancyToast.LENGTH_LONG,FancyToast.DEFAULT,true).show()
        }

        //        Pending Jobs
        try {
            db.collection("createdJobs")
                .whereEqualTo("engineerEmail", engineerEmailQuery.toString()).whereEqualTo("jobStatus", "PENDING",)
                .whereEqualTo("createdYear", currentYear,)

                .get()
                .addOnSuccessListener { documents ->

                    dbPendingCounter.setText(documents.size().toString())



                }
                .addOnFailureListener { exception ->
                    dbPendingCounter.setText("0")
                }
        }catch (e:IOException){

        }

        pendingJobsLayout.setOnClickListener {
//            Toast.makeText(context, "Total Pending Jobs : ${dbPendingCounter.text.toString()} \n Job Year :  $currentYear" +
//                    "\n Current date : $currentDate", Toast.LENGTH_LONG).show()
            FancyToast.makeText(context,"Total Pending Jobs : ${dbPendingCounter.text.toString()} \n Job Year :  $currentYear" +
                    "\n Current date : $currentDate",FancyToast.LENGTH_LONG,FancyToast.DEFAULT,true).show()
        }
//        Days Worked
        try {
            var duration : Int = 0
            db.collection("createdJobs")

                .whereEqualTo("engineerEmail", engineerEmailQuery.toString()).whereEqualTo("jobStatus", "COMPLETED",)
                .whereEqualTo("createdYear", currentYear,)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        duration += document.data["jobDuration"].toString().toInt()
                    }

                    dbWorkedDays.setText(duration.toString())




                }
                .addOnFailureListener { exception ->
                    dbWorkedDays.setText("0")
//                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                }
        }catch (e:IOException){

        }

        workDaysLayout.setOnClickListener {
//            Toast.makeText(context, "Total Worked Days : ${dbWorkedDays.text.toString()} \n Job Year :  $currentYear" +
//                    "\n Current date : $currentDate", Toast.LENGTH_LONG).show()
            FancyToast.makeText(context,"Total Worked Days : ${dbWorkedDays.text.toString()} \n Job Year :  $currentYear" +
                    "\n Current date : $currentDate",FancyToast.LENGTH_LONG,FancyToast.DEFAULT,true).show()
        }

        //        Site Visited
        try {
            var siteVisited : Int = 0
            db.collection("createdJobs")

                .whereEqualTo("engineerEmail", engineerEmailQuery.toString()).whereEqualTo("createdYear", currentYear,).whereEqualTo("jobStatus", "COMPLETED",)
                .get()
                .addOnSuccessListener { documents ->

                    dbCustomerVisited.setText(documents.size().toString())




                }
                .addOnFailureListener { exception ->
                    dbCustomerVisited.setText("0")
//                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                }
        }catch (e:IOException){

        }

        customerVisitLayout.setOnClickListener {
//            Toast.makeText(context, "Total Customer Visits : ${dbCustomerVisited.text.toString()} \n Job Year :  $currentYear" +
//                    "\n Current date : $currentDate", Toast.LENGTH_LONG).show()
            FancyToast.makeText(context,"Total Customer Visits : ${dbCustomerVisited.text.toString()} \n Job Year :  $currentYear" +
                    "\n Current date : $currentDate",FancyToast.LENGTH_LONG,FancyToast.DEFAULT,true).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        isOnline(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
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
        FancyToast.makeText(context, "Error checking internet connection!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
        return false
    }
}