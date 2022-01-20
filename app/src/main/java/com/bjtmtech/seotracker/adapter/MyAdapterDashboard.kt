package com.bjtmtech.seotracker.adapter



import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.helper.widget.MotionEffect.TAG
import androidx.recyclerview.widget.RecyclerView
import com.bjtmtech.seotracker.R
import com.bjtmtech.seotracker.data.ServiceEngineerData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.dashboard_items.*
import java.io.IOException
import java.util.*

class MyAdapterDashboard (private val engineerProfileList : MutableList<ServiceEngineerData>) : RecyclerView.Adapter<MyAdapterDashboard.MyDashHolder>() {

    private lateinit var mListener: onItemClickListener
    private val calendar = Calendar.getInstance()
    var startYear:Int = calendar.get(Calendar.YEAR)
    val db = Firebase.firestore
    var engineerEmailQuery: String ?= null
    var duration : Int = 0

    interface onItemClickListener {
        fun onItemClick(position: Int)

    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyAdapterDashboard.MyDashHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.dashboard_items, parent, false
        )

        return MyDashHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyAdapterDashboard.MyDashHolder, position: Int) {
        val engineersProfile: ServiceEngineerData = engineerProfileList[position]

        holder.engineerName.text = engineersProfile.firstName.toString().capitalize()+" "+engineersProfile.lastName.toString().capitalize()
        holder.engineerCountry.text = engineersProfile.country.toString().capitalize()
        holder.engineerTitle.text = engineersProfile.jobTitle.toString().capitalize()
        holder.engineerEmail.text = engineersProfile.email.toString()
        engineerEmailQuery = engineersProfile.email.toString()
        holder.dateText.text = startYear.toString()

//        Get user work details based on email retrieved from mutable list
        val emails = ArrayList<String>()
        emails.add(engineerEmailQuery.toString())
//        Loop through list of emails while i query database to get work history and work durations
        for(email in emails) {
//            Log.d(TAG,"Current email: ${email.trim()}")
            db.collection("createdJobs")
                .whereEqualTo("engineerEmail", email.toString())
                .whereEqualTo("jobStatus", "COMPLETED")
                .whereEqualTo("createdYear", startYear.toString())
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
//                        Log.d(TAG,"Current ID: ${document.id}")
//                        Log.d(TAG,"Current Year: ${startYear}")
                        duration += document.data["jobDuration"].toString().toInt()
                    }
//                    add duration to specific holder component and clear the duration count
                    holder.daysWorked.text = duration.toString()
                    duration = 0

                }
                .addOnFailureListener { exception ->
                    holder.daysWorked.text = "0"

                }

        }

    }

    override fun getItemCount(): Int {
        return engineerProfileList.size
    }

    public class MyDashHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {

        val engineerName: TextView = itemView.findViewById(R.id.text_view_name)
        val engineerCountry: TextView = itemView.findViewById(R.id.textCountry)
        val engineerTitle: TextView = itemView.findViewById(R.id.jotTitle)
        val engineerEmail: TextView = itemView.findViewById(R.id.emailText)
        val dateText : TextView = itemView.findViewById(R.id.reportYear)
        val daysWorked: TextView = itemView.findViewById(R.id.countTxtView)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}