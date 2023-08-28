package com.synervoz.voicecommunicationapp.ui.room

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.synervoz.voicecommunicationapp.R

class UserListAdapter(userIds: List<String>) : RecyclerView.Adapter<UserListAdapter.ItemViewHolder>() {

    var userIds: List<String> = userIds
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userIdTextView: TextView = itemView.findViewById(R.id.user_id)
        fun bind(userId: String) {
            userIdTextView.text = userId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userIds.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(userIds[position])
    }
}