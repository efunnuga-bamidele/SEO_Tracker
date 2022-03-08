package com.bjtmtech.seotracker

import LoadingDialog
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bjtmtech.seotracker.adapter.MyJobHistoryAdapter
import com.bjtmtech.seotracker.data.JobHistoryData
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_job_history.*
import java.io.IOException
import kotlin.collections.ArrayList
import android.view.MenuInflater
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.compose.ui.text.capitalize
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.bjtmtech.seotracker.adapter.MyAdapterCustomerName
import com.bjtmtech.seotracker.data.ServiceEngineerData
import com.bjtmtech.seotracker.databinding.FragmentJobHistoryBinding
import com.bjtmtech.seotracker.ui.ViewJobsHistoryFragment
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

//import android.widget.SearchView


class jobHistoryFragment : Fragment(), View.OnClickListener {

    private var EditUID : Int? = null
    val db = Firebase.firestore
    private var dataSize: Int = 0
    private lateinit var recyclerViewHistory : RecyclerView
    private lateinit var  jobsHistoryList : ArrayList<JobHistoryData>
    lateinit var  heading : Array<String>
    private lateinit var  myAdapterHistory : MyJobHistoryAdapter

    private lateinit var sharedPref : SharedPreferences
    private var PRIVATE_MODE = 0
    private lateinit var searchArrayList: ArrayList<JobHistoryData>

    var userCountry : String ?= null
    var userEmail : String ?= null

    var queryName : String ?= null
    var queryCountry : String ?= null

    val loading = LoadingDialog(this)
    val handle = Handler()
//    lateinit var menuInflator : MenuInflater
    private lateinit var binding: FragmentJobHistoryBinding

    var alertDialog: AlertDialog? = null

    private val calendar = Calendar.getInstance()
    var currentYear:Int = calendar.get(Calendar.YEAR)
    var currentMonth:Int = calendar.get(Calendar.MONTH) + 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentJobHistoryBinding.bind(view)
//        return binding.root

        setupFabButtons()

        //        set sharedpreference to get email of user
        sharedPref = requireContext().getSharedPreferences("myProfile", PRIVATE_MODE)
//        create variable to collect the email address
        userEmail = sharedPref.getString("email", "defaultemail@mail.com").toString()
        loading.startLoading()
//        setup()
//
        recyclerViewHistory = jhvRecyclerView

        recyclerViewHistory.layoutManager = LinearLayoutManager(context)
        recyclerViewHistory.setHasFixedSize(true)

        jobsHistoryList = arrayListOf()
        searchArrayList = arrayListOf()
//        Toast.makeText(context, jobsHistoryList.toString(), Toast.LENGTH_SHORT).show()

//        myAdapterHistory = MyJobHistoryAdapter(jobsHistoryList)
        myAdapterHistory = MyJobHistoryAdapter(searchArrayList)


        recyclerViewHistory.adapter = myAdapterHistory

        myAdapterHistory.setOnItemClickListener(object : MyJobHistoryAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {

            }
            })

        getEngineerNames()

        //Add Divider
        recyclerViewHistory.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        handle.postDelayed({
            loading.isDismiss()
        }, 1000)
        //        Code to getting country list

        val countryNamesList = java.util.ArrayList<String>()
        val countriesList: MutableList<String> = java.util.ArrayList()
        val locales = Locale.getISOCountries()
        for (countryCode in locales) {
            val obj = Locale("", countryCode)
            countriesList.add(obj.getDisplayCountry(Locale.ENGLISH))
            Collections.sort(countriesList)
        }
        for (s in countriesList) {
            countryNamesList.add(s)
        }

        val arrayAdapterCountry = ArrayAdapter(requireContext(),
            R.layout.customer_name_dropdown_items, countryNamesList)
        engineerCountry.setAdapter(arrayAdapterCountry)


        isOnline(requireContext())


        generateReport.setOnClickListener {
            loading.isDismiss()

            if(isOnline(requireContext())){
                queryName = engineerNameText.text.toString()
                loading.startLoading()

                for (i in jobsHistoryList.indices) {

                    jobsHistoryList.removeAt(0)
                    searchArrayList.removeAt(0)
                }
                myAdapterHistory.notifyDataSetChanged()
                    if (queryName!!.isNotEmpty()){
                        getEngineerJobHistory()
                    }
            }
        }
//
        countryCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (countryCheckBox.isChecked){
                engineerCountry.isEnabled = true
                textInputLayout3.isEnabled = true
                engineerCountry.isClickable = true
                textInputLayout3.isClickable = true
            }else{
                engineerCountry.isEnabled = false
                textInputLayout3.isEnabled = false
                engineerCountry.isClickable = false
                textInputLayout3.isClickable = false
            }
        })
//
        stopDateCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (stopDateCheckBox.isChecked){
                stopDateFilter.isEnabled = true
                stopDateFilterText.isEnabled = true
                stopDateFilter.isClickable = true
                stopDateFilterText.isClickable = true
            }else{
                stopDateFilter.isEnabled = false
                stopDateFilterText.isEnabled = false
                stopDateFilter.isClickable = false
                stopDateFilterText.isClickable = false
            }
        })
//
        startDateCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (startDateCheckBox.isChecked){
                startDateFilterText.isEnabled = true
                startDateFilter.isEnabled = true
                startDateFilterText.isClickable = true
                startDateFilter.isClickable = true
            }else{
                startDateFilterText.isEnabled = false
                startDateFilter.isEnabled = false
                startDateFilterText.isClickable = false
                startDateFilter.isClickable = false
            }
        })
        createDialog()
    }

    private fun setupFabButtons() {
        binding.fabMenuActions.shrink()
        binding.fabMenuActions.setOnClickListener(this)
        binding.fabMenuYtdReport.setOnClickListener(this)
        binding.fabMenuMtdReport.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fab_menu_actions -> {
                expandOrCollapseFAB()
            }
            R.id.fab_menu_ytd_report -> {
                showToast("Show user summary ytd report")
                alertDialog?.show()
//                showDialog()
            }
            R.id.fab_menu_mtd_report -> {
                showToast("Show user summary mtd report\"")
                alertDialog?.show()
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun expandOrCollapseFAB() {
        if (binding.fabMenuActions.isExtended) {
            binding.fabMenuActions.shrink()
            binding.fabMenuYtdReport.hide()
            binding.fabMenuYtdReportText.visibility = View.GONE
            binding.fabMenuMtdReport.hide()
            binding.fabMenuMtdReportText.visibility = View.GONE
        } else {
            binding.fabMenuActions.extend()
            binding.fabMenuYtdReport.show()
            binding.fabMenuYtdReportText.visibility = View.VISIBLE
            binding.fabMenuMtdReport.show()
            binding.fabMenuMtdReportText.visibility = View.VISIBLE
        }
    }


    private fun getEngineerJobHistory() {
        try {
//            FancyToast.makeText(context, "Month: "+currentMonth+" Year:"+currentYear, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
            db.collection("createdJobs").orderBy("createdDate", Query.Direction.DESCENDING)
                .whereEqualTo("engineerName", queryName)
//                .whereEqualTo("createdYear", currentYear.toString())
//                .whereEqualTo("createdMonth", "1")
//                .whereGreaterThanOrEqualTo("startDate", "02.28.2022")
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
                                jobsHistoryList.add(dc.document.toObject(JobHistoryData::class.java))
                                Log.d("Firebase Data ", jobsHistoryList.toString())

                            }

                        }
                        myAdapterHistory.notifyDataSetChanged()
                        searchArrayList.addAll(jobsHistoryList)
                        handle.postDelayed({
                            loading.isDismiss()
                        }, 1000)
                    }

                })
        }catch (e: IOException){
            handle.postDelayed({
                loading.isDismiss()
            }, 1000)
            FancyToast.makeText(context, "Error while fetching data from database", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
        }
    }


    private fun getEngineerNames() {

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.data["email"] == userEmail) {
                        userCountry = document.data["country"].toString()
                        FancyToast.makeText(
                            context, "List of engineers for $userCountry",
                            FancyToast.LENGTH_SHORT, FancyToast.INFO,
                            true
                        ).show()
                        engineerCountry.setText(userCountry.toString())
                        db.collection("users")

                            .whereEqualTo("country", userCountry)
                            .whereEqualTo("level", "User")
                            .get()
                            .addOnSuccessListener { result ->
                                val engineerNamesList = java.util.ArrayList<String>()
                                engineerNamesList.add("")
                                for (document in result) {
                                    engineerNamesList.add(document.data["firstName"].toString()+ " " + document.data["lastName"].toString())
                                }
                                val arrayAdapter = ArrayAdapter(requireContext(), R.layout.customer_name_dropdown_items, engineerNamesList)
                                engineerNameText.setAdapter(arrayAdapter)
                            }
                            .addOnFailureListener { exception ->
                                Log.d(TAG, "Error getting documents: ", exception)
                            }

                    }
                }
            }
    }

//    Swipe listener event
//    private var simpleCallback = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)){
    private var simpleCallback = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
           var position = viewHolder.adapterPosition
            var currentHistory = myAdapterHistory.getItemId(position)
            when(direction){
                ItemTouchHelper.RIGHT -> {
                    val args = Bundle()
                    args.putString("key", position.toString())
                    args.putString("action", "SwipeRight")
                    val fm: FragmentManager = activity!!.supportFragmentManager
                    val overlay = ViewJobsHistoryFragment()
                    overlay.setArguments(args)
                    overlay.show(fm, "FragmentDialog")
                }

            }
            myAdapterHistory.notifyDataSetChanged()
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




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_job_history, container, false)
    }

//
    override fun onDestroy() {
        super.onDestroy()
    }

    fun createDialog() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("SUMMARY REPORT")
        alertDialogBuilder.setMessage(buildSpannedString {
            bold { "" +
                "-------------------------------------------------\n" +
                "-----------Yet To Date Summary Reports-----------\n" +
                "-------------------------------------------------\n" +
                "~Engineers Name:  Service Engineer\n" +
                "~Report Date:  00 - 00 - 0000\n" +
                "~Date Range: January to November\n" +
                "-------------------------------------------------\n" +
                "~Site Visited: \t20 \n" +
                "~Total Worked Days: \t120 \n" +
                "~Total Open Jobs: \t1 \n" +
                "~Total Completed Jobs: \t14 \n" +
                "~Total Pending Jobs: \t3 \n" +
                "~Total Canceled Jobs: \t2 \n" +
                "-------------------------------------------------\n"+
                    ""
                }
//        alertDialogBuilder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
//            try {
//
//            }catch (e:Exception){
//
//            }
//        }
        alertDialogBuilder.setNegativeButton("Close", { dialogInterface: DialogInterface, i: Int ->
//            Toast.makeText(context, "Action Canceled", Toast.LENGTH_SHORT).show()
        })

        alertDialog = alertDialogBuilder.create()
    })


}
}