package com.bjtmtech.seotracker

import android.content.ContentValues.TAG
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bjtmtech.seotracker.adapter.MyAdapterJobType
import com.bjtmtech.seotracker.data.JobTypes
import com.bjtmtech.seotracker.ui.MainActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_jobtype.*
import java.io.IOException
import kotlin.collections.ArrayList


class jobtypeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var EditUID : Int? = null

    val db = Firebase.firestore
    private var dataSize: Int = 0
    private lateinit var recyclerView : RecyclerView
    private lateinit var  jobsArrayList : ArrayList<JobTypes>
    private lateinit var  myAdapter : MyAdapterJobType
    private lateinit var  database: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()

        var navView: NavigationView = (activity as MainActivity).findViewById(R.id.nav_view)

//        database = FirebaseFirestore.getInstance()
        recyclerView = jtvRecyclerView

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)


        jobsArrayList = arrayListOf()

        myAdapter = MyAdapterJobType(jobsArrayList)

        recyclerView.adapter = myAdapter


        myAdapter.setOnItemClickListener(object: MyAdapterJobType.onItemClickListener{
            override fun onItemClick(position: Int) {
                EditUID = jobsArrayList[position].UID
                jtJobType.setText(jobsArrayList[position].name.toString())

            }

        } )

        //Add Divider
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            ))

        EventChangeListener()

        isOnline(context!!)
        //Call the action to create the table data
        jtCreateButton.setOnClickListener {
    if (isOnline(context!!)){
        try{
            db.collection("jobTypes")
                .get()
                .addOnSuccessListener { result ->
                    dataSize = result.size()

                    // Get te datas from the text input
                    val jobType = jtJobType.text.toString().capitalize().trim()
                    val jtID = dataSize + 1

                    // Create a job hashmap
                    val job = hashMapOf(
                        "UID" to jtID,
                        "name" to jobType
                    )

                    db.collection("jobTypes").document(job["UID"].toString())
                        .set(job)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(
                                context,
                                "Job type successfully written!",
                                Toast.LENGTH_SHORT
                            ).show()
                            jobsArrayList.clear()
                            EventChangeListener()
                            jtJobType.text!!.clear()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error adding job type!", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }catch (e : IOException){
            FancyToast.makeText(context, "Error while fetching data from database", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
        }
        }

//
        }



        jtUpdateButton.setOnClickListener {
            if(isOnline(context!!)) {
                try {
                    db.collection("jobTypes").document(EditUID.toString())
                        .update("name", jtJobType.text.toString().capitalize().trim())
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Job type successfully updated!",
                                Toast.LENGTH_SHORT
                            ).show()
                            jobsArrayList.clear()
                            EventChangeListener()
                            jtJobType.text!!.clear()

                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Error updating job type!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } catch (e: IOException) {
                    FancyToast.makeText(
                        context,
                        "Error while fetching data from database",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,
                        true
                    ).show()
                }
            }
        }

        jtDeleteButton.setOnClickListener {
            if (isOnline(context!!)) {
                try {
                    db.collection("jobTypes").document(EditUID.toString())
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Job type successfully deleted!",
                                Toast.LENGTH_SHORT
                            ).show()
                            jobsArrayList.clear()
                            EventChangeListener()
                            jtJobType.text!!.clear()

                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Error deleting job type!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } catch (e: IOException) {
                    FancyToast.makeText(
                        context,
                        "Error while fetching data from database",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,
                        true
                    ).show()
                }
            }
        }

        jtClearButton.setOnClickListener {
            (activity as MainActivity).replaceFragment(jobtypeFragment(), "Job Type")
            navView.setCheckedItem(R.id.nav_jobType)
        }
    }


    private fun EventChangeListener() {
        try{
        db.collection("jobTypes").
        addSnapshotListener(object : EventListener<QuerySnapshot>{
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null){
                    Log.e("Firestore", error.message.toString())
                    return
                }

                for (i in jobsArrayList.indices) {
                    jobsArrayList.removeAt(0)
                }

                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        jobsArrayList.add(dc.document.toObject(JobTypes::class.java))
                    }
                }

                myAdapter.notifyDataSetChanged()
            }

        })
        } catch (e: IOException) {
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
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
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


    private fun setup() {

        // [END get_firestore_instance]

        // [START set_firestore_settings]
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings
        // [END set_firestore_settings]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_jobtype, container, false)
    }

}