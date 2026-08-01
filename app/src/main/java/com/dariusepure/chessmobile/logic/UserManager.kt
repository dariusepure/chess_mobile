package com.dariusepure.chessmobile.logic

import android.content.Context
import android.content.SharedPreferences

object UserManager {
    private const val PREFS_NAME = "chess_prefs"
    private const val USERS_KEY = "users_list"
    private const val CURRENT_USER_KEY = "current_user_email"
    private const val THEME_MODE_KEY = "theme_mode"

    private val users = mutableListOf<User>(
        User("admin", "Admin", "admin@chess.com", "admin")
    )
    
    var currentUser: User? = null
        private set

    fun getThemeMode(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(THEME_MODE_KEY, 0) // 0 = System, 1 = Light, 2 = Dark
    }

    fun setThemeMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(THEME_MODE_KEY, mode).apply()
    }

    fun getAllUsers(): List<User> = users.toList()

    fun findUserByUsername(username: String): User? = users.find { it.username.equals(username, ignoreCase = true) }

    fun addFriend(context: Context, friendUid: String): Boolean {
        val user = currentUser ?: return false
        if (user.uid == friendUid) return false
        if (user.friends.contains(friendUid)) return false
        
        val updatedUser = user.copy(friends = user.friends + friendUid)
        updateUser(context, updatedUser)
        currentUser = updatedUser
        return true
    }

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val usersData = prefs.getString(USERS_KEY, "") ?: ""
        if (usersData.isNotEmpty()) {
            usersData.split(";").forEach { userData ->
                val parts = userData.split("|")
                if (parts.size >= 8) {
                    val user = User(
                        uid = parts[0],
                        username = parts[1],
                        email = parts[2],
                        password = parts[3],
                        wins = parts[4].toIntOrNull() ?: 0,
                        losses = parts[5].toIntOrNull() ?: 0,
                        draws = parts[6].toIntOrNull() ?: 0,
                        points = parts[7].toIntOrNull() ?: 0,
                        friends = if (parts.size > 8 && parts[8].isNotEmpty()) parts[8].split(",") else emptyList()
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
            "${it.uid}|${it.username}|${it.email}|${it.password}|${it.wins}|${it.losses}|${it.draws}|${it.points}|${it.friends.joinToString(",")}" 
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

    fun register(context: Context, email: String, password: String, username: String): Boolean {
        if (users.any { it.email == email || it.username == username }) return false
        
        val newUser = User(
            uid = java.util.UUID.randomUUID().toString(),
            username = username,
            email = email,
            password = password
        )
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
