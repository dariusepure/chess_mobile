package com.dariusepure.chessmobile.logic

import android.content.Context

object GameRepository {
    private const val PREFS_NAME = "game_prefs"
    private const val HISTORY_KEY = "game_history"
    private const val SETTINGS_KEY = "game_settings"

    fun saveGame(context: Context, history: List<Move>, playerColor: Colors, vsComputer: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyStr = history.joinToString(";") { "${it.from}|${it.to}|${it.promotedType ?: ' '}" }
        prefs.edit()
            .putString(HISTORY_KEY, historyStr)
            .putString(SETTINGS_KEY, "${playerColor.name}|${vsComputer}")
            .apply()
    }

    fun loadGame(context: Context): Triple<List<Triple<Position, Position, Char?>>, Colors, Boolean>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyStr = prefs.getString(HISTORY_KEY, null) ?: return null
        val settingsStr = prefs.getString(SETTINGS_KEY, null) ?: return null
        
        try {
            val history = if (historyStr.isEmpty()) emptyList() else historyStr.split(";").map { moveStr ->
                val parts = moveStr.split("|")
                val from = Position.fromString(parts[0])!!
                val to = Position.fromString(parts[1])!!
                val promo = if (parts[2][0] == ' ') null else parts[2][0]
                Triple(from, to, promo)
            }
            
            val settingsParts = settingsStr.split("|")
            val color = Colors.valueOf(settingsParts[0])
            val vsComp = settingsParts[1].toBoolean()
            
            return Triple(history, color, vsComp)
        } catch (e: Exception) {
            return null
        }
    }
    
    fun clearSavedGame(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
