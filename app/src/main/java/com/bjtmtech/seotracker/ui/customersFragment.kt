package com.bjtmtech.seotracker

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bjtmtech.seotracker.adapter.MyAdapterCustomerName
import com.bjtmtech.seotracker.data.CustomerName
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_customers.*
import java.io.IOException


class customersFragment : Fragment() {

    private var EditUID : Int? = null

    val db = Firebase.firestore
    private var dataSize: Int = 0
    private lateinit var recyclerView : RecyclerView
    private lateinit var  customersArrayList : MutableList<CustomerName>
    private lateinit var  myAdapter : MyAdapterCustomerName
    var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        releaseCreateButton()
        

        recyclerView = cvRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        customersArrayList = mutableListOf()

        myAdapter = MyAdapterCustomerName(customersArrayList)
        recyclerView.adapter  = myAdapter


        myAdapter.setOnItemClickListener(object : MyAdapterCustomerName.onItemClickListener{
            override fun onItemClick(position: Int) {
//                FancyToast.makeText(context,customersArrayList[position].name.toString(), FancyToast.INFO, FancyToast.LENGTH_SHORT, true).show()
                EditUID = customersArrayList[position].UID
                cvCustomerNameEditText.setText(customersArrayList[position].name.toString())
                cvCustomerNameEditText.isFocusable = true
                cvCreateCustomer.isEnabled = false
                cvCreateCustomer.isClickable = false
                cvCreateCustomer.isFocusable = false
            }
        })

        //Add Divider
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        EventChangeListener()


        cvCreateCustomer.setOnClickListener {
            if(isOnline(context!!)){
                try{
                db.collection("customerNames")
                    .get()
                    .addOnSuccessListener { result ->
                        dataSize = result.size()
                        val customerNameData = cvCustomerNameEditText.text.toString().capitalize().trim()
                        val cvID = dataSize  + 1

                        if(customerNameData.isNotEmpty() && cvID != null){
//                  FancyToast.makeText(context, "Fields Are filled $customerNameData  : $cvID", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show()
                            val nameData = hashMapOf(
                                "UID" to cvID,
                                "name" to customerNameData
                            )
                            db.collection("customerNames").document(nameData["UID"].toString())
                                .set(nameData)
                                .addOnSuccessListener { documentRefrence ->
                                    FancyToast.makeText(context, "Customer profile created successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
//                                    customersArrayList.clear()
                                    cvCustomerNameEditText.text!!.clear()
                                    EventChangeListener()
                                }
                                .addOnFailureListener { e ->
                                    FancyToast.makeText(context, "Error creating customer profile!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
                                }

                        }

                        else{
                            FancyToast.makeText(context, "Fields are empty, Please try again!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()

                        }
                    }
            }catch (e: IOException){
                Log.e("ERROR", "Exception : $e");

                }
            }
        }

        cvUpdateButton.setOnClickListener {
            if (isOnline(context!!)){
                    try {
                        if(cvCustomerNameEditText.text.toString().isNotEmpty()) {
                            db.collection("customerNames").document(EditUID.toString())
                                .update("name", cvCustomerNameEditText.text.toString())
                                .addOnSuccessListener {
                                    FancyToast.makeText(context, "Customer profile successfully updated!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
//                                    myAdapter.notifyDataSetChanged()

                                    cvCustomerNameEditText.text!!.clear()
                                    releaseCreateButton()
//                                    customersArrayList.clear()
                                    EventChangeListener()
                                }
                                .addOnFailureListener {
                                    FancyToast.makeText(context, "Error updating customer profile!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()

                                }
                        }else{
                            FancyToast.makeText(context, "Field is empty, Please try again!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()
                        }
                    }catch (e: IOException){
                        Log.e("ERROR", "Exception : $e");
                    }

                }
        }

        createDialog()
        cvDeleteButton.setOnClickListener {
            alertDialog?.show()
        }

        cvClearButton.setOnClickListener {
            customersArrayList.clear()
            EventChangeListener()
            cvCustomerNameEditText.text!!.clear()
            releaseCreateButton()
        }

    }

    private fun releaseCreateButton() {
        cvCreateCustomer.isEnabled = true
        cvCreateCustomer.isClickable = true
        cvCreateCustomer.isFocusable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customers, container, false)
    }

    private fun EventChangeListener() {
        try {
            db.collection("customerNames").orderBy("UID", Query.Direction.ASCENDING)
                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                    override fun onEvent(
                        value: QuerySnapshot?,
                        error: FirebaseFirestoreException?
                    ) {
                        if (error != null) {
                            Log.e("Firestore", error.message.toString())
                            return
                        }
//
                        for (i in customersArrayList.indices) {
                            customersArrayList.removeAt(0)
                        }


                        for (dc: DocumentChange in value?.documentChanges!!) {
                            if (dc.type == DocumentChange.Type.ADDED) {
                                customersArrayList.add(dc.document.toObject(CustomerName::class.java))
                            }
                        }
                        myAdapter.notifyDataSetChanged()

                    }

                })
        }catch (e : IOException){
            FancyToast.makeText(context, "Error while fetching data from database", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
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


    fun createDialog() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Delete Customer Profile")
        alertDialogBuilder.setMessage("Are you sure you want to delete record?")
        alertDialogBuilder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            deleteDocument()
        }
        alertDialogBuilder.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int ->
//            Toast.makeText(context, "Action Canceled", Toast.LENGTH_SHORT).show()
        })

        alertDialog = alertDialogBuilder.create()
    }

    private fun deleteDocument() {
        if (isOnline(context!!)) {
            try {
                db.collection("customerNames").document(EditUID.toString())
                    .delete()
                    .addOnSuccessListener {
                        FancyToast.makeText(context, "Customer profile successfully deleted!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()

                        cvCustomerNameEditText.text!!.clear()
                        releaseCreateButton()
//                        customersArrayList.clear()
                        EventChangeListener()

                    }
                    .addOnFailureListener {
                        FancyToast.makeText(context, "Error deleting customer profile!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()

                    }

            }catch (e: IOException){
                Log.e("ERROR", "Exception : $e");
            }
        }
    }


}