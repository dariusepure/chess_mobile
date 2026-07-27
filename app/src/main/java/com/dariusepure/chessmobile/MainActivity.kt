package com.dariusepure.chessmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dariusepure.chessmobile.logic.Colors
import com.dariusepure.chessmobile.logic.Position
import com.dariusepure.chessmobile.logic.UserManager
import com.dariusepure.chessmobile.pieces.Piece
import com.dariusepure.chessmobile.ui.ChessViewModel
import com.dariusepure.chessmobile.ui.LoginScreen
import com.dariusepure.chessmobile.ui.MainMenuScreen
import com.dariusepure.chessmobile.ui.PawnPromotionDialog
import com.dariusepure.chessmobile.ui.theme.ChessMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UserManager.init(this)
        
        setContent {
            ChessMobileTheme {
                val navController = rememberNavController()
                val viewModel: ChessViewModel = viewModel()
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
                            onStartGame = { color, vsComputer ->
                                viewModel.startGame(color, vsComputer)
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
                            currentTheme = viewModel.currentTheme,
                            onThemeSelect = { viewModel.setTheme(it) }
                        )
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF302E2B))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top Bar with Status and Quit
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.gameStatus,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Button(
                onClick = onQuit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3C39)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Quit", color = Color.White, fontSize = 14.sp)
            }
        }

        // Opponent Info & Clock
        GamePlayerInfo(
            name = if (viewModel.currentPlayer == Colors.WHITE) "Computer" else "You",
            time = if (viewModel.currentPlayer == Colors.WHITE) viewModel.blackTime else viewModel.whiteTime,
            isCurrent = viewModel.currentPlayer == Colors.BLACK // Adjust based on human color
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Captured by Black
        CapturedPiecesRow(pieces = viewModel.capturedByBlack)

        Spacer(modifier = Modifier.weight(1f))

        ChessBoard(
            boardState = viewModel.boardState,
            selectedPosition = viewModel.selectedPosition,
            validMoves = viewModel.validMoves,
            onSquareClick = { viewModel.onSquareClick(it) },
            theme = viewModel.currentTheme
        )

        Spacer(modifier = Modifier.weight(1f))

        // Captured by White
        CapturedPiecesRow(pieces = viewModel.capturedByWhite)

        Spacer(modifier = Modifier.height(8.dp))

        // Human Info & Clock
        GamePlayerInfo(
            name = "You",
            time = if (viewModel.currentPlayer == Colors.WHITE) viewModel.whiteTime else viewModel.blackTime,
            isCurrent = viewModel.currentPlayer == Colors.WHITE // Adjust
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // History
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

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

@Composable
fun CapturedPiecesRow(pieces: List<Piece>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        pieces.forEach { piece ->
            Text(
                text = getPieceSymbol(piece),
                fontSize = 24.sp,
                color = if (piece.color == Colors.WHITE) Color.White else Color(0xFF1E1E1E),
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
fun ChessBoard(
    boardState: Map<Position, Piece>,
    selectedPosition: Position?,
    validMoves: List<Position>,
    onSquareClick: (Position) -> Unit,
    theme: com.dariusepure.chessmobile.ui.BoardTheme
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
    theme: com.dariusepure.chessmobile.ui.BoardTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isLightSquare = (position.x.code + position.y) % 2 != 0
    val baseColor = if (isLightSquare) theme.lightSquare else theme.darkSquare
    
    val backgroundColor = when {
        isSelected -> Color(0xFFF6F669)
        isValidMove -> if (piece != null) Color(0xFFFF7B7B) else Color(0xFFF6F669).copy(alpha = 0.5f)
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
                fontSize = 40.sp,
                color = if (piece.color == Colors.WHITE) Color.White else Color(0xFF1E1E1E)
            )
        }
    }
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
