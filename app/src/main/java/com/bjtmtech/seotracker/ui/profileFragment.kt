package com.bjtmtech.seotracker.ui

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bjtmtech.seotracker.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.countryEditText
import kotlinx.android.synthetic.main.fragment_profile.emailEditText
import kotlinx.android.synthetic.main.fragment_profile.firstNameEditText
import java.io.IOException
import java.util.*

class profileFragment : Fragment() {

    private lateinit var engineerEmailQuery : String
    private lateinit var sharedPref : SharedPreferences
    private var PRIVATE_MODE = 0
    var db = Firebase.firestore
    var documentID: String? = null
    val countryNamesList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        getProfileDetail()
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        isOnline(requireContext())

        editProfileBtn.setOnClickListener {
            if(editProfileBtn.text == "Edit Profile"){
                editProfileBtn.setText("Update Profile")
                //
                firstNameEditText.isEnabled = true
                firstNameEditText.isClickable = true
                firstNameEditText.isFocusable = true
                //
                titleEditText.isEnabled = true
                titleEditText.isClickable = true
                titleEditText.isFocusable = true
                //
                countryEditText.isEnabled = true
                countryEditText.isClickable = true
                countryEditText.isFocusable = true
                createCountryList()

            }else if(editProfileBtn.text == "Update Profile"){


                if(isOnline(requireContext())){
                    if (firstNameEditText.text!!.isNotEmpty() && titleEditText.text!!.isNotEmpty()
                        && countryEditText.text!!.isNotEmpty()
                    ) {

                        val firstName = firstNameEditText.text.toString().split(" ")[0]
                        val lastName = firstNameEditText.text.toString().split(" ")[1]
                        val country = countryEditText.text.toString().trim()
                        val jobTitle = titleEditText.text.toString().trim()
                        try{
//
//                        FancyToast.makeText(context, "$documentID $firstName $lastName", FancyToast.LENGTH_SHORT,FancyToast.SUCCESS, true)
//                            .show()
////                                            db.collection("users").document(documentID!!.trim())
                            db.collection("users").document(documentID!!.trim())
                                .update(mapOf(
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "jobTitle" to jobTitle,
                                    "country" to country.capitalize(),
                                    "updatedDate" to FieldValue.serverTimestamp()
                                ))
                                .addOnSuccessListener {
                                    FancyToast.makeText(context, "Profile updated created!", FancyToast.LENGTH_SHORT,FancyToast.SUCCESS, true)
                                        .show()
                                    editProfileBtn.setText("Edit Profile")
                                    firstNameEditText.isEnabled = false
                                    //
                                    titleEditText.isEnabled = false
                                    //
                                    countryEditText.isEnabled = false
                                    countryNamesList.clear()
                                    val arrayAdapterCountry = ArrayAdapter(requireContext()!!,
                                        R.layout.customer_name_dropdown_items, countryNamesList)
                                    countryEditText.setAdapter(arrayAdapterCountry)
                                }
                                .addOnFailureListener { exception ->
                                    FancyToast.makeText(context, "Error updating profile!", FancyToast.LENGTH_SHORT,FancyToast.ERROR, true)
                                        .show()
                                }
                        } catch (e: Exception) {
                            Log.d(TAG, e.printStackTrace().toString())
                            FancyToast.makeText(context, "Error: $e", FancyToast.LENGTH_SHORT,FancyToast.ERROR, true)
                                .show()
                        }
                    } else {
                        FancyToast.makeText(
                            context,
                            "Some details are missing, Please check all fields!",
                            FancyToast.LENGTH_SHORT,FancyToast.WARNING, true
                        ).show()
                    }
                }
            }
        }
    }

    private fun createCountryList() {
        //        Code to getting country list

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

        val arrayAdapterCountry = ArrayAdapter(requireContext()!!,
            R.layout.customer_name_dropdown_items, countryNamesList)
        countryEditText.setAdapter(arrayAdapterCountry)
    }

    private fun getProfileDetail() {

        if(isOnline(requireContext())){
                try{
                    sharedPref = requireContext().getSharedPreferences("myProfile" , PRIVATE_MODE)
                    engineerEmailQuery = sharedPref.getString("email" , "defaultemail@mail.com" ).toString()

                    db.collection("users")
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result)
                                if(document.data["email"] == engineerEmailQuery){
                                    documentID = document.id.toString()
//                        FancyToast.makeText(context, documentID.toString(), FancyToast.LENGTH_SHORT,
//                        FancyToast.INFO,true).show()
                                    firstNameEditText.setText(document.data["firstName"].toString() +" "+document.data["lastName"])
                                    emailEditText.setText(document.data["email"].toString())
                                    titleEditText.setText(document.data["jobTitle"].toString())
                                    countryEditText.setText(document.data["country"].toString())
                                    levelEditText.setText(document.data["level"].toString())
                                    break
                                }
                        }

                }catch (e: IOException){

                }
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
                     return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        FancyToast.makeText(context, "Error checking internet connection!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
        return false
    }
}