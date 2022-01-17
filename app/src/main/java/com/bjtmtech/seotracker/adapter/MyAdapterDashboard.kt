package com.bjtmtech.seotracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bjtmtech.seotracker.R
import com.bjtmtech.seotracker.data.ServiceEngineerData
import java.util.*

class MyAdapterDashboard (private val engineerProfileList : MutableList<ServiceEngineerData>) : RecyclerView.Adapter<MyAdapterDashboard.MyDashHolder>() {

    private lateinit var mListener: onItemClickListener
    private val calendar = Calendar.getInstance()
    var startYear:Int = calendar.get(Calendar.YEAR)

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

        holder.engineerName.text = engineersProfile.firstName.toString()+" "+engineersProfile.lastName.toString()
        holder.engineerCountry.text = engineersProfile.country.toString()
        holder.engineerTitle.text = engineersProfile.jobTitle.toString()
        holder.engineerEmail.text = engineersProfile.email.toString()
        holder.dateText.text = startYear.toString()

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

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}