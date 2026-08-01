package com.dariusepure.chessmobile.logic

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreGameRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveGame(uid: String, history: List<Move>, playerColor: Colors, vsComputer: Boolean) {
        val historyStr = history.joinToString(";") { "${it.from}|${it.to}|${it.promotedType ?: ' '}" }
        val data = mapOf(
            "history" to historyStr,
            "playerColor" to playerColor.name,
            "vsComputer" to vsComputer
        )
        try {
            db.collection("saved_games").document(uid).set(data).await()
        } catch (e: Exception) {}
    }

    suspend fun loadGame(uid: String): Triple<List<Triple<Position, Position, Char?>>, Colors, Boolean>? {
        return try {
            val doc = db.collection("saved_games").document(uid).get().await()
            if (!doc.exists()) return null
            
            val historyStr = doc.getString("history") ?: ""
            val playerColor = Colors.valueOf(doc.getString("playerColor") ?: "WHITE")
            val vsComputer = doc.getBoolean("vsComputer") ?: true
            
            val history = if (historyStr.isEmpty()) emptyList() else historyStr.split(";").map { moveStr ->
                val parts = moveStr.split("|")
                val from = Position.fromString(parts[0])!!
                val to = Position.fromString(parts[1])!!
                val promo = if (parts[2][0] == ' ') null else parts[2][0]
                Triple(from, to, promo)
            }
            
            Triple(history, playerColor, vsComputer)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun clearSavedGame(uid: String) {
        try {
            db.collection("saved_games").document(uid).delete().await()
        } catch (e: Exception) {}
    }
}
