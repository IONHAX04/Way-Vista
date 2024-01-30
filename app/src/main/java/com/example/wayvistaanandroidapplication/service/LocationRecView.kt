package com.example.wayvistaanandroidapplication.service

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wayvistaanandroidapplication.R
import com.example.wayvistaanandroidapplication.utilities.MapLocation

class LocationRecView(
    private val context: Context,
    private val locationList: List<MapLocation>,
    val currentIndex: (Int) -> Unit
) : RecyclerView.Adapter<LocationRecView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationRecView.ViewHolder {
        val mapView = LayoutInflater.from(context).inflate(R.layout.history, parent, false)
        return ViewHolder(mapView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = locationList[position]
        holder.location.text = item.time
        holder.image.setImageResource(R.drawable.destination);
        holder.lat.text = (item.places +", "+ item.states)
        holder.long.text = (item.districts + ":"+item.userId)

        holder.template.setOnClickListener { currentIndex(position) }
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    class ViewHolder(mapView: View) : RecyclerView.ViewHolder(mapView) {
        val template: RelativeLayout = mapView.findViewById(R.id.template)
        val image: ImageView = mapView.findViewById(R.id.recent)
        val location: TextView = mapView.findViewById(R.id.location)
        val lat: TextView = mapView.findViewById(R.id.lat)
        val long: TextView = mapView.findViewById(R.id.lon)
    }
}