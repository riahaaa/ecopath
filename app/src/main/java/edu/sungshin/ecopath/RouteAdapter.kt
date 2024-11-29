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

        // 소요 시간을 "시간:분" 형식으로 변환
        val formattedDuration = formatDuration(route.duration.toInt())
        holder.duration.text = "소요 시간: $formattedDuration"

        // 거리를 킬로미터로 변환
        val formattedDistance = formatDistance(route.distance.toInt())
        holder.distance.text = "거리: $formattedDistance"

        holder.itemView.setOnClickListener {
            onRouteClick(route)
        }
    }


    // 거리(미터)를 "킬로미터"로 변환하는 함수
    private fun formatDistance(distanceInMeters: Int): String {
        val distanceInKm = distanceInMeters / 1000.0
        return if (distanceInKm >= 1) {
            String.format("%.1f km", distanceInKm)
        } else {
            "$distanceInMeters m"
        }
    }


    // 소요 시간(초)을 "시간:분" 형식으로 변환하는 함수
    private fun formatDuration(duration: Int): String {
        val hours = duration / 3600 // 1시간 = 3600초
        val minutes = (duration % 3600) / 60 // 남은 초를 분으로 변환

        return if (hours > 0) {
            "${hours}시간 ${minutes}분"
        } else {
            "${minutes}분"
        }
    }

    override fun getItemCount(): Int = routeList.size

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routeInfo: TextView = itemView.findViewById(R.id.textViewRouteInfo)
        val duration: TextView = itemView.findViewById(R.id.textViewRouteDuration)
        val distance: TextView = itemView.findViewById(R.id.textViewRouteDistance)
    }

}