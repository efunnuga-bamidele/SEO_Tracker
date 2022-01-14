package com.bjtmtech.seotracker.ui


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bjtmtech.seotracker.R
import com.bjtmtech.seotracker.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_register.*
import java.io.IOException
import java.util.*


class RegisterActivity : AppCompatActivity() {

    val db = Firebase.firestore
    private lateinit var auth : FirebaseAuth
    lateinit var shared : SharedPreferences
    private val calendar = Calendar.getInstance()
    var startYear:Int = calendar.get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        copyRightView.text = "Copyright @ $startYear"


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

        val arrayAdapterCountry = ArrayAdapter(this,
            R.layout.customer_name_dropdown_items, countryNamesList)
        countryEditText.setAdapter(arrayAdapterCountry)





        registerBtn.setOnClickListener{


            val firstName = firstNameEditText.text.toString().capitalize().trim()
            val lastName = lastNameEditText.text.toString().capitalize().trim()
            val country = countryEditText.text.toString().capitalize().trim()
            val jobTitle = jobTitleEditText.text.toString().capitalize().trim()
            val email = emailEditText.text.toString().lowercase().trim()
            val password = initialPassword.text.toString().trim()
            val retypePassword = reTypePassword.text.toString().trim()



//Check for empty input fields
            if(firstName.isNotEmpty() && lastName.isNotEmpty() && country.isNotEmpty() && jobTitle.isNotEmpty() && email.isNotEmpty()
                && email.contains("@") && password.isNotEmpty() && password.length >= 6 && retypePassword.isNotEmpty()){

                if( password == retypePassword){

                    val User = User(firstName, lastName, country, jobTitle, email)
                    if(isOnline(this)){

                        try{
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                        RegisterActivity()
                    ) {
                            task ->
                        if(task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "User Registration Successfully!",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            val user = hashMapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "country" to country,
                                "jobTitle" to jobTitle,
                                "email" to email,
                                "level" to "Admin",
                                "language" to "english"
                            )

                            db.collection("users").document(user["firstName"].toString()+"_"+user["lastName"].toString())
                                .set(user)
                                .addOnSuccessListener { documentReference ->
                                    clearField()

                                    Toast.makeText(
                                        this,
                                        "User Profile Created!",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "User Registration Failed! Please try again",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                        }else{

//                            Login user if account exists
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener{mTask ->

                                    if(mTask.isSuccessful){
                                        Toast.makeText(
                                            this,
                                            "User Already Exist Logging In User",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        var userName = email.split("@")[0]
                                        var dashboardIntent = Intent(this, MainActivity::class.java)
                                        shared = getSharedPreferences("myProfile" , Context.MODE_PRIVATE)
                                        val edit = shared.edit()
                                        edit.putString("email" , email)
                                        edit.putString("name", userName)
                                        edit.apply()

                                        startActivity(dashboardIntent)
                                        finish()

                                    }else{
                                        FancyToast.makeText(
                                            this,
                                            "User Registration Failed! Please try again",
                                            FancyToast.LENGTH_SHORT,
                                            FancyToast.ERROR,
                                            true
                                        ).show()

                                    }
                                }
                        }
                    }
//                    End
                        }catch (e: IOException){
                            Log.e("ERROR", "Exception : $e");

                        }
                    }

                }else{
                    FancyToast.makeText(
                        this,
                        "Password and Re-Enter Password is not a Match! ",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.WARNING,
                        true
                    ).show()
                }

            }else{
                FancyToast.makeText(
                    this,
                    "Make sure all fields are filled",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.WARNING,
                    true
                ).show()
            }

        }

        clearBtn.setOnClickListener {
            clearField()
        }
    }

    private fun clearField() {
        firstNameEditText.text!!.clear()
        lastNameEditText.text!!.clear()
        countryEditText.text!!.clear()
        jobTitleEditText.text!!.clear()
        emailEditText.text!!.clear()
        initialPassword.text!!.clear()
        reTypePassword.text!!.clear()
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

}

