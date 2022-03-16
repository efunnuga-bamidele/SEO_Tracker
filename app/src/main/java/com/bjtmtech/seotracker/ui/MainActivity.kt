package com.bjtmtech.seotracker.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.nav_header.*
import android.content.SharedPreferences
import android.view.Menu
import com.bjtmtech.seotracker.R
import com.bjtmtech.seotracker.customersFragment
import com.bjtmtech.seotracker.jobHistoryFragment
import com.bjtmtech.seotracker.jobtypeFragment
import com.google.firebase.firestore.ktx.firestore
import com.shashank.sony.fancytoastlib.FancyToast
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser
    lateinit var userDetail:String
    lateinit var message2:String
    lateinit var shared : SharedPreferences
    lateinit var navView: NavigationView
    var db = Firebase.firestore

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
            navView = findViewById(R.id.nav_view)


        drawerLayout = findViewById(R.id.drawerLayout)





        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        checkUserLevel()




        }
    private fun checkUserLevel(){
        shared = getSharedPreferences("myProfile" , Context.MODE_PRIVATE)
        userDetail = shared.getString("email" , "defaultemail@mail.com" ).toString()

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result)
                    if(document.data["email"] == userDetail){
//                        Toast.makeText(this, document.data["level"].toString(), Toast.LENGTH_SHORT).show()
                        if (document.data["level"].toString() == "Admin"){
                            replaceFragment(dashboardFragment(), "Dashboard")
//            replaceFragment(jobHistoryFragment(), "Dashboard")
                            navView.setCheckedItem(R.id.nav_dashboard)

                            navView.setNavigationItemSelectedListener {

                                it.isChecked = true  //to highlight the item
                                when (it.itemId) {

                                    R.id.nav_dashboard -> replaceFragment(dashboardFragment(), it.title.toString())
//                R.id.nav_create_job -> replaceFragment(createJobFragment(), it.title.toString())
//                R.id.nav_view_active_job -> replaceFragment(activeJobFragment(), it.title.toString())

                                    R.id.nav_jobHistory -> replaceFragment(jobHistoryFragment(), it.title.toString())
                                    R.id.nav_jobType -> replaceFragment(jobtypeFragment(), it.title.toString())
//                R.id.nav_language -> replaceFragment(languageFragment(), it.title.toString())
                                    R.id.nav_customers -> replaceFragment(customersFragment(), it.title.toString())
                                    R.id.nav_profile -> replaceFragment(profileFragment(), it.title.toString())
                                    R.id.nav_logout -> signout()
                                }
                                true
                            }
                        }else{
                            signout()
                            FancyToast.makeText(
                                this,
                                "Error user not an administrator!",
                                FancyToast.LENGTH_SHORT,
                                FancyToast.ERROR,
                                true
                            ).show()
                        }
                        break
                    }
            }



    }


    private var back_press: Long = 0

    override fun onBackPressed() {
        if (back_press + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(
                baseContext, "Please press again to exit!",
                Toast.LENGTH_SHORT
            ).show()
            back_press = System.currentTimeMillis()
        }
    }

    //    used to replace fragment
    public fun replaceFragment(fragment: Fragment, title: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.framelayout, fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers()
        setTitle(title)  //set title

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)) {
            getExtras()
//            checkUserLevel()
            return true
        }

        val id = item.getItemId()

//        if (id == R.id.nav_language) {
//            replaceFragment(languageFragment(), "Language")
////            to bring out action dialogbox
////            navView.setCheckedItem(0)
//            return true
//        }
        return super.onOptionsItemSelected(item)
    }

    private fun getExtras(){

        //Extract user details
        shared = getSharedPreferences("myProfile" , Context.MODE_PRIVATE)
        user_email.text = shared.getString("email" , "defaultemail@mail.com" ).toString()
        user_name.text = shared.getString("name" , "defultuser" ).toString()
        userDetail = shared.getString("email" , "defaultemail@mail.com" ).toString()

    }

    private fun signout(){
        Firebase.auth.signOut()
        var loginPage = Intent(this, LoginActivity::class.java)
        startActivity(loginPage)
        finish()
        val edit = shared.edit()
        edit.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.setting_menu, menu)
        return true
    }




}


