package com.dariusepure.chessmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dariusepure.chessmobile.logic.Colors
import com.dariusepure.chessmobile.logic.Difficulty
import com.dariusepure.chessmobile.logic.Position
import com.dariusepure.chessmobile.logic.UserManager
import com.dariusepure.chessmobile.pieces.Piece
import com.dariusepure.chessmobile.ui.*
import com.dariusepure.chessmobile.ui.theme.ChessMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UserManager.init(this)
        
        setContent {
            val viewModel: ChessViewModel = viewModel()
            
            ChessMobileTheme(themeMode = viewModel.themeMode) {
                val navController = rememberNavController()
                val startDest = if (UserManager.currentUser != null) "main_menu" else "login"
                
                NavHost(navController = navController, startDestination = startDest) {
                    composable("login") {
                        LoginScreen(onLoginSuccess = {
                            navController.navigate("main_menu") {
                                popUpTo("login") { inclusive = true }
                            }
                        })
                    }
                    composable("main_menu") {
                        MainMenuScreen(
                            onStartGame = { color, vsComputer, difficulty ->
                                viewModel.startGame(color, vsComputer, difficulty)
                                navController.navigate("game")
                            },
                            onResumeGame = {
                                viewModel.resumeGame()
                                navController.navigate("game")
                            },
                            hasSavedGame = viewModel.hasSavedGame(),
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("main_menu") { inclusive = true }
                                }
                            },
                            onLeaderboard = { navController.navigate("leaderboard") },
                            onFriends = { navController.navigate("friends") },
                            currentTheme = viewModel.currentTheme,
                            onThemeSelect = { viewModel.setTheme(it) },
                            themeMode = viewModel.themeMode,
                            onThemeModeSelect = { viewModel.updateThemeMode(it) }
                        )
                    }
                    composable("leaderboard") {
                        LeaderboardScreen(onBack = { navController.popBackStack() })
                    }
                    composable("friends") {
                        FriendsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("game") {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = Color(0xFF302E2B)
                        ) { innerPadding ->
                            ChessGameScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                onQuit = {
                                    navController.navigate("main_menu") {
                                        popUpTo("game") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChessGameScreen(
    modifier: Modifier = Modifier,
    viewModel: ChessViewModel = viewModel(),
    onQuit: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.gameStatus,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = onQuit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Quit", color = MaterialTheme.colorScheme.onSecondary, fontSize = 14.sp)
            }
        }

        GamePlayerInfo(
            name = if (viewModel.currentPlayer == Colors.WHITE) "Opponent" else "You",
            time = if (viewModel.currentPlayer == Colors.WHITE) viewModel.blackTime else viewModel.whiteTime,
            isCurrent = viewModel.currentPlayer == Colors.BLACK
        )

        Spacer(modifier = Modifier.height(8.dp))
        CapturedPiecesRow(pieces = viewModel.capturedByBlack)

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EvaluationBar(evaluation = viewModel.evaluation)
            Spacer(modifier = Modifier.width(8.dp))
            ChessBoard(
                boardState = viewModel.boardState,
                selectedPosition = viewModel.selectedPosition,
                validMoves = viewModel.validMoves,
                onSquareClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onSquareClick(it) 
                },
                theme = viewModel.currentTheme,
                lastMove = viewModel.lastMove
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        CapturedPiecesRow(pieces = viewModel.capturedByWhite)

        Spacer(modifier = Modifier.height(8.dp))
        GamePlayerInfo(
            name = "You",
            time = if (viewModel.currentPlayer == Colors.WHITE) viewModel.whiteTime else viewModel.blackTime,
            isCurrent = viewModel.currentPlayer == Colors.WHITE
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        HistoryRow(history = viewModel.getFormattedHistory())

        if (viewModel.showPromotionDialog) {
            PawnPromotionDialog(
                color = viewModel.currentPlayer,
                onSelect = { viewModel.onPromote(it) }
            )
        }
    }
}

@Composable
fun EvaluationBar(evaluation: Float) {
    val maxEval = 15f
    val normalizedEval = (evaluation / maxEval).coerceIn(-1f, 1f)
    val whitePercentage = (normalizedEval + 1f) / 2f

    Column(
        modifier = Modifier
            .width(12.dp)
            .height(280.dp)
            .background(Color.Black, shape = MaterialTheme.shapes.extraSmall)
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.extraSmall)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight((1f - whitePercentage).coerceAtLeast(0.01f))
                .background(Color.DarkGray)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(whitePercentage.coerceAtLeast(0.01f))
                .background(Color.White)
        )
    }
}

@Composable
fun GamePlayerInfo(name: String, time: Int, isCurrent: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, color = Color.White, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
        Text(
            text = formatTime(time),
            color = if (time < 30) Color.Red else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color(0xFF3E3C39), shape = MaterialTheme.shapes.small)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CapturedPiecesRow(pieces: List<Piece>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        pieces.forEach { piece ->
            Text(
                text = getPieceSymbol(piece),
                fontSize = 20.sp,
                color = if (piece.color == Colors.WHITE) Color.White else Color(0xFF1E1E1E),
                modifier = Modifier.padding(horizontal = 1.dp)
            )
        }
    }
}

@Composable
fun HistoryRow(history: List<String>) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFF3E3C39))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(history.size) { index ->
            Text(text = history[index], color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun ChessBoard(
    boardState: Map<Position, Piece>,
    selectedPosition: Position?,
    validMoves: List<Position>,
    onSquareClick: (Position) -> Unit,
    theme: com.dariusepure.chessmobile.ui.BoardTheme,
    lastMove: com.dariusepure.chessmobile.logic.Move?
) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        for (row in 8 downTo 1) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 'A'..'H') {
                    val position = Position(col, row)
                    ChessSquare(
                        position = position,
                        piece = boardState[position],
                        isSelected = position == selectedPosition,
                        isValidMove = position in validMoves,
                        isLastMove = position == lastMove?.from || position == lastMove?.to,
                        theme = theme,
                        modifier = Modifier.weight(1f),
                        onClick = { onSquareClick(position) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChessSquare(
    position: Position,
    piece: Piece?,
    isSelected: Boolean,
    isValidMove: Boolean,
    isLastMove: Boolean,
    theme: com.dariusepure.chessmobile.ui.BoardTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isLightSquare = (position.x.code + position.y) % 2 != 0
    val baseColor = if (isLightSquare) theme.lightSquare else theme.darkSquare
    
    val backgroundColor = when {
        isSelected -> Color(0xFFF6F669)
        isValidMove -> if (piece != null) Color(0xFFFF7B7B) else Color(0xFFF6F669).copy(alpha = 0.5f)
        isLastMove -> Color(0xFFF6F669).copy(alpha = 0.4f)
        else -> baseColor
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (piece != null) {
            Text(
                text = getPieceSymbol(piece),
                fontSize = 32.sp,
                color = if (piece.color == Colors.WHITE) Color.White else Color(0xFF1E1E1E)
            )
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

fun getPieceSymbol(piece: Piece): String {
    return when (piece.type) {
        'K' -> if (piece.color == Colors.WHITE) "♔" else "♚"
        'Q' -> if (piece.color == Colors.WHITE) "♕" else "♛"
        'R' -> if (piece.color == Colors.WHITE) "♖" else "♜"
        'B' -> if (piece.color == Colors.WHITE) "♗" else "♝"
        'N' -> if (piece.color == Colors.WHITE) "♘" else "♞"
        'P' -> if (piece.color == Colors.WHITE) "♙" else "♟"
        else -> ""
    }
}
