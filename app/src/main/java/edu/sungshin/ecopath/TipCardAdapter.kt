package edu.sungshin.ecopath

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sungshin.ecopath.R

class TipCardAdapter(private val tips: List<TipCard>) : RecyclerView.Adapter<TipCardAdapter.TipCardViewHolder>() {

    class TipCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardImage: ImageView = view.findViewById(R.id.card_image)
        val cardTitle: TextView = view.findViewById(R.id.card_title)
        val cardDescription: TextView = view.findViewById(R.id.card_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return TipCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipCardViewHolder, position: Int) {
        val tip = tips[position]
        holder.cardImage.setImageResource(tip.imageResId)
        holder.cardTitle.text = tip.title
        holder.cardDescription.text = tip.description
    }

    override fun getItemCount(): Int {
        return tips.size
    }
}
