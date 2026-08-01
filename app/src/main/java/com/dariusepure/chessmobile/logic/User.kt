package com.dariusepure.chessmobile.logic

data class User(
    val uid: String,
    val username: String,
    val email: String,
    val password: String, // Still used for local fallback, will be replaced by Firebase Auth
    var wins: Int = 0,
    var losses: Int = 0,
    var draws: Int = 0,
    var points: Int = 0,
    val friends: List<String> = emptyList() // List of friend UIDs
)
