package com.example.snake2025


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.snake2025.models.Score


class ScoresAdapter(private val items: List<Score>) : RecyclerView.Adapter<ScoresAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvPlayer)
        val tvScore: TextView = v.findViewById(R.id.tvScore)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_score, parent, false)
        return VH(v)
    }


    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.tvName.text = s.username ?: "Unknown"
        holder.tvScore.text = s.score.toString()
    }


    override fun getItemCount(): Int = items.size
}