package com.example.atnachta

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.atnachta.data.Profile
import com.example.atnachta.databinding.ListItemPersonsResultsBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot

class ProfileAdapter(options: FirestoreRecyclerOptions<Profile>, private val listener: OnProfileSelectedListener) : FirestoreRecyclerAdapter<Profile, ProfileAdapter.ProfileHolder>(
    options
) {

    class ProfileHolder(val binding: ListItemPersonsResultsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_persons_results,
//            parent, false)
        return ProfileHolder(ListItemPersonsResultsBinding.
                inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ProfileHolder, position: Int, model: Profile) {
        holder.binding.fullNameText.text = "${model.firstName} ${model.lastName}"
        holder.binding.ageText.text = if (model.age == null){ "" } else {model.age.toString()}
        holder.binding.IDValue.text = model.ID
        holder.binding.AddressValue.text = model.homeAddress
        holder.binding.qualityImage.setImageResource(R.drawable.ic_baseline_person_24)

        // onClickListener for the holder - click received by the holder but handled by the fragment
        holder.binding.root.setOnClickListener {
            listener.onProfileSelected(snapshots.getSnapshot(position),false)
        }
        holder.binding.root.setOnLongClickListener {
            listener.onProfileSelected(snapshots.getSnapshot(position),true)
            true
        }
    }

//    override fun onBindViewHolder(holder: ProfileHolder, position: Int, model: Profile) {
//        holder.profileName.text = "First Name: ${model.firstName}"
//        holder.profileAge.text = "Phone Number: ${model.phone}"
//        holder.profileImage.setImageResource(R.drawable.ic_launcher_background)
//
//    }

    interface OnProfileSelectedListener{
        fun onProfileSelected(snapshot: DocumentSnapshot, newReference: Boolean)
    }
}