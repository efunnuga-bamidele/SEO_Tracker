package com.bjtmtech.seotracker

import LoadingDialog
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bjtmtech.seotracker.adapter.MyJobHistoryAdapter
import com.bjtmtech.seotracker.data.JobHistoryData
import com.bjtmtech.seotracker.databinding.FragmentJobHistoryBinding
import com.bjtmtech.seotracker.ui.ViewJobsHistoryFragment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_job_history.*
import java.io.IOException
import java.text.SimpleDateFormat
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

    var alertDialogMTD: AlertDialog? = null
    var alertDialogYTD: AlertDialog? = null

    private val calendar = Calendar.getInstance()
    var currentYear:Int = calendar.get(Calendar.YEAR)
    var currentMonth:Int = calendar.get(Calendar.MONTH) + 1
    var currentDay:Int = calendar.get(Calendar.DAY_OF_MONTH)

    var customerVisitedMTD:Int ?= 0
    var jobCompletedMTD:Int ?= 0
    var openJobsMTD:Int ?= 0
    var canceledJobsMTD:Int ?= 0
    var pendingJobsMTD:Int ? =0
    var jobDurationMTD:Int ? =0
    var openJobDurationMTD:Int ? =0
    var dateRange:String ? = null
    var yearRange:String ? = null


    var customerVisitedYTD:Int ?= 0
    var jobCompletedYTD:Int ?= 0
    var openJobsYTD:Int ?= 0
    var canceledJobsYTD:Int ?= 0
    var pendingJobsYTD:Int ? =0
    var jobDurationYTD:Int ? =0
    var openJobDurationYTD:Int ? =0

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

        myAdapterHistory.setOnItemClickListener(object : MyJobHistoryAdapter.onItemClickListener {
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

        val arrayAdapterCountry = ArrayAdapter(
            requireContext(),
            R.layout.customer_name_dropdown_items, countryNamesList
        )
        engineerCountry.setAdapter(arrayAdapterCountry)


        isOnline(requireContext())


        generateReport.setOnClickListener {
            loading.isDismiss()

            if (isOnline(requireContext())) {
                queryName = engineerNameText.text.toString()
                loading.startLoading()
//If only name search is executed
                if (queryName!!.isNotEmpty() && startDateCheckBox.isChecked == false  && stopDateCheckBox.isChecked == false  && countryCheckBox.isChecked == false ) {

                    for (i in jobsHistoryList.indices) {

                        jobsHistoryList.removeAt(0)
                        searchArrayList.removeAt(0)
                        myAdapterHistory.notifyDataSetChanged()
                    }

                    getEngineerJobHistory()
//                        myAdapterHistory.notifyDataSetChanged()
                } else if(queryName!!.isNotEmpty() && startDateCheckBox.isChecked && stopDateCheckBox.isChecked == false && countryCheckBox.isChecked  == false) {
                    FancyToast.makeText(
                        context, "Error, Stop date not enabled",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR, true
                    )
                        .show()

                } else if(queryName!!.isNotEmpty() && startDateCheckBox.isChecked  == false && stopDateCheckBox.isChecked && countryCheckBox.isChecked  == false) {
                    FancyToast.makeText(
                        context, "Error, Start date not enabled",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR, true
                    )
                        .show()

                } else if(queryName!!.isNotEmpty() && startDateCheckBox.isChecked && stopDateCheckBox.isChecked && countryCheckBox.isChecked == false) {
                    if(startDateFilterText.text!!.isNotEmpty() && stopDateFilterText.text!!.isEmpty()){
                        FancyToast.makeText(
                            context, "Error, stopDateFilterText is empty",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.ERROR, true
                        )
                            .show()

                    }else if(startDateFilterText.text!!.isEmpty() && stopDateFilterText.text!!.isEmpty()){


                    }else if(startDateFilterText.text!!.isNotEmpty() && stopDateFilterText.text!!.isNotEmpty()){
                        getEngineerJobHistoryByRange()
                    }

                }
                else {

                    FancyToast.makeText(
                        context, "Error while fetching data from database",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR, true
                    )
                        .show()

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
//Text field date format
        fun EditText.transformIntoDatePicker(context: Context, format: String, maxDate: Date? = null) {
            isFocusableInTouchMode = false
            isClickable = true
            isFocusable = false

            val myCalendar = Calendar.getInstance()
            val datePickerOnDataSetListener =
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    myCalendar.set(Calendar.YEAR, year)
                    myCalendar.set(Calendar.MONTH, monthOfYear)
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat(format, Locale.UK)
                    setText(sdf.format(myCalendar.time))
                }

            setOnClickListener {
                DatePickerDialog(
                    context, datePickerOnDataSetListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)
                ).run {
                    maxDate?.time?.also { datePicker.maxDate = it }
                    show()
                }
            }
        }
//
        stopDateCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (stopDateCheckBox.isChecked){
                stopDateFilter.isEnabled = true
                stopDateFilterText.isEnabled = true
                stopDateFilter.isClickable = true
                stopDateFilterText.isClickable = true
                stopDateFilterText.transformIntoDatePicker(requireContext(), "MM.dd.yyyy", Date())

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
                startDateFilterText.transformIntoDatePicker(requireContext(), "MM.dd.yyyy", Date())
            }else{
                startDateFilterText.isEnabled = false
                startDateFilter.isEnabled = false
                startDateFilterText.isClickable = false
                startDateFilter.isClickable = false
            }
        })

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
//                showToast("Show user summary ytd report")

                createDialogYTD()
                alertDialogYTD?.show()
//                showDialog()
            }
            R.id.fab_menu_mtd_report -> {
//                showToast("Show user summary mtd report\"")
                createDialogMTD()
                alertDialogMTD?.show()
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


    fun purgeValues(){
        customerVisitedMTD= 0
        jobCompletedMTD= 0
        openJobsMTD= 0
        canceledJobsMTD= 0
        pendingJobsMTD =0
        jobDurationMTD =0
        openJobDurationMTD =0


        customerVisitedYTD= 0
        jobCompletedYTD= 0
        openJobsYTD= 0
        canceledJobsYTD= 0
        pendingJobsYTD =0
        jobDurationYTD=0
        openJobDurationYTD =0
    }

    
    private fun getEngineerJobHistory() {
        db.clearPersistence()
        val jobStatus = mutableListOf<String>("COMPLETED", "ACTIVE", "PENDING", "CANCELED")
        var currentMonthDate ="0"+(currentMonth).toString()
        purgeValues()

   try {

            db.collection("createdJobs")
                .whereEqualTo("engineerName", queryName)
                .whereIn("jobStatus",jobStatus)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }
                    searchArrayList.clear()
                    jobsHistoryList.clear()
                    for (dc in snapshots!!.documentChanges) {

                        //INFORMATION EXTRACTION WITHOUT FILTER FUll Year
                        if(dc.document.data["jobStatus"].toString() == "COMPLETED"){
                            jobCompletedYTD = jobCompletedYTD?.inc()
                            val durationYTD = dc.document.data["jobDuration"]
                            jobDurationYTD = jobDurationYTD?.plus(durationYTD.toString().toInt())

                        }else if(dc.document.data["jobStatus"].toString() == "ACTIVE"){
                            openJobsYTD = openJobsYTD?.inc()
                            val durationOpenYTD = dc.document.data["jobDuration"]
                            openJobDurationYTD = openJobDurationYTD?.plus(durationOpenYTD.toString().toInt())


                        }else if(dc.document.data["jobStatus"].toString() == "CANCELED"){
                            canceledJobsYTD = canceledJobsYTD?.inc()

                        }else if(dc.document.data["jobStatus"].toString() == "PENDING"){
                            pendingJobsYTD = pendingJobsYTD?.inc()

                        }
                        customerVisitedYTD = snapshots.size() //Get length of result

                        //Current Date Filter Session
                        val str = dc.document.data["startDate"].toString()
                        val delim = "."
                        val list = str.split(delim)
                        if(list[0] == currentMonthDate && list[2].toInt() > currentYear-1 && list[2].toInt() < currentYear+1){

                            Log.d(TAG, "Jobs: ${dc.document.data["startDate"]}")
                            jobsHistoryList.add(dc.document.toObject(JobHistoryData::class.java))
                            //Information Extraction with filter
                            customerVisitedMTD = customerVisitedMTD?.inc()
                            if(dc.document.data["jobStatus"].toString() == "COMPLETED"){
                                jobCompletedMTD = jobCompletedMTD?.inc()
                                val duration = dc.document.data["jobDuration"]
                                jobDurationMTD = jobDurationMTD?.plus(duration.toString().toInt())

                            }else if(dc.document.data["jobStatus"].toString() == "ACTIVE"){
                                openJobsMTD = openJobsMTD?.inc()
                                val durationOpen = dc.document.data["jobDuration"]
                                openJobDurationMTD = openJobDurationMTD?.plus(durationOpen.toString().toInt())

                            }else if(dc.document.data["jobStatus"].toString() == "CANCELED"){
                                canceledJobsMTD = canceledJobsMTD?.inc()

                            }else if(dc.document.data["jobStatus"].toString() == "PENDING"){
                                pendingJobsMTD = pendingJobsMTD?.inc()

                            }

                        }
                    }
                    myAdapterHistory.notifyDataSetChanged()
                    searchArrayList.addAll(jobsHistoryList)
                    handle.postDelayed({
                        loading.isDismiss()
                    }, 1000)
                }
       dateRange = currentMonthDate +" / "+currentYear
       yearRange = currentYear.toString()

        }catch (e: IOException){
            handle.postDelayed({
                loading.isDismiss()
            }, 1000)
            FancyToast.makeText(context, "Error while fetching data from database", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
        }
    }


    private fun getEngineerJobHistoryByRange() {
        db.clearPersistence()
        val jobStatus = mutableListOf<String>("COMPLETED", "ACTIVE", "PENDING", "CANCELED")
        var beginDateRange = startDateFilterText.text.toString()
        val beginDateRangeList = beginDateRange.split(".")
        var endDateRange = stopDateFilterText.text.toString()
        val endDateRangeList = endDateRange.split(".")
        purgeValues()

        try {

            db.collection("createdJobs")
                .whereEqualTo("engineerName", queryName)
                .whereIn("jobStatus",jobStatus)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }
                    searchArrayList.clear()
                    jobsHistoryList.clear()
                    for (dc in snapshots!!.documentChanges) {

                        //INFORMATION EXTRACTION WITHOUT FILTER FUll Year
                        if(dc.document.data["jobStatus"].toString() == "COMPLETED"){
                            jobCompletedYTD = jobCompletedYTD?.inc()
                            val durationYTD = dc.document.data["jobDuration"]
                            jobDurationYTD = jobDurationYTD?.plus(durationYTD.toString().toInt())

                        }else if(dc.document.data["jobStatus"].toString() == "ACTIVE"){
                            openJobsYTD = openJobsYTD?.inc()
                            val durationOpenYTD = dc.document.data["jobDuration"]
                            openJobDurationYTD = openJobDurationYTD?.plus(durationOpenYTD.toString().toInt())


                        }else if(dc.document.data["jobStatus"].toString() == "CANCELED"){
                            canceledJobsYTD = canceledJobsYTD?.inc()

                        }else if(dc.document.data["jobStatus"].toString() == "PENDING"){
                            pendingJobsYTD = pendingJobsYTD?.inc()

                        }
                        customerVisitedYTD = snapshots.size() //Get length of result
                        //-------------------------------------------------------------------//
                        val str = dc.document.data["startDate"].toString()
                        val delim = "."
                        val list = str.split(delim)
                        if(list[0] >= beginDateRangeList[0] && list[0] <= endDateRangeList[0]
                            && list[1] >= beginDateRangeList[1]
//                             list[1] <= endDateRangeList[1]
                            && list[2] >= beginDateRangeList[2] && list[2] <= endDateRangeList[2]){
                            Log.d(TAG, "Jobs: ${dc.document.data["startDate"]}")
                            jobsHistoryList.add(dc.document.toObject(JobHistoryData::class.java))
                            //Information Extraction with filter
                            customerVisitedMTD = customerVisitedMTD?.inc()
                            if(dc.document.data["jobStatus"].toString() == "COMPLETED"){
                                jobCompletedMTD = jobCompletedMTD?.inc()
                                val duration = dc.document.data["jobDuration"]
                                jobDurationMTD = jobDurationMTD?.plus(duration.toString().toInt())

                            }else if(dc.document.data["jobStatus"].toString() == "ACTIVE"){
                                openJobsMTD = openJobsMTD?.inc()
                                val durationOpen = dc.document.data["jobDuration"]
                                openJobDurationMTD = openJobDurationMTD?.plus(durationOpen.toString().toInt())

                            }else if(dc.document.data["jobStatus"].toString() == "CANCELED"){
                                canceledJobsMTD = canceledJobsMTD?.inc()

                            }else if(dc.document.data["jobStatus"].toString() == "PENDING"){
                                pendingJobsMTD = pendingJobsMTD?.inc()

                            }
                        }
                        dateRange = startDateFilterText.text.toString() +" - "+ stopDateFilterText.text.toString()
                        yearRange = beginDateRangeList[2].toString() +" - "+endDateRangeList[2].toString()
                    }
                    myAdapterHistory.notifyDataSetChanged()
                    searchArrayList.addAll(jobsHistoryList)
                    handle.postDelayed({
                        loading.isDismiss()
                    }, 1000)
                }


        }catch (e: IOException){
            handle.postDelayed({
                loading.isDismiss()
            }, 1000)
            FancyToast.makeText(context, "Error while fetching data from database", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
        }
    }

fun createReport(){

}

    fun createDialogMTD() {
//        showToast(""+queryName.toString())
        val alertDialogBuilderMTD = AlertDialog.Builder(context)
        alertDialogBuilderMTD.setTitle("SUMMARY REPORT")
        alertDialogBuilderMTD.setMessage(""+
                "\n" +
                "Month To Date Summary Reports\n"+
                "\n" +
                "~Engineers Name: "+queryName.toString()+"\n" +
                "~Report Date:  "+currentMonth+" - "+currentDay+" - "+currentYear+"\n" +
                "~Date Range: "+dateRange+"\n" +
                "\n" +
                "~Site Visited: \t "+customerVisitedMTD.toString()+" \n" +
                "~Total Worked Days: \t "+jobDurationMTD.toString()+" \n" +
                "~Total Open Jobs: \t "+openJobsMTD.toString()+" \n" +
                "~Total Open Job Days: \t "+openJobDurationMTD.toString()+" \n" +
                "~Total Completed Jobs: \t "+jobCompletedMTD.toString()+" \n" +
                "~Total Pending Jobs: \t "+pendingJobsMTD.toString()+" \n" +
                "~Total Canceled Jobs: \t "+canceledJobsMTD.toString()+" \n" +
                "\n" +
                ""
        )
        alertDialogBuilderMTD.setNegativeButton("Close", { dialogInterface: DialogInterface, i: Int ->
            }
        )

        alertDialogMTD = alertDialogBuilderMTD.create()
    }


    fun createDialogYTD() {
        val alertDialogBuilderYTD = AlertDialog.Builder(context)
        alertDialogBuilderYTD.setTitle("SUMMARY REPORT")
        alertDialogBuilderYTD.setMessage(""+
                "\n" +
                "Month To Date Summary Reports\n"+
                "\n" +
                "~Engineers Name: "+queryName.toString()+"\n" +
                "~Report Date:  "+currentMonth+" - "+currentDay+" - "+currentYear+"\n" +
                "~Date Range: "+yearRange+"\n" +
                "\n" +
                "~Site Visited: \t "+customerVisitedYTD.toString()+" \n" +
                "~Total Worked Days: \t "+jobDurationYTD.toString()+" \n" +
                "~Total Open Jobs: \t "+openJobsYTD.toString()+" \n" +
                "~Total Open Job Days: \t "+openJobDurationYTD.toString()+" \n" +
                "~Total Completed Jobs: \t "+jobCompletedYTD.toString()+" \n" +
                "~Total Pending Jobs: \t "+pendingJobsYTD.toString()+" \n" +
                "~Total Canceled Jobs: \t "+canceledJobsYTD.toString()+" \n" +
                "\n" +
                ""
        )
        alertDialogBuilderYTD.setNegativeButton("Close", { dialogInterface: DialogInterface, i: Int ->
//            Toast.makeText(context, "Action Canceled", Toast.LENGTH_SHORT).show()
        }
        )

        alertDialogYTD = alertDialogBuilderYTD.create()
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



}

