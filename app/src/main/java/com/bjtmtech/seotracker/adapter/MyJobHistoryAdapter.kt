package com.bjtmtech.seotracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bjtmtech.seotracker.R
import com.bjtmtech.seotracker.data.JobHistoryData

class MyJobHistoryAdapter(private var myJobsHistoryList : ArrayList<JobHistoryData>) : RecyclerView.Adapter<MyJobHistoryAdapter.JobViewHolder>() {
private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyJobHistoryAdapter.JobViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.jobhistory_item, parent, false)

        return JobViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyJobHistoryAdapter.JobViewHolder, position: Int) {
       val jobhistory : JobHistoryData = myJobsHistoryList[position]

        holder.documentName.text = jobhistory.id.toString()
        holder.documentDetails.text = jobhistory.customerName.toString() +" [ "+jobhistory.startDate.toString() + " - "+
                jobhistory.stopDate.toString()+" ]"

//        Company [2010.02.11 - 2010.02.19"
    }

    override fun getItemCount(): Int {
        return myJobsHistoryList.size
    }

    public class JobViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val documentName : TextView = itemView.findViewById(R.id.text_view_name)
        val documentDetails : TextView = itemView.findViewById(R.id.text_view_document)


        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

    }
}