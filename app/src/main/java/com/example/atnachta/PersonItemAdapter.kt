package com.example.atnachta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.atnachta.data.Girl

class PersonItemAdapter(var girlsData : MutableList<Girl>):
        RecyclerView.Adapter<PersonItemAdapter.ViewHolder>(){

//    var girlsData = mutableListOf<Girl>()
//        set(value){
//            field = value
//            //Note: When notifyDataSetChanged() is called, the RecyclerView redraws the whole list,
//            // not just the changed items. This is simple, and it works for now.
//            notifyDataSetChanged()
//        }

    override fun getItemCount() = girlsData.size
    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        val girlItem = girlsData[position]
        bind(holder, girlItem)
//        holder.textView.text = girlItem
//        if ("2" in girlItem){
//            holder.textView.setTextColor(Color.RED)
//        }
    }

    private fun bind(holder: ViewHolder, girlItem: Girl) {
//        val res = holder.itemView.context.resources
        holder.profileName.setText("${girlItem.firstName} ${girlItem.lastName}")
//        holder.profileCity.text = "???"
        holder.profileAge.setText("${girlItem.age}")
        holder.profileImage.setImageResource(when (girlItem.age) {
            15 -> R.drawable.ic_launcher_background
            else -> R.drawable.ic_launcher_foreground
        })
//        holder.qualityImage.setOnClickListener { holder.sleepLength.text = "hsdghdsfjk"}
        holder.profileImage.setOnClickListener {addEyal(girlItem)}
    }

    public fun editResultList(girlsData: MutableList<Girl>){
        this.girlsData = girlsData
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.inflate(R.layout.list_item_persons_results, parent, false)
        val view = layoutInflater.inflate(R.layout.list_item_persons_results, parent, false)
        return ViewHolder(view)
    }
    private fun addEyal(girly : Girl){
        girlsData.add(Girl(girly.firstName,girly.lastName,girly.age+10))
        val girl: Girl= girlsData[0]
        girlsData[0] = girlsData[this.itemCount-1]
        girlsData[this.itemCount-1] = girl
        notifyItemInserted(this.itemCount-1)
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val profileName: TextView = itemView.findViewById(R.id.fullNameText)
        val profileCity: TextView = itemView.findViewById(R.id.cityNameText)
        val profileImage: ImageView = itemView.findViewById(R.id.quality_image)
        val profileAge: TextView = itemView.findViewById(R.id.ageText)
    }


}