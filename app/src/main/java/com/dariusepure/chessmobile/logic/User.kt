package com.dariusepure.chessmobile.logic

data class User(
    val email: String,
    val password: String,
    var wins: Int = 0,
    var losses: Int = 0,
    var draws: Int = 0,
    var points: Int = 0
)
