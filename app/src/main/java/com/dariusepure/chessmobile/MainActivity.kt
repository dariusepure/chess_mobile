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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dariusepure.chessmobile.logic.*
import com.dariusepure.chessmobile.pieces.Piece
import com.dariusepure.chessmobile.ui.*
import com.dariusepure.chessmobile.ui.theme.ChessMobileTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            var isReady by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                UserManager.init(this@MainActivity) {
                    isReady = true
                }
            }

            if (!isReady) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF302E2B)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF769656))
                }
                return@setContent
            }

            val viewModel: ChessViewModel = viewModel()
            
            ChessMobileTheme {
                val navController = rememberNavController()
                val startDest = if (UserManager.currentUser != null) "main_menu" else "login"

                // Listen for online matches
                var incomingMatch by remember { mutableStateOf<FirestoreMatch?>(null) }
                DisposableEffect(UserManager.currentUser) {
                    val registrations = UserManager.listenToMatches { matches ->
                        val activeMatch = matches.find { it.status == FirestoreGameStatus.ACTIVE }
                        if (activeMatch != null && viewModel.onlineMatchId != activeMatch.matchId) {
                            incomingMatch = activeMatch
                        }
                    }
                    onDispose { registrations.forEach { it.remove() } }
                }

                if (incomingMatch != null) {
                    AlertDialog(
                        onDismissRequest = { incomingMatch = null },
                        title = { Text("Game Invite") },
                        text = { Text("You have been challenged to a game!") },
                        confirmButton = {
                            Button(onClick = {
                                val match = incomingMatch!!
                                val myColor = if (match.whitePlayerId == UserManager.currentUser?.uid) Colors.WHITE else Colors.BLACK
                                viewModel.startOnlineGame(match.matchId, myColor)
                                incomingMatch = null
                                navController.navigate("game")
                            }) {
                                Text("Accept")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { incomingMatch = null }) {
                                Text("Decline")
                            }
                        }
                    )
                }
                
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
                            hasSavedGame = viewModel.hasSavedGame,
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("main_menu") { inclusive = true }
                                }
                            },
                            onLeaderboard = { navController.navigate("leaderboard") },
                            onFriends = { navController.navigate("friends") },
                            onExportBackup = {
                                val success = UserManager.exportLocalBackup(this@MainActivity)
                                val msg = if (success) "Backup created" else "Backup failed"
                                android.widget.Toast.makeText(this@MainActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                            },
                            activeMatches = viewModel.activeMatches,
                            onJoinMatch = { match ->
                                val myColor = if (match.whitePlayerId == UserManager.currentUser?.uid) Colors.WHITE else Colors.BLACK
                                viewModel.startOnlineGame(match.matchId, myColor)
                                navController.navigate("game")
                            },
                            currentTheme = viewModel.currentTheme,
                            onThemeSelect = { viewModel.setTheme(it) }
                        )
                    }
                    composable("leaderboard") {
                        LeaderboardScreen(onBack = { navController.popBackStack() })
                    }
                    composable("friends") {
                        FriendsScreen(
                            onBack = { navController.popBackStack() },
                            onStartOnlineGame = { matchId, color ->
                                viewModel.startOnlineGame(matchId, color)
                                navController.navigate("game")
                            }
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
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                color = MaterialTheme.colorScheme.onBackground
            )
            Row {
                IconButton(onClick = { 
                    val pgn = viewModel.getPGN()
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, pgn)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Share Game PGN"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share PGN", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = onQuit,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Quit", color = MaterialTheme.colorScheme.onSecondary, fontSize = 14.sp)
                }
            }
        }

        // Opponent Info & Clock
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
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val squareSize = if (boardSize.width > 0) boardSize.width / 8f else 0f

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(8.dp)
            .onGloballyPositioned { boardSize = it.size }
    ) {
        // 1. Grid
        Column {
            for (row in 8 downTo 1) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 'A'..'H') {
                        val position = Position(col, row)
                        ChessSquare(
                            position = position,
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

        // 2. Pieces
        if (squareSize > 0) {
            boardState.forEach { (pos, piece) ->
                AnimatedPiece(
                    piece = piece,
                    position = pos,
                    squareSize = squareSize
                )
            }
        }
    }
}

@Composable
fun AnimatedPiece(
    piece: Piece,
    position: Position,
    squareSize: Float
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val targetOffset = Offset(
        x = (position.x.code - 'A'.code) * squareSize,
        y = (8 - position.y) * squareSize
    )
    val animatedOffset by animateOffsetAsState(targetValue = targetOffset, label = "pieceMove")

    val drawableId = remember(piece) {
        context.resources.getIdentifier(getPieceDrawableName(piece), "drawable", context.packageName)
    }

    Box(
        modifier = Modifier
            .size(with(androidx.compose.ui.platform.LocalDensity.current) { squareSize.toDp() })
            .offset { IntOffset(animatedOffset.x.roundToInt(), animatedOffset.y.roundToInt()) }
            .zIndex(1f),
        contentAlignment = Alignment.Center
    ) {
        if (drawableId != 0) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = drawableId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.85f),
                tint = Color.Unspecified
            )
        } else {
            Text(
                text = getPieceSymbol(piece),
                fontSize = 32.sp,
                color = if (piece.color == Colors.WHITE) Color.White else Color(0xFF1E1E1E)
            )
        }
    }
}

fun getPieceDrawableName(piece: Piece): String {
    val colorPrefix = if (piece.color == Colors.WHITE) "w" else "b"
    val typeName = when (piece.type) {
        'K' -> "king"; 'Q' -> "queen"; 'R' -> "rook"; 'B' -> "bishop"; 'N' -> "knight"; 'P' -> "pawn"; else -> ""
    }
    return "${colorPrefix}_${typeName}"
}

@Composable
fun ChessSquare(
    position: Position,
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
        isValidMove -> Color(0xFFF6F669).copy(alpha = 0.5f)
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
