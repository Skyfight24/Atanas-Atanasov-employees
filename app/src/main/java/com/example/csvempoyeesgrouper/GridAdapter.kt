package com.example.csvempoyeesgrouper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GridAdapter(private val data: List<GridItem>) : RecyclerView.Adapter<GridAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val employee1: TextView = itemView.findViewById(R.id.employee1)  //set later on
        val employee2: TextView = itemView.findViewById(R.id.employee2)
        val projectId: TextView = itemView.findViewById(R.id.projectId)
        val daysWorked: TextView = itemView.findViewById(R.id.daysWorked)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_employee_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.employee1.text = data[position].employee1.toString()
        holder.employee2.text = data[position].employee2.toString()
        holder.projectId.text = data[position].projectId.toString()
        holder.daysWorked.text = data[position].daysWorked.toString()
    }

    override fun getItemCount(): Int {
        return data.size
    }
}