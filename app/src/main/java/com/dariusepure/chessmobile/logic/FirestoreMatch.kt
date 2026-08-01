package com.dariusepure.chessmobile.logic

enum class FirestoreGameStatus {
    WAITING, ACTIVE, FINISHED
}

data class FirestoreMatch(
    val matchId: String = "",
    val whitePlayerId: String = "",
    val blackPlayerId: String = "",
    val moves: List<String> = emptyList(), // Store moves as "from|to|promo" strings
    val status: FirestoreGameStatus = FirestoreGameStatus.WAITING,
    val winnerId: String? = null,
    val lastMoveTime: Long = System.currentTimeMillis()
)
