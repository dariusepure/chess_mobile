package com.dariusepure.chessmobile.logic

data class FirestoreUser(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    var wins: Int = 0,
    var losses: Int = 0,
    var draws: Int = 0,
    var points: Int = 0,
    val friends: List<String> = emptyList()
)
