package com.dariusepure.chessmobile.logic

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

object UserManager {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { 
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        firestore.firestoreSettings = settings
        firestore
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    var currentUser: FirestoreUser? = null
        private set

    var isReady = false
        private set

    fun init(context: Context, onReady: () -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            scope.launch {
                fetchUserProfile(firebaseUser.uid)
                isReady = true
                withContext(Dispatchers.Main) { onReady() }
            }
        } else {
            isReady = true
            onReady()
        }
    }

    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                fetchUserProfile(result.user!!.uid)
                Result.success(true)
            } else Result.failure(Exception("Login failed: User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, username: String): Result<Boolean> {
        return try {
            // 1. Check if username exists
            val usernameQuery = db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            
            if (!usernameQuery.isEmpty) {
                return Result.failure(Exception("Username '$username' is already taken"))
            }

            // 2. Create Auth user
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Account creation failed"))

            // 3. Create Firestore profile
            val newUser = FirestoreUser(
                uid = firebaseUser.uid,
                username = username,
                email = email
            )
            db.collection("users").document(firebaseUser.uid).set(newUser).await()
            
            currentUser = newUser
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(uid: String) {
        try {
            val doc = db.collection("users").document(uid).get().await()
            currentUser = doc.toObject(FirestoreUser::class.java)
        } catch (e: Exception) {
            currentUser = null
        }
    }

    fun logout() {
        auth.signOut()
        currentUser = null
    }

    suspend fun signInWithGoogle(context: Context): Result<Boolean> {
        val credentialManager = CredentialManager.create(context)
        
        val webClientId = "748472212284-ns1r9mo0dhgi86slpuqcat1dubbr2n4u.apps.googleusercontent.com" 

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            
            android.util.Log.d("UserManager", "Credential type: ${credential.type}")
            
            if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                return Result.failure(Exception("Unexpected credential type: ${credential.type}"))
            }

            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
            android.util.Log.d("UserManager", "ID Token: ${googleIdToken.idToken.take(10)}...")
            
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)
            
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Firebase user is null"))
            
            val doc = db.collection("users").document(firebaseUser.uid).get().await()
            if (!doc.exists()) {
                val newUser = FirestoreUser(
                    uid = firebaseUser.uid,
                    username = firebaseUser.displayName ?: "User_${firebaseUser.uid.take(5)}",
                    email = firebaseUser.email ?: ""
                )
                db.collection("users").document(firebaseUser.uid).set(newUser).await()
                currentUser = newUser
            } else {
                currentUser = doc.toObject(FirestoreUser::class.java)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): List<FirestoreUser> {
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.toObjects(FirestoreUser::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findUserByUsername(username: String): FirestoreUser? {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            snapshot.toObjects(FirestoreUser::class.java).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addFriend(friendUid: String): Boolean {
        val user = currentUser ?: return false
        if (user.uid == friendUid) return false
        if (user.friends.contains(friendUid)) return false
        
        val updatedFriends = user.friends + friendUid
        return try {
            db.collection("users").document(user.uid)
                .update("friends", updatedFriends)
                .await()
            currentUser = user.copy(friends = updatedFriends)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createMatch(opponentId: String, playerIsWhite: Boolean): String? {
        val uid = currentUser?.uid ?: return null
        val match = FirestoreMatch(
            matchId = java.util.UUID.randomUUID().toString(),
            whitePlayerId = if (playerIsWhite) uid else opponentId,
            blackPlayerId = if (playerIsWhite) opponentId else uid,
            status = FirestoreGameStatus.ACTIVE
        )
        return try {
            db.collection("active_matches").document(match.matchId).set(match).await()
            match.matchId
        } catch (e: Exception) {
            null
        }
    }

    fun listenToMatches(onUpdate: (List<FirestoreMatch>) -> Unit): List<ListenerRegistration> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        
        val registrations = mutableListOf<ListenerRegistration>()
        
        // Use auth.currentUser directly to be sure we have the latest ID
        val whiteListener = db.collection("active_matches")
            .whereEqualTo("whitePlayerId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error == null) onUpdate(snapshot?.toObjects(FirestoreMatch::class.java) ?: emptyList())
            }
            
        registrations.add(whiteListener)
        return registrations
    }

    suspend fun sendMove(matchId: String, moveStr: String, moves: List<String>) {
        try {
            db.collection("active_matches").document(matchId)
                .update(
                    "moves", moves + moveStr,
                    "lastMoveTime", System.currentTimeMillis()
                ).await()
        } catch (e: Exception) {}
    }

    suspend fun finishMatch(matchId: String, winnerId: String?, draw: Boolean = false) {
        try {
            val matchDoc = db.collection("active_matches").document(matchId).get().await()
            val match = matchDoc.toObject(FirestoreMatch::class.java) ?: return

            val batch = db.batch()
            
            batch.update(db.collection("active_matches").document(matchId), mapOf(
                "status" to FirestoreGameStatus.FINISHED,
                "winnerId" to winnerId
            ))

            val whiteRef = db.collection("users").document(match.whitePlayerId)
            val blackRef = db.collection("users").document(match.blackPlayerId)

            if (draw) {
                batch.update(whiteRef, "points", com.google.firebase.firestore.FieldValue.increment(5))
                batch.update(whiteRef, "draws", com.google.firebase.firestore.FieldValue.increment(1))
                batch.update(blackRef, "points", com.google.firebase.firestore.FieldValue.increment(5))
                batch.update(blackRef, "draws", com.google.firebase.firestore.FieldValue.increment(1))
            } else if (winnerId != null) {
                val winnerRef = if (winnerId == match.whitePlayerId) whiteRef else blackRef
                val loserRef = if (winnerId == match.whitePlayerId) blackRef else whiteRef
                
                batch.update(winnerRef, "points", com.google.firebase.firestore.FieldValue.increment(20))
                batch.update(winnerRef, "wins", com.google.firebase.firestore.FieldValue.increment(1))
                batch.update(loserRef, "losses", com.google.firebase.firestore.FieldValue.increment(1))
            }

            batch.commit().await()
            
            db.collection("match_history").add(match.copy(status = FirestoreGameStatus.FINISHED, winnerId = winnerId)).await()
            db.collection("active_matches").document(matchId).delete().await()
        } catch (e: Exception) {}
    }

    suspend fun updateUser(user: FirestoreUser) {
        try {
            db.collection("users").document(user.uid).set(user).await()
            if (currentUser?.uid == user.uid) {
                currentUser = user
            }
        } catch (e: Exception) {}
    }

    fun getThemeMode(context: Context): Int {
        return context.getSharedPreferences("chess_prefs", Context.MODE_PRIVATE)
            .getInt("theme_mode", 0)
    }

    fun setThemeMode(context: Context, mode: Int) {
        context.getSharedPreferences("chess_prefs", Context.MODE_PRIVATE)
            .edit().putInt("theme_mode", mode).apply()
    }

    fun exportLocalBackup(context: Context): Boolean {
        val user = currentUser ?: return false
        return try {
            val json = org.json.JSONObject().apply {
                put("uid", user.uid)
                put("username", user.username)
                put("email", user.email)
                put("points", user.points)
                put("wins", user.wins)
                put("losses", user.losses)
                put("draws", user.draws)
            }
            val file = java.io.File(context.filesDir, "chess_backup.json")
            file.writeText(json.toString(2))
            true
        } catch (e: Exception) {
            false
        }
    }
}
