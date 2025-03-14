package com.example.meditrack.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.meditrack.R

class HistoryAdapter(context: Context, private val items: List<String>) :
    ArrayAdapter<String>(context, 0, items) {

    override fun getCount(): Int {
        return items.size + 1
    }

    override fun getItem(position: Int): String? {

        return if (position < items.size) items[position] else null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        return if (position < items.size) {

            val view = convertView ?: inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = items[position]

            textView.setTextColor(ContextCompat.getColor(context,R.color.handles))
            view.setBackgroundColor(Color.WHITE)
            view
        } else {

            val view = convertView ?: inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = "Очистить историю"

            textView.setTextColor(ContextCompat.getColor(context, R.color.red))
            view.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
            view
        }
    }
}
