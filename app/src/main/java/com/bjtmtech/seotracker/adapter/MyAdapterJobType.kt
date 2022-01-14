package com.bjtmtech.seotracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bjtmtech.seotracker.data.JobTypes
import com.bjtmtech.seotracker.R

class MyAdapterJobType(private val jobTypeList : ArrayList<JobTypes>) : RecyclerView.Adapter<MyAdapterJobType.MyViewHolder>(){

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }



    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.jobtype_item,
        parent, false)

        return MyViewHolder(itemView, mListener)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val jobs : JobTypes = jobTypeList[position]

        holder.uid.text = jobs.UID.toString()
        holder.jobTypeName.text = jobs.name.toString()
        holder.myButton
    }

    override fun getItemCount(): Int {
      return jobTypeList.size
    }

    public class MyViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val uid : TextView = itemView.findViewById(R.id.jtvId)
        val jobTypeName : TextView = itemView.findViewById(R.id.jtvJobTypeName)
        val myButton = itemView.findViewById<Button>(R.id.myButton)

        init {
            myButton.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}