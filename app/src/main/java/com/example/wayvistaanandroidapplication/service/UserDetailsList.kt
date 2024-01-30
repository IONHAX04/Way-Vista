package com.example.wayvistaanandroidapplication.service

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.wayvistaanandroidapplication.R
import com.example.wayvistaanandroidapplication.activities.ProfileActivity
import com.example.wayvistaanandroidapplication.utilities.UserDetails

class UserDetailsList(
    private val list: List<UserDetails>,
    val userSelect: (UserDetails) -> Unit
) : RecyclerView.Adapter<UserDetailsList.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userDet = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_template, parent, false)
        return ViewHolder(userDet)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
        if (user.id != ProfileActivity.userId) {
            holder.userTemplate.text = user.name
        } else {
            holder.template.visibility = View.GONE
        }
        holder.template.setOnClickListener {
            userSelect(user)
        }
    }

    class ViewHolder(userDet: View) : RecyclerView.ViewHolder(userDet) {
        val template: LinearLayout = userDet.findViewById(R.id.templateUser)
        val userTemplate: TextView = userDet.findViewById(R.id.userDetailsTemplate)
    }
}