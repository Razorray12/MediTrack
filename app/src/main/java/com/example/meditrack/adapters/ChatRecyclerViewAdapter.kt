package com.example.meditrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.holders.ChatMessage

private const val VIEW_TYPE_RECEIVED = 0
private const val VIEW_TYPE_SENT = 1

class ChatRecyclerViewAdapter(private val messages: MutableList<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSender: TextView = itemView.findViewById(R.id.tv_sender_name)
        val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSentByMe) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_message_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_message_received, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentViewHolder) {
            holder.tvMessage.text = message.content
            holder.tvTimestamp.text = message.timestamp
        } else if (holder is ReceivedViewHolder) {
            holder.tvSender.text = message.senderName
            holder.tvMessage.text = message.content
            holder.tvTimestamp.text = message.timestamp
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(newMessage: ChatMessage) {
        messages.add(newMessage)
        notifyItemInserted(messages.size - 1)
    }

    fun setMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }
}