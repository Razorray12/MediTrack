package com.example.meditrack.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meditrack.adapters.ChatRecyclerViewAdapter
import com.example.meditrack.databinding.FragmentChatBinding
import com.example.meditrack.holders.ChatMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()
    private lateinit var chatAdapter: ChatRecyclerViewAdapter

    private var webSocket: WebSocket? = null
    private var userFio: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAdapter = ChatRecyclerViewAdapter(mutableListOf())
        binding.recyclerChat.layoutManager = LinearLayoutManager(context)
        binding.recyclerChat.adapter = chatAdapter

        loadChatHistory()

        binding.btnSend.setOnClickListener {
            val text = binding.editMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                webSocket?.send(text)

                val msg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderName = userFio ?: "Me",
                    content = text,
                    timestamp = getFormattedTime(),
                    isSentByMe = true
                )
                chatAdapter.addMessage(msg)

                binding.editMessage.setText("")
                binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        val prefs = requireActivity().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        userFio = prefs.getString("fio", null)

        connectToWebSocket()
    }

    override fun onPause() {
        super.onPause()
        webSocket?.close(1000, "Fragment paused")
        webSocket = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadChatHistory() {
        val prefs = requireActivity().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null) ?: return

        val url = "http://192.168.0.159:8080/chat/history"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) return

                    val bodyStr = response.body?.string() ?: return

                    val jsonArray = org.json.JSONArray(bodyStr)
                    val messages = mutableListOf<ChatMessage>()

                    val currentUserId = prefs.getString("user_id", "") ?: ""

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val senderName = obj.optString("sender", "")
                        val senderId = obj.optString("senderId", "")
                        val message = obj.optString("message", "")
                        val timestamp = obj.optString("timestamp", "")
                        val isMe = (senderId == currentUserId)

                        messages.add(
                            ChatMessage(
                                id = UUID.randomUUID().toString(),
                                senderName = senderName,
                                content = message,
                                timestamp = timestamp,
                                isSentByMe = isMe
                            )
                        )
                    }

                    requireActivity().runOnUiThread {
                        chatAdapter.setMessages(messages)
                        binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }
            }
        })
    }

    private fun connectToWebSocket() {
        val prefs = requireActivity().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)

        if (token.isNullOrEmpty()) {
            return
        }

        val wsUrl = "ws://192.168.0.159:8080/chat"

        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
            }

            override fun onMessage(webSocket: WebSocket, text: String) {

                val dateStr = getFormattedTime()
                val obj = JSONObject(text)
                val senderId = obj.optString("senderId", "")
                val senderName = obj.optString("senderName", "")
                val content = obj.optString("message", "")

                val prefs = requireActivity().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                val currentUserId = prefs.getString("user_id", "") ?: ""

                val isMe = (senderId == currentUserId)

                val newMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderName = senderName,
                    content = content,
                    timestamp = dateStr,
                    isSentByMe = isMe
                )

                requireActivity().runOnUiThread {
                    chatAdapter.addMessage(newMsg)
                    binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            }
        })
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

}
