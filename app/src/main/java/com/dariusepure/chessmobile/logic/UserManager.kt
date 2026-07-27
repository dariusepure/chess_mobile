package com.dariusepure.chessmobile.logic

import android.content.Context
import android.content.SharedPreferences

object UserManager {
    private const val PREFS_NAME = "chess_prefs"
    private const val USERS_KEY = "users_list"
    private const val CURRENT_USER_KEY = "current_user_email"

    private val users = mutableListOf<User>(
        User("admin@chess.com", "admin")
    )
    
    var currentUser: User? = null
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val usersData = prefs.getString(USERS_KEY, "") ?: ""
        if (usersData.isNotEmpty()) {
            usersData.split(";").forEach { userData ->
                val parts = userData.split("|")
                if (parts.size >= 6) {
                    val user = User(
                        parts[0], parts[1], 
                        parts[2].toIntOrNull() ?: 0, 
                        parts[3].toIntOrNull() ?: 0, 
                        parts[4].toIntOrNull() ?: 0, 
                        parts[5].toIntOrNull() ?: 0
                    )
                    if (users.none { it.email == user.email }) {
                        users.add(user)
                    }
                }
            }
        }
        
        val lastUserEmail = prefs.getString(CURRENT_USER_KEY, null)
        if (lastUserEmail != null) {
            currentUser = users.find { it.email == lastUserEmail }
        }
    }

    private fun saveUsers(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val usersData = users.joinToString(";") { 
            "${it.email}|${it.password}|${it.wins}|${it.losses}|${it.draws}|${it.points}" 
        }
        prefs.edit().putString(USERS_KEY, usersData).apply()
    }

    fun login(context: Context, email: String, password: String): Boolean {
        val user = users.find { it.email == email && it.password == password }
        if (user != null) {
            currentUser = user
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(CURRENT_USER_KEY, email).apply()
            return true
        }
        return false
    }

    fun register(context: Context, email: String, password: String): Boolean {
        if (users.any { it.email == email }) return false
        val newUser = User(email, password)
        users.add(newUser)
        currentUser = newUser
        saveUsers(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(CURRENT_USER_KEY, email).apply()
        return true
    }

    fun logout(context: Context) {
        currentUser = null
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(CURRENT_USER_KEY).apply()
    }

    fun updateUser(context: Context, user: User) {
        val index = users.indexOfFirst { it.email == user.email }
        if (index != -1) {
            users[index] = user
            saveUsers(context)
        }
    }
}
