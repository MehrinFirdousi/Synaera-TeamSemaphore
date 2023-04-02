package com.example.synaera

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.synaera.databinding.ChatItemBinding

open class ChatRecyclerAdapter(var items: ArrayList<ChatBubble>, val listener : (ChatBubble, Int) -> Unit) :
    RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder>() {

    private lateinit var context : Context

    inner class ViewHolder(b: ChatItemBinding) : RecyclerView.ViewHolder(b.root) {
        var binding = b
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context

        return ViewHolder(
            ChatItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatParams = holder.binding.chatItemText.layoutParams as ConstraintLayout.LayoutParams
        val editParams = holder.binding.editBttn.layoutParams as ConstraintLayout.LayoutParams

        val currItem = items[position]
        holder.binding.chatItemText.text = currItem.text

        holder.binding.chatItemText.setOnClickListener { listener(currItem, position) }
        holder.binding.editBttn.setOnClickListener{ listener(currItem, position) }

        if (currItem.sender) {
            chatParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            chatParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
            holder.binding.chatItemText.setBackgroundResource(R.drawable.other_message_bubble)
            editParams.startToEnd = holder.binding.chatItemText.id
            editParams.endToStart = ConstraintLayout.LayoutParams.UNSET
//            holder.binding.chatItemText.setBackgroundColor(ContextCompat.getColor(context, R.color.charcoal))
        } else {
            chatParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            chatParams.startToStart = ConstraintLayout.LayoutParams.UNSET
            holder.binding.chatItemText.setBackgroundResource(R.drawable.my_message_bubble)
            editParams.endToStart = holder.binding.chatItemText.id
            editParams.startToEnd = ConstraintLayout.LayoutParams.UNSET
        }

        holder.binding.chatItemText.post {
            holder.binding.chatItemText.maxWidth = (holder.binding.chatItemLayout.width * 0.8).toInt()
        }
    }

}