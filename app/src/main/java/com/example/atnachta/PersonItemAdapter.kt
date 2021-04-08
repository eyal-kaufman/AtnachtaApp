package com.example.atnachta

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.atnachta.data.Girl

class PersonItemAdapter: RecyclerView.Adapter<TextItemViewHolder>(){
    var girlsData = listOf<Girl>()
        set(value){
            field = value
            //Note: When notifyDataSetChanged() is called, the RecyclerView redraws the whole list,
            // not just the changed items. This is simple, and it works for now.
            notifyDataSetChanged()
        }

    override fun getItemCount() = girlsData.size
    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        val girlItem = girlsData[position]
        holder.textView.text = girlItem.firstName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.person_item_view, parent, false) as
                TextView
        return TextItemViewHolder(view)
    }

}