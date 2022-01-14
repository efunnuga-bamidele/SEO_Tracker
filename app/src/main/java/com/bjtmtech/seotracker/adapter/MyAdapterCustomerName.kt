package com.bjtmtech.seotracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bjtmtech.seotracker.R
import com.bjtmtech.seotracker.data.CustomerName

class MyAdapterCustomerName(private val customerNameList : MutableList<CustomerName>) : RecyclerView.Adapter<MyAdapterCustomerName.MyViewHolder>(){

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }



    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.customer_item,
            parent, false)

        return MyViewHolder(itemView, mListener)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val customers : CustomerName = customerNameList[position]

        holder.uid.text = customers.UID.toString()
        holder.customerName.text = customers.name.toString()
        holder.myButton
    }

    override fun getItemCount(): Int {
        return customerNameList.size
    }

    public class MyViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val uid : TextView = itemView.findViewById(R.id.cvId)
        val customerName : TextView = itemView.findViewById(R.id.cvCustomerName)
        val myButton = itemView.findViewById<Button>(R.id.myButton)

        init {
            myButton.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}