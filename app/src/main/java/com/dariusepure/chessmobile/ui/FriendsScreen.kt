package com.dariusepure.chessmobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dariusepure.chessmobile.logic.Colors
import com.dariusepure.chessmobile.logic.FirestoreUser
import com.dariusepure.chessmobile.logic.UserManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(onBack: () -> Unit, onStartOnlineGame: (String, Colors) -> Unit) {
    val scope = rememberCoroutineScope()
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var friendUsername by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf<List<FirestoreUser>>(emptyList()) }
    
    val currentUser = UserManager.currentUser

    LaunchedEffect(currentUser?.friends) {
        friends = UserManager.getAllUsers().filter { currentUser?.friends?.contains(it.uid) == true }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        showAddFriendDialog = true 
                        errorMsg = ""
                        friendUsername = ""
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Friend")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (friends.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No friends yet. Add some!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(friends) { friend ->
                        FriendItem(friend = friend, onChallenge = {
                            scope.launch {
                                val matchId = UserManager.createMatch(friend.uid, true)
                                if (matchId != null) {
                                    onStartOnlineGame(matchId, Colors.WHITE)
                                }
                            }
                        })
                    }
                }
            }
        }

        if (showAddFriendDialog) {
            AlertDialog(
                onDismissRequest = { showAddFriendDialog = false },
                title = { Text("Add Friend") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = friendUsername,
                            onValueChange = { friendUsername = it },
                            label = { Text("Enter Username") },
                            isError = errorMsg.isNotEmpty()
                        )
                        if (errorMsg.isNotEmpty()) {
                            Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { 
                        scope.launch {
                            val friend = UserManager.findUserByUsername(friendUsername)
                            if (friend == null) {
                                errorMsg = "User not found"
                            } else if (friend.uid == UserManager.currentUser?.uid) {
                                errorMsg = "Cannot add yourself"
                            } else if (UserManager.currentUser?.friends?.contains(friend.uid) == true) {
                                errorMsg = "Already friends"
                            } else {
                                UserManager.addFriend(friend.uid)
                                showAddFriendDialog = false 
                            }
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddFriendDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun FriendItem(friend: FirestoreUser, onChallenge: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3E3C39))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF769656))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = friend.username, color = Color.White, fontSize = 18.sp)
                Text(text = "${friend.points} pts", color = Color.Gray, fontSize = 14.sp)
            }
            Button(onClick = onChallenge) {
                Text("Challenge")
            }
        }
    }
}
