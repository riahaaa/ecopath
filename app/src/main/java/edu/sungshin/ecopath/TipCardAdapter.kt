package edu.sungshin.ecopath

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sungshin.ecopath.R


class TipCardAdapter(private val tips: List<TipCard>) : RecyclerView.Adapter<TipCardAdapter.TipCardViewHolder>() {

    inner class TipCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.page_tip_card, parent, false)
        return TipCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipCardViewHolder, position: Int) {
        val tip = tips[position]
        holder.title.text = tip.content
    }

    override fun getItemCount(): Int {
        return tips.size
    }
}
