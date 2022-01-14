package com.bjtmtech.seotracker.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import android.content.SharedPreferences
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.bjtmtech.seotracker.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_login.*
//import kotlinx.android.synthetic.main.activity_login.*
//import kotlinx.android.synthetic.main.activity_register.*
import java.io.IOException
import java.util.*


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var shared: SharedPreferences
    val db = Firebase.firestore
    var queryFirstName: String? = null
    var queryLastName: String? = null
    var queryEmail: String? = null
    private val calendar = Calendar.getInstance()
    var startYear:Int = calendar.get(Calendar.YEAR)
    var alertDialog: AlertDialog? = null
    lateinit var resetMail : TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        onStart() //check if user session is still logged in and go to dashboard

        val registerButton = findViewById<TextView>(R.id.registerUser)
        copyRightViewLV.text = "Copyright @ $startYear"
        resetUser.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        registerUser.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        resetMail = TextInputEditText(this)
        resetMail.hint = "Email address"
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
//            finish()
        }

        loginBtn.setOnClickListener {

            val loginEmail = loginEmailText.text.toString().trim()

            val loginPassword = loginTextPassword.text.toString().trim()

            if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)) {
                loginUser(loginEmail, loginPassword)
            } else {
                FancyToast.makeText(
                    this,
                    "User input empty!",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    true
                ).show()

            }


        }
        createDialog()
        resetUser.setOnClickListener {
            alertDialog?.show()
        }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.

            try {
                var currentUser = auth.currentUser
                if (currentUser != null) {
                    currentUser = auth.currentUser
                    var userName = currentUser?.email.toString().split("@")[0]
                    if (currentUser != null) {
                        var dashboardIntent = Intent(this, MainActivity::class.java)
                        shared = getSharedPreferences("myProfile", Context.MODE_PRIVATE)
                        val edit = shared.edit()
                        edit.putString("email", currentUser.email.toString())
                        edit.putString("name", userName)
                        edit.apply()

                        startActivity(dashboardIntent)
                        finish()

                    }
                }
            } catch (e: IOException) {
                Log.e("ERROR", "Exception : $e");

            }

    }

        private fun loginUser(email: String, password: String) {
            if (isOnline(this)) {
                try {


            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { mTask ->

                    if (mTask.isSuccessful) {
                        FancyToast.makeText(
                            this,
                            "User Logged in Successfully",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.SUCCESS,
                            true
                        ).show()
//                        queryEmail = email.toString()
                        queryEmail = auth.currentUser!!.email.toString()
//                        Toast.makeText(this, "$queryEmail", Toast.LENGTH_SHORT).show()
                        var userName = queryEmail!!.split("@")[0]
//                        var userName = email.split("@")[0]
                        var dashboardIntent = Intent(this, MainActivity::class.java)
                        shared = getSharedPreferences("myProfile", Context.MODE_PRIVATE)
                        val edit = shared.edit()
                        edit.putString("email", email)
                        edit.putString("name", userName)
                        edit.apply()

                        startActivity(dashboardIntent)
                        finish()

                    } else {

                        FancyToast.makeText(
                            this,
                            "User Logged in Failed!",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.ERROR,
                            true
                        ).show()

                    }
                }
                } catch (e: IOException) {
                    Log.e("ERROR", "Exception : $e");

                }
        }
        }

    fun createDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle("Reset Password?")
        alertDialogBuilder.setMessage("Enter your registered email address to receive the reset link")
        alertDialogBuilder.setView(resetMail)
        alertDialogBuilder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
//            deleteDocument()
            var mail = resetMail.text.toString()
            if (isOnline(this)){
                if(!TextUtils.isEmpty(mail)){
                    auth.sendPasswordResetEmail(mail)
                        .addOnSuccessListener {
                            FancyToast.makeText(this, "Reset link sent to: ${resetMail.text.toString()}!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()

                        }.addOnFailureListener {
                            FancyToast.makeText(this, "Failed to send reset link", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()

                        }
                }else{
                    FancyToast.makeText(
                        this,
                        "Email field is empty!",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,
                        true
                    ).show()

                }
            }

        }
        alertDialogBuilder.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int ->
            FancyToast.makeText(this, "Action Canceled", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show()
        })

        alertDialog = alertDialogBuilder.create()
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

