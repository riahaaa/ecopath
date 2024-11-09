package edu.sungshin.ecopath

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RouteAdapter(
    private val routeList: List<Route>,
    private val onRouteClick: (Route) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.route_item, parent, false)
        return RouteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routeList[position]

        holder.routeInfo.text = route.info
        holder.duration.text = "소요 시간: ${route.duration}"
        holder.distance.text = "거리: ${route.distance}"
        holder.transportIcon.setImageResource(getTransportIcon(route.travelMode))

        holder.itemView.setOnClickListener {
            onRouteClick(route)
        }
    }

    override fun getItemCount(): Int = routeList.size

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routeInfo: TextView = itemView.findViewById(R.id.textViewRouteInfo)
        val duration: TextView = itemView.findViewById(R.id.textViewRouteDuration)
        val distance: TextView = itemView.findViewById(R.id.textViewRouteDistance)
        val transportIcon: ImageView = itemView.findViewById(R.id.imageViewTransportMode)
    }

    private fun getTransportIcon(mode: String): Int {
        return when (mode.lowercase()) {
            "driving" -> R.drawable.ic_car

            else -> R.drawable.ic_default
        }
    }
}
