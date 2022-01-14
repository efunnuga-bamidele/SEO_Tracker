package com.bjtmtech.seotracker.ui


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bjtmtech.seotracker.R
import com.bjtmtech.seotracker.jobHistoryFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
//import kotlinx.android.synthetic.main.fragment_active_job.*
import kotlinx.android.synthetic.main.fragment_view_jobs_history.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

class ViewJobsHistoryFragment() : DialogFragment() {

    var db = Firebase.firestore

    private val calendar = Calendar.getInstance()

    var startDate:String? = null
    var stopDate:String? = null

    var startYear:Int = calendar.get(Calendar.YEAR)
    var startMonth:Int = calendar.get(Calendar.MONTH)
    var startDay:Int = calendar.get(Calendar.DAY_OF_MONTH)
    var createdYear:Int ?= 0
    var createdMonth:Int ?= 0

    var stopYear:Int = calendar.get(Calendar.YEAR)
    var stopMonth:Int = calendar.get(Calendar.MONTH)
    var stopDay:Int = calendar.get(Calendar.DAY_OF_MONTH)
    var dataRef: String ?= null
    var alertDialog: AlertDialog? = null

    lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog_MinWidth)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth)
    }


//    private fun setStyle(styleNoTitle: Int, themeDevicedefaultDialogMinwidth: Int) {
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_jobs_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        navView = (activity as MainActivity).findViewById(R.id.nav_view)

        val tableAction = requireArguments().getString("action")

        if(tableAction == "Clicked"){
            val tabelIndex = requireArguments().getString("key")
            vjhCompleteJob.visibility = View.GONE
            vjhOpenJob.visibility = View.GONE
            vjhPendJob.visibility = View.GONE
            vjhCancelJob.visibility = View.GONE
            vjhUpdateJob.visibility = View.GONE
            vjhDeleteJob.visibility = View.GONE
            vjhtextLogoActive.setText("View Mode ")

            db.collection("createdJobs").orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    if (tabelIndex != null) {
                        vjhCustomerName.setText(result.documents[tabelIndex.toInt()]["customerName"].toString())
                        vjhCustomerCountry.setText(result.documents[tabelIndex.toInt()]["customerCountry"].toString())
                        vjhCustomerState.setText(result.documents[tabelIndex.toInt()]["customerState"].toString())
                        vjhBtnStartDate.setText("Start:\n"+result.documents[tabelIndex.toInt()]["startDate"].toString())
                        vjhBtnStopDate.setText("Stop:\n"+result.documents[tabelIndex.toInt()]["stopDate"].toString())
                        vjhDateRangeText.setText(result.documents[tabelIndex.toInt()]["jobDuration"].toString())
                        vjhJobType.setText(result.documents[tabelIndex.toInt()]["jobType"].toString())
                        vjhJobStatus.setText(result.documents[tabelIndex.toInt()]["jobStatus"].toString())
                        vjhCreatedDate.setText(result.documents[tabelIndex.toInt()]["createdDate"].toString())
                        vjhEngineerName.setText(result.documents[tabelIndex.toInt()]["engineerName"].toString())

                    }
                }
        }else if (tableAction == "SwipeRight"){
            val tabelIndex = requireArguments().getString("key")
            vjhtextLogoActive.setText("Edit Mode ")
            enabletextFields()
            getAllListDetails()



            db.collection("createdJobs").orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    if (tabelIndex != null) {
                        dataRef = result.documents[tabelIndex.toInt()]["id"].toString()
                        vjhCustomerName.setText(result.documents[tabelIndex.toInt()]["customerName"].toString())
                        vjhCustomerCountry.setText(result.documents[tabelIndex.toInt()]["customerCountry"].toString())
                        vjhCustomerState.setText(result.documents[tabelIndex.toInt()]["customerState"].toString())
                        vjhBtnStartDate.setText("Start:\n"+result.documents[tabelIndex.toInt()]["startDate"].toString())
                        vjhBtnStopDate.setText("Stop:\n"+result.documents[tabelIndex.toInt()]["stopDate"].toString())
                        vjhDateRangeText.setText(result.documents[tabelIndex.toInt()]["jobDuration"].toString())
                        vjhJobType.setText(result.documents[tabelIndex.toInt()]["jobType"].toString())
                        vjhJobStatus.setText(result.documents[tabelIndex.toInt()]["jobStatus"].toString())
                        vjhCreatedDate.setText(result.documents[tabelIndex.toInt()]["createdDate"].toString())
                        vjhEngineerName.setText(result.documents[tabelIndex.toInt()]["engineerName"].toString())

                    }
                }

        }


        closeBtnImage.setOnClickListener{
            super.onDismiss(requireDialog())
        }
        vjhCompleteJob.setOnClickListener {

            vjhJobStatus.setText("COMPLETED")

        }
        vjhOpenJob.setOnClickListener {

            vjhJobStatus.setText("ACTIVE")

        }
        vjhPendJob.setOnClickListener {

            vjhJobStatus.setText("PENDING")

        }
        vjhCancelJob.setOnClickListener {

            vjhJobStatus.setText("CANCELED")

        }

        vjhUpdateJob.setOnClickListener {
            var startDateQuery : String? = null
            var stopDateQuery :String? = null
            var customerNameQuery :String? = null
            var customerCountryQuery :String? = null
            var customerStateQuery :String? = null
            var jobDurationQuery :String? = null
            var jobTypeQuery :String? = null


            if (vjhBtnStopDate.text.toString()  != "STOP"  ) {
                val customerNameQuery = vjhCustomerName.text.toString()
                val customerCountryQuery = vjhCustomerCountry.text.toString()
                val customerStateQuery = vjhCustomerState.text.toString().capitalize()
                val jobDurationQuery = vjhDateRangeText.text.toString()
                val jobTypeQuery = vjhJobType.text.toString()
                val jobStatus = vjhJobStatus.text.toString()

                if (vjhBtnStartDate.text.split(" ").size == 1) {
                    Log.d(TAG, "one index "+vjhBtnStartDate.text.split(" ").size )
                    startDateQuery = vjhBtnStartDate.text.split(":")[1].toString().trim()
                    stopDateQuery = vjhBtnStopDate.text.split(":")[1].toString().trim()

                    //push update here


                } else if (vjhBtnStartDate.text.split(" ").size > 1) {
                    Log.d(TAG, "two index "+vjhBtnStartDate.text.split(" ").size)
                    startDateQuery = vjhBtnStartDate.text.split(":")[1].toString().trim()
                    stopDateQuery = vjhBtnStopDate.text.split(":")[1].toString().trim()

//                    Toast.makeText(context, "Reset Page with original data", Toast.LENGTH_SHORT).show()

                }
                if(customerNameQuery!!.isNotEmpty() && customerCountryQuery!!.isNotEmpty()
                    && customerStateQuery!!.isNotEmpty() && startDateQuery!!.isNotEmpty()
                    && stopDateQuery!!.isNotEmpty() && jobDurationQuery!!.isNotEmpty()
                    && jobTypeQuery!!.isNotEmpty()){

                    db.collection("createdJobs").document(dataRef.toString())
                        .update(mapOf(
                            "customerName" to customerNameQuery.toString(),
                            "customerCountry" to customerCountryQuery.toString(),
                            "customerState" to customerStateQuery.toString(),
                            "startDate" to startDateQuery.toString(),
                            "stopDate" to stopDateQuery.toString(),
                            "jobDuration" to jobDurationQuery.toString(),
                            "jobType" to jobTypeQuery.toString(),
                            "jobStatus" to jobStatus.toString(),
                            "updatedDate" to FieldValue.serverTimestamp()
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(context, "Job history updated successfully!", Toast.LENGTH_SHORT).show()
                            (activity as MainActivity).replaceFragment(jobHistoryFragment(), "Job History")
                            navView.setCheckedItem(R.id.nav_jobHistory)
                            super.onDismiss(requireDialog())
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error updating job!", Toast.LENGTH_SHORT).show()
                        }
                }else{
                    Toast.makeText(context, "Some details are missing, Please check all fields!", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context, "The start date, stop date and duration are not set", Toast.LENGTH_SHORT).show()
            }
        }



//date Creation
        val dataSetListenerStart = object: DatePickerDialog.OnDateSetListener{
            override fun onDateSet(view: DatePicker?, startYear: Int, startMonth: Int, startDay: Int) {
//                Log.d(ContentValues.TAG,"Get the date")
                calendar.set(Calendar.YEAR, startYear)
                calendar.set(Calendar.MONTH, startMonth)
                calendar.set(Calendar.DAY_OF_MONTH, startDay)
                updateDateStartDate()
            }
        }

        vjhBtnStartDate.setOnClickListener(object: View.OnClickListener{
            override fun onClick(view: View?) {
                DatePickerDialog(context!!, dataSetListenerStart,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()

            }

        } )


        val dataSetListenerStop = object: DatePickerDialog.OnDateSetListener{
            override fun onDateSet(view: DatePicker?, stopYear: Int, stopMonth: Int, stopDay: Int) {
//                Log.d(ContentValues.TAG,"Get the date")
                calendar.set(Calendar.YEAR, stopYear)
                calendar.set(Calendar.MONTH, stopMonth)
                calendar.set(Calendar.DAY_OF_MONTH, stopDay)
                updateDateStopDate()
            }
        }

        vjhBtnStopDate.setOnClickListener(object: View.OnClickListener{
            override fun onClick(view: View?) {
                DatePickerDialog(context!!, dataSetListenerStop,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()

            }

        } )
        createDialog()
        vjhDeleteJob.setOnClickListener {
            alertDialog?.show()
        }
    }




    fun createDialog() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Delete Job History")
        alertDialogBuilder.setMessage("Are you sure you want to delete record?")
        alertDialogBuilder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            deleteDocument()

        }
        alertDialogBuilder.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int ->
            Toast.makeText(context, "Action Canceled", Toast.LENGTH_SHORT).show()
        })

        alertDialog = alertDialogBuilder.create()
    }

    private fun deleteDocument() {
        Toast.makeText(context, dataRef.toString(), Toast.LENGTH_SHORT).show()
        // [START delete_document]
        db.collection("createdJobs").document(dataRef.toString().trim())
            .delete()
            .addOnSuccessListener {
                //Log.d(TAG, "Job record successfully deleted!")
                Toast.makeText(context, "Job record successfully deleted!", Toast.LENGTH_SHORT).show()
                (activity as MainActivity).replaceFragment(jobHistoryFragment(), "Job History")
                navView.setCheckedItem(R.id.nav_jobHistory)
                super.onDismiss(requireDialog())
            }
            .addOnFailureListener { e ->
                //Log.w(TAG, "Error deleting document", e)
                Toast.makeText(context, "Error deleting document!", Toast.LENGTH_SHORT).show()
            }
        // [END delete_document]
    }

    private fun updateDateStartDate() {
        val myFormat = "MM.dd.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        startDate = sdf.format(calendar.time)

        startYear = sdf.format(calendar.time).split(".")[2].toInt()
        createdYear = startYear
        startMonth = sdf.format(calendar.time).split(".")[0].toInt()
        createdMonth = startMonth
        startDay = sdf.format(calendar.time).split(".")[1].toInt()
        vjhBtnStartDate.text = "Start: "+startDate
        if(vjhBtnStartDate.text != "START"){
//            Toast.makeText(context, "is not empty", Toast.LENGTH_SHORT).show()
            vjhBtnStopDate.isEnabled = true
            vjhBtnStopDate.isClickable = true
//            vjhBtnStopDate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),R.color.btnInverse))
            vjhBtnStopDate.text = "STOP"
            vjhDateRangeText.text = "0"
        }

    }


    private fun updateDateStopDate() {
        val myFormat = "MM.dd.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        stopDate = sdf.format(calendar.time)

        stopYear= sdf.format(calendar.time).split(".")[2].toInt()
        stopMonth = sdf.format(calendar.time).split(".")[0].toInt()
        stopDay = sdf.format(calendar.time).split(".")[1].toInt()
        vjhBtnStopDate.text = "Stop: "+stopDate
        daysBetweenDates()
    }

    fun daysBetweenDates(){
//        val start = LocalDate.of(startYear, startMonth, startDay)
//        val end = LocalDate.of(stopYear, stopMonth, stopDay)
//
//        val days = ChronoUnit.DAYS.between(start, end) + 1
////        Toast.makeText(context, "days $days",Toast.LENGTH_SHORT).show()
//        vjhDateRangeText.text = days.toString()
    }



    @SuppressLint("NewApi")
    private fun enabletextFields() {
//        vjhCustomerName.isEnabled = true
//        vjhCustomerName.isClickable = true
//        vjhCustomerName.isFocusable = true
        //
        vjhCustomerCountry.isEnabled = true
        vjhCustomerCountry.isClickable = true
        vjhCustomerCountry.isFocusable = true
        //
        vjhCustomerState.isEnabled = true
        vjhCustomerState.isClickable = true
        vjhCustomerState.isFocusable = true
        //
        vjhJobType.isEnabled = true
        vjhJobType.isClickable = true
        vjhJobType.isFocusable = true
        //
        vjhBtnStartDate.isEnabled = true
        vjhBtnStartDate.isClickable = true
        vjhBtnStartDate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),R.color.btnInverse))

    }

    private fun getAllListDetails() {
    try{
        //        Code to getting country list
        val countryNamesList = ArrayList<String>()
        val countriesList: MutableList<String> = ArrayList()
        val locales = Locale.getISOCountries()
        for (countryCode in locales) {
            val obj = Locale("", countryCode)
            countriesList.add(obj.getDisplayCountry(Locale.ENGLISH))
            Collections.sort(countriesList)
        }
        for (s in countriesList) {
            countryNamesList.add(s)
        }

        val arrayAdapterCountry = ArrayAdapter(requireContext(), R.layout.customer_name_dropdown_items, countryNamesList)
        vjhCustomerCountry.setAdapter(arrayAdapterCountry)


        //Get jobTypes on view Created
        db.collection("jobTypes")
            .get()
            .addOnSuccessListener { result ->
                val jobTypesList = ArrayList<String>()
                for (document in result) {
                    //                        Log.d(TAG, "${document.id} => ${document.data["name"]}")
                    jobTypesList.add(document.data["name"].toString())
                }


                val arrayAdapter = ArrayAdapter(requireContext(), R.layout.customer_name_dropdown_items, jobTypesList)
                vjhJobType.setAdapter(arrayAdapter)

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
        }catch (e : IOException){
            FancyToast.makeText(context, "Error while fetching data from database", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }



}