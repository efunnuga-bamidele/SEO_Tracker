package com.bjtmtech.seotracker

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
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
import androidx.appcompat.widget.SearchView
import com.bjtmtech.seotracker.ui.ViewJobsHistoryFragment
import java.util.*

//import android.widget.SearchView


class jobHistoryFragment : Fragment() {

    private var EditUID : Int? = null
    val db = Firebase.firestore
    private var dataSize: Int = 0
    private lateinit var recyclerViewHistory : RecyclerView
    private lateinit var  jobsHistoryList : ArrayList<JobHistoryData>
    lateinit var  heading : Array<String>
    private lateinit var  myAdapterHistory : MyJobHistoryAdapter

    private lateinit var engineerEmailQuery : String
    private lateinit var sharedPref : SharedPreferences
    private var PRIVATE_MODE = 0
    private lateinit var searchArrayList: ArrayList<JobHistoryData>
//    lateinit var menuInflator : MenuInflater


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater): Boolean {
//        inflater.inflate(R.menu.search_menu, menu)
//
//        return super.onCreateOptionsMenu(menu, inflater)
//
////        return super.onCreateOptionsMenu(menu!!, inflater)
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu?.findItem(R.id.search_action)
        val searchView = item?.actionView as SearchView

        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchArrayList.clear()
                val searchText = newText!!.toLowerCase(Locale.getDefault())
                if(searchText.isNotEmpty()){
                    jobsHistoryList.forEach{
                        if (it.customerName!!.toLowerCase(Locale.getDefault())!!.contains(searchText)){
                            searchArrayList.add(it)
                        }
                    }
                    myAdapterHistory!!.notifyDataSetChanged()
                }else {

//                    for (i in jobsHistoryList.indices) {
//                        jobsHistoryList.removeAt(0)
//                    }
                    searchArrayList.clear()
                    searchArrayList.addAll(jobsHistoryList)
                    myAdapterHistory!!.notifyDataSetChanged()
                }

                return false
            }

        })
        return super.onCreateOptionsMenu(menu!!, inflater)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.search_action -> FancyToast.makeText(
//                context,
//                "Search Clicked",
//                FancyToast.LENGTH_SHORT,
//                FancyToast.INFO,
//                true
//            ).show()
////            R.id.refresh -> webView.reload()
//        }
//        return true
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //        set sharedpreference to get email of user
        sharedPref = context!!.getSharedPreferences("myProfile", PRIVATE_MODE)
//        create variable to collect the email address
        engineerEmailQuery = sharedPref.getString("email", "defaultemail@mail.com").toString()
        setup()

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
//                Toast.makeText(context, "You clicked item "+position, Toast.LENGTH_SHORT).show()
//                ViewJobsHistoryFragment().show(childFragmentManager, "View")
                val args = Bundle()
                args.putString("key", position.toString())
                args.putString("action", "Clicked")
                val fm: FragmentManager = activity!!.supportFragmentManager
                val overlay = ViewJobsHistoryFragment()
                overlay.setArguments(args)
                overlay.show(fm, "FragmentDialog")

            }

        })

        EventChangeListener()

        //Add Divider
        recyclerViewHistory.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )


        //get item position on recycler view
    val itemTouchHelper = ItemTouchHelper(simpleCallback)
    itemTouchHelper.attachToRecyclerView(recyclerViewHistory)

        isOnline(context!!)

    }

    private fun setup() {

        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings
    }

    private fun EventChangeListener() {
        try {
            db.collection("createdJobs").orderBy("createdDate", Query.Direction.DESCENDING)
                .whereEqualTo("engineerEmail", engineerEmailQuery.toString())
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

                            }

                        }
                        myAdapterHistory.notifyDataSetChanged()
                        searchArrayList.addAll(jobsHistoryList)
                    }

                })
        }catch (e: IOException){
            FancyToast.makeText(context, "Error while fetching data from database", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
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
//    override fun onDestroy() {
//        super.onDestroy()
//    }

}
