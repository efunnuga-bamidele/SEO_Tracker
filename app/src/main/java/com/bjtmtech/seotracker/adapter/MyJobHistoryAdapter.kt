package com.bjtmtech.seotracker.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
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

    private fun colorMyText(inputText:String,startIndex:Int,endIndex:Int,textColor:Int):Spannable{
        val outPutColoredText: Spannable = SpannableString(inputText)
        outPutColoredText.setSpan(
            ForegroundColorSpan(textColor), startIndex, endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return outPutColoredText
    }
    override fun onBindViewHolder(holder: MyJobHistoryAdapter.JobViewHolder, position: Int) {
       val jobhistory : JobHistoryData = myJobsHistoryList[position]

        holder.documentName.text ="ID: "+ jobhistory.id.toString()
        holder.customerName.text = jobhistory.customerName.toString()
        holder.engineerName.text = jobhistory.engineerName.toString()
        holder.documentDetails.text =  buildSpannedString {
                                                            bold { append("Period: ") }
                                                            append(colorMyText("${jobhistory.startDate.toString()} - ${jobhistory.stopDate.toString()}",0,"${jobhistory.startDate.toString()} - ${jobhistory.stopDate.toString()}".length,
                                                                Color.RED))

                                                        }
//            "Period: "+jobhistory.startDate.toString() + " - "+
//                jobhistory.stopDate.toString()
        holder.jobStatus.text = buildSpannedString {
                                                    bold { append("Job Status: ") }
                                                    append(colorMyText("${jobhistory.jobStatus.toString()}",0,"${jobhistory.jobStatus.toString()}".length,
                                                        Color.RED))
        }


        holder.jobType.text =   buildSpannedString {
            bold { append("Job Type: ") }
            append("${jobhistory.jobType.toString()}")
        }
//            "Job Status: "+jobhistory.jobStatus.toString()
        holder.jobCreated.text =    buildSpannedString {
                                                        bold { append("Job Created on: ") }
                                                        append("${jobhistory.createdDate.toString()}")
                                                    }
//            "Job Created on: "+jobhistory.createdDate.toString()
        holder.jobCountry.text =   buildSpannedString {
                                                        bold { append("Customer Country: ") }
                                                        append("${jobhistory.customerCountry.toString()}")
                                                    }
//            "Customer Country: "+ jobhistory.customerCountry.toString()
        holder.jobDuration.text =    buildSpannedString {
                                                        bold { append("Job Duration: ") }
                                                        bold { append(colorMyText("${jobhistory.jobDuration.toString()}",0,"${jobhistory.jobDuration.toString()}".length,
                                                                                                        Color.BLUE))}
                                                    }
//            "Job Duration: "+ jobhistory.jobDuration.toString()

//        Company [2010.02.11 - 2010.02.19"

        val isVisible : Boolean = jobhistory.visibility
        holder.constraintLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
        holder.documentName.setOnClickListener{
            jobhistory.visibility = !jobhistory.visibility
            notifyItemChanged(position)
        }

    }

    override fun getItemCount(): Int {
        return myJobsHistoryList.size
    }

    public class JobViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val documentName : TextView = itemView.findViewById(R.id.text_view_name)
        val documentDetails : TextView = itemView.findViewById(R.id.text_view_document)
        val customerName : TextView = itemView.findViewById(R.id.text_view_customer)
        val engineerName : TextView = itemView.findViewById(R.id.text_view_engineer)
        val jobCountry : TextView = itemView.findViewById(R.id.text_view_country)
        val jobStatus : TextView = itemView.findViewById(R.id.text_view_status)
        val jobType : TextView = itemView.findViewById(R.id.text_job_type)
        val jobCreated : TextView = itemView.findViewById(R.id.text_view_Created)
        val jobDuration : TextView = itemView.findViewById(R.id.text_view_Durtion)
        val constraintLayout : ConstraintLayout = itemView.findViewById(R.id.expandableLayout)


        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

    }
}