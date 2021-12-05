package com.gmail.kijevigombooc.studybuddy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gmail.kijevigombooc.studybuddy.R
import kotlinx.android.synthetic.main.item_topic.view.*

class TopicAdapter(private val topics : List<String>, private val onClick : (String) -> Unit) : RecyclerView.Adapter<TopicAdapter.ViewHolder>(){

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val view = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.tvTopicName.text = topics[position]
        holder.view.setOnClickListener{
            onClick(topics[position])
        }
    }

    override fun getItemCount(): Int {
        return topics.size
    }
}