package com.gmail.kijevigombooc.studybuddy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gmail.kijevigombooc.studybuddy.R
import com.gmail.kijevigombooc.studybuddy.databinding.ItemSubjectBinding
import kotlinx.android.synthetic.main.item_subject.view.*

class SubjectAdapter(private val onClick : (String) -> Unit) : RecyclerView.Adapter<SubjectAdapter.ViewHolder>(){

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val binding = ItemSubjectBinding.bind(itemView)
    }

    var subjects : MutableList<String> = mutableListOf<String>()

    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subject, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvSubjectName.text = subjects[position]
        holder.binding.root.setOnClickListener{
            onClick(subjects[position])
        }
    }

    fun addSubject(subjectName : String, index : Int){
        subjects.add(index, subjectName)
        notifyItemInserted(index)
        notifyItemRangeChanged(index, subjects.size - index)
    }

    fun removeSubject(position: Int) : String {
        val toReturn = subjects[position]
        subjects.removeAt(position)
        notifyItemRemoved(position)
        if (position < subjects.size) {
            notifyItemRangeChanged(position, subjects.size - position)
        }
        return toReturn
    }

    override fun getItemCount(): Int {
        return subjects.size
    }
}