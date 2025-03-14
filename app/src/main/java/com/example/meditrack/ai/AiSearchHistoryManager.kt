package com.example.meditrack.ai

import android.content.Context

class AiSearchHistoryManager(context: Context) {

    private val prefs = context.getSharedPreferences("ai_search_history", Context.MODE_PRIVATE)
    private val KEY_HISTORY = "ai_history"
    private val MAX_HISTORY_SIZE = 5
    private val DELIMITER = ";;"

    fun getHistory(): List<String> {
        val saved = prefs.getString(KEY_HISTORY, "") ?: ""
        if (saved.isBlank()) return emptyList()
        return saved.split(DELIMITER)
    }

    private fun saveHistory(historyList: List<String>) {
        val joined = historyList.joinToString(DELIMITER)
        prefs.edit().putString(KEY_HISTORY, joined).apply()
    }

    fun addQuery(query: String) {
        if (query.isBlank()) return
        val currentHistory = getHistory().toMutableList()

        currentHistory.remove(query)

        currentHistory.add(0, query)

        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.subList(MAX_HISTORY_SIZE, currentHistory.size).clear()
        }
        saveHistory(currentHistory)
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
