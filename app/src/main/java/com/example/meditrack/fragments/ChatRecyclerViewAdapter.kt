package com.example.meditrack.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.databinding.ItemChatMessageReceivedBinding
import com.example.meditrack.databinding.ItemChatMessageSentBinding
import com.example.myapp.placeholder.ChatMessage

class ChatRecyclerViewAdapter(
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<ChatRecyclerViewAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 0
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.isSentByMe) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemChatMessageSentBinding.inflate(inflater, parent, false)
            MessageViewHolder.SentViewHolder(binding)
        } else {
            val binding = ItemChatMessageReceivedBinding.inflate(inflater, parent, false)
            MessageViewHolder.ReceivedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    sealed class MessageViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(message: ChatMessage)

        class SentViewHolder(private val binding: ItemChatMessageSentBinding) : MessageViewHolder(binding.root) {
            override fun bind(message: ChatMessage) {
                binding.tvMessage.text = message.content
                binding.tvTimestamp.text = message.timestamp
            }
        }

        class ReceivedViewHolder(private val binding: ItemChatMessageReceivedBinding) : MessageViewHolder(binding.root) {
            override fun bind(message: ChatMessage) {
                binding.tvSenderName.text = message.senderName
                binding.tvMessage.text = message.content
                binding.tvTimestamp.text = message.timestamp
            }
        }
    }
}
