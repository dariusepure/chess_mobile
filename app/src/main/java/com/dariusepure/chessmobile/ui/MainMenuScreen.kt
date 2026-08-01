package com.dariusepure.chessmobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dariusepure.chessmobile.logic.Colors
import com.dariusepure.chessmobile.logic.Difficulty
import com.dariusepure.chessmobile.logic.FirestoreMatch
import com.dariusepure.chessmobile.logic.UserManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onStartGame: (Colors, Boolean, Difficulty) -> Unit,
    onResumeGame: () -> Unit,
    hasSavedGame: Boolean,
    onLogout: () -> Unit,
    onLeaderboard: () -> Unit,
    onFriends: () -> Unit,
    onExportBackup: () -> Unit,
    activeMatches: List<FirestoreMatch>,
    onJoinMatch: (FirestoreMatch) -> Unit,
    currentTheme: BoardTheme,
    onThemeSelect: (BoardTheme) -> Unit
) {
    val context = LocalContext.current
    var selectedColor by remember { mutableStateOf(Colors.WHITE) }
    var vsComputer by remember { mutableStateOf(true) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF302E2B)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hello, ${UserManager.currentUser?.username ?: "Player"}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "ID: ${UserManager.currentUser?.uid?.take(8)}...", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(24.dp))
            
            if (activeMatches.isNotEmpty()) {
                Text(text = "Online Games", color = Color.White, fontWeight = FontWeight.Bold)
                activeMatches.forEach { match ->
                    val isMyTurn = if (match.moves.size % 2 == 0) {
                        match.whitePlayerId == UserManager.currentUser?.uid
                    } else {
                        match.blackPlayerId == UserManager.currentUser?.uid
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onJoinMatch(match) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3E3C39))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Game vs Opponent", color = Color.White)
                            if (isMyTurn) {
                                Badge(containerColor = Color(0xFF769656)) { Text("YOUR TURN", color = Color.White) }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3E3C39))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Game Settings", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = "Your Side", color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedColor == Colors.WHITE,
                            onClick = { selectedColor = Colors.WHITE },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF769656))
                        )
                        Text("White", color = Color.White)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = selectedColor == Colors.BLACK,
                            onClick = { selectedColor = Colors.BLACK },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF769656))
                        )
                        Text("Black", color = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = vsComputer,
                            onCheckedChange = { vsComputer = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF769656))
                        )
                        Text("vs Computer AI", color = Color.White)
                    }

                    if (vsComputer) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Difficulty", color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Difficulty.entries.forEach { diff: Difficulty ->
                                FilterChip(
                                    selected = selectedDifficulty == diff,
                                    onClick = { selectedDifficulty = diff },
                                    label = { Text(diff.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF769656),
                                        labelColor = Color.White,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Board Theme", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AllThemes.forEach { theme ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(theme.darkSquare)
                                    .border(
                                        width = 2.dp,
                                        color = if (currentTheme == theme) Color.White else Color.Transparent
                                    )
                                    .clickable { onThemeSelect(theme) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onStartGame(selectedColor, vsComputer, selectedDifficulty) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF769656))
            ) {
                Text("New Game", fontSize = 18.sp, color = Color.White)
            }
            
            if (hasSavedGame) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onResumeGame,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF769656))
                ) {
                    Text("Resume Last Game", fontSize = 18.sp, color = Color(0xFF769656))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { 
                UserManager.logout()
                onLogout()
            }) {
                Text("Logout", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = onExportBackup) {
                Text("Create Local Backup", color = Color(0xFF769656), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onLeaderboard) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard", tint = Color(0xFFFFD700))
                    }
                    Text("Rank", color = Color.White, fontSize = 12.sp)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onFriends) {
                        Icon(Icons.Default.People, contentDescription = "Friends", tint = Color(0xFF769656))
                    }
                    Text("Friends", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
