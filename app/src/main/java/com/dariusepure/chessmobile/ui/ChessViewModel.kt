package com.dariusepure.chessmobile.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dariusepure.chessmobile.logic.*
import com.dariusepure.chessmobile.pieces.Piece
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChessViewModel(application: Application) : AndroidViewModel(application) {
    private val game = Game()
    
    var boardState by mutableStateOf(game.board.getAllPieces())
        private set
    
    var currentPlayer by mutableStateOf(game.currentPlayer)
        private set

    var selectedPosition by mutableStateOf<Position?>(null)
        private set
        
    var validMoves by mutableStateOf<List<Position>>(emptyList())
        private set

    var gameStatus by mutableStateOf("White's turn")
        private set

    var evaluation by mutableStateOf(0f)
        private set

    var lastMove by mutableStateOf<Move?>(null)
        private set

    var onlineMatchId by mutableStateOf<String?>(null)
        private set

    var activeMatches by mutableStateOf<List<FirestoreMatch>>(emptyList())
        private set
    
    private var globalMatchesListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
    private var matchListener: com.google.firebase.firestore.ListenerRegistration? = null

    var currentTheme by mutableStateOf(ClassicTheme)
        private set

    var whiteTime by mutableStateOf(600)
        private set
    
    var blackTime by mutableStateOf(600)
        private set
        
    private var timerJob: kotlinx.coroutines.Job? = null

    var showPromotionDialog by mutableStateOf(false)
        private set
        
    private var pendingMove: Pair<Position, Position>? = null

    var capturedByWhite by mutableStateOf<List<Piece>>(emptyList())
        private set

    var capturedByBlack by mutableStateOf<List<Piece>>(emptyList())
        private set

    var hasSavedGame by mutableStateOf(false)
        private set

    init {
        checkSavedGame()
        listenToUserMatches()
    }

    private fun listenToUserMatches() {
        globalMatchesListeners.forEach { it.remove() }
        globalMatchesListeners.clear()
        val newListeners = UserManager.listenToMatches { matches ->
            activeMatches = matches
        }
        globalMatchesListeners.addAll(newListeners)
    }

    fun checkSavedGame() {
        val uid = UserManager.currentUser?.uid ?: return
        viewModelScope.launch {
            hasSavedGame = FirestoreGameRepository.loadGame(uid) != null
        }
    }

    fun startGame(playerColor: Colors, vsComputer: Boolean, difficulty: Difficulty = Difficulty.MEDIUM) {
        onlineMatchId = null
        matchListener?.remove()
        
        game.start(playerColor, vsComputer, difficulty)
        whiteTime = 600
        blackTime = 600
        startTimer()
        updateUIState()
        
        val user = UserManager.currentUser
        if (user != null) {
            viewModelScope.launch {
                FirestoreGameRepository.saveGame(user.uid, game.history, game.humanPlayerColor, game.isComputerOpponent)
                hasSavedGame = true
            }
        }
        
        if (vsComputer && playerColor == Colors.BLACK) {
            makeComputerMoveWithDelay()
        }
    }

    fun startOnlineGame(matchId: String, playerColor: Colors) {
        onlineMatchId = matchId
        game.start(playerColor, false)
        whiteTime = 600
        blackTime = 600
        startTimer()
        
        matchListener?.remove()
        matchListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("active_matches")
            .document(matchId)
            .addSnapshotListener { snapshot, _ ->
                val match = snapshot?.toObject(FirestoreMatch::class.java) ?: return@addSnapshotListener
                if (match.moves.size > game.history.size) {
                    val newMoves = match.moves.drop(game.history.size)
                    newMoves.forEach { moveStr ->
                        val parts = moveStr.split("|")
                        val from = Position.fromString(parts[0])!!
                        val to = Position.fromString(parts[1])!!
                        val promo = if (parts[2][0] == ' ') null else parts[2][0]
                        game.makeMove(from, to, promo)
                    }
                    updateUIState()
                }
            }
    }

    fun resumeGame() {
        val user = UserManager.currentUser ?: return
        viewModelScope.launch {
            val saved = FirestoreGameRepository.loadGame(user.uid) ?: return@launch
            game.start(saved.second, saved.third)
            saved.first.forEach { move ->
                game.makeMove(move.first, move.second, move.third)
            }
            startTimer()
            updateUIState()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (game.currentPlayer == Colors.WHITE) {
                    if (whiteTime > 0) whiteTime--
                } else {
                    if (blackTime > 0) blackTime--
                }
            }
        }
    }

    fun onSquareClick(position: Position) {
        if (onlineMatchId != null && game.currentPlayer != game.humanPlayerColor) return
        if (onlineMatchId == null && game.isComputerOpponent && game.currentPlayer != game.humanPlayerColor) return

        val selected = selectedPosition
        if (selected == null) {
            val piece = game.board.getPieceAt(position)
            if (piece != null && piece.color == game.currentPlayer) {
                selectedPosition = position
                validMoves = game.board.getValidMovesForPiece(position)
            }
        } else {
            if (position in validMoves) {
                val piece = game.board.getPieceAt(selected)
                if (piece is com.dariusepure.chessmobile.pieces.Pawn && (position.y == 1 || position.y == 8)) {
                    pendingMove = selected to position
                    showPromotionDialog = true
                } else {
                    executeMove(selected, position)
                }
                selectedPosition = null
                validMoves = emptyList()
            } else {
                val piece = game.board.getPieceAt(position)
                if (piece != null && piece.color == game.currentPlayer) {
                    selectedPosition = position
                    validMoves = game.board.getValidMovesForPiece(position)
                } else {
                    selectedPosition = null
                    validMoves = emptyList()
                }
            }
        }
    }

    fun onPromote(type: Char) {
        val move = pendingMove ?: return
        executeMove(move.first, move.second, type)
        showPromotionDialog = false
        pendingMove = null
    }

    private fun executeMove(from: Position, to: Position, promotedType: Char? = null) {
        val isCapture = game.board.getPieceAt(to) != null
        val move = game.makeMove(from, to, promotedType) ?: return
        updateUIState()
        
        val user = UserManager.currentUser
        if (user != null) {
            viewModelScope.launch {
                if (onlineMatchId != null) {
                    val moveStr = "${move.from}|${move.to}|${move.promotedType ?: ' '}"
                    val currentMoves = game.history.dropLast(1).map { "${it.from}|${it.to}|${it.promotedType ?: ' '}" }
                    UserManager.sendMove(onlineMatchId!!, moveStr, currentMoves)
                } else {
                    FirestoreGameRepository.saveGame(user.uid, game.history, game.humanPlayerColor, game.isComputerOpponent)
                }
            }
        }
        
        if (isCapture) SoundManager.playCaptureSound() else SoundManager.playMoveSound()
        if (game.board.isInCheck(game.currentPlayer)) SoundManager.playCheckSound()

        if (onlineMatchId == null && game.isComputerOpponent && !game.board.isCheckmate(game.currentPlayer)) {
            makeComputerMoveWithDelay()
        }
    }

    private fun makeComputerMoveWithDelay() {
        viewModelScope.launch {
            delay(1000)
            val move = game.makeComputerMove()
            updateUIState()
            
            val user = UserManager.currentUser
            if (user != null) {
                FirestoreGameRepository.saveGame(user.uid, game.history, game.humanPlayerColor, game.isComputerOpponent)
            }
            
            if (move?.capturedPiece != null) SoundManager.playCaptureSound() else SoundManager.playMoveSound()
            if (game.board.isInCheck(game.currentPlayer)) SoundManager.playCheckSound()
        }
    }

    fun setTheme(theme: BoardTheme) {
        currentTheme = theme
    }

    fun getPGN(): String {
        val pgn = StringBuilder()
        pgn.append("[Event \"Casual Game\"]\n")
        pgn.append("[Site \"ChessMobile\"]\n")
        pgn.append("[White \"${if (game.humanPlayerColor == Colors.WHITE) "Human" else "Computer"}\"]\n")
        pgn.append("[Black \"${if (game.humanPlayerColor == Colors.BLACK) "Human" else "Computer"}\"]\n")
        pgn.append("[Result \"*\"]\n\n")

        getFormattedHistory().forEach { pgn.append("$it ") }
        return pgn.toString()
    }

    fun getFormattedHistory(): List<String> {
        val history = mutableListOf<String>()
        for (i in 0 until game.history.size step 2) {
            val whiteMove = game.history[i]
            val blackMove = if (i + 1 < game.history.size) game.history[i + 1] else null
            val round = (i / 2) + 1
            var moveStr = "$round. ${formatMove(whiteMove)}"
            if (blackMove != null) {
                moveStr += " ${formatMove(blackMove)}"
            }
            history.add(moveStr)
        }
        return history
    }

    private fun formatMove(move: Move): String {
        return "${move.from}${if (move.capturedPiece != null) "x" else "-"}${move.to}"
    }

    private fun updateUIState() {
        boardState = game.board.getAllPieces()
        currentPlayer = game.currentPlayer
        lastMove = game.board.lastMove
        
        var eval = 0f
        boardState.values.forEach { piece ->
            val value = when(piece.type) {
                'Q' -> 9f; 'R' -> 5f; 'B' -> 3f; 'N' -> 3f; 'P' -> 1f; else -> 0f
            }
            if (piece.color == Colors.WHITE) eval += value else eval -= value
        }
        evaluation = eval

        val allCaptured = game.history.mapNotNull { it.capturedPiece }
        capturedByWhite = allCaptured.filter { it.color == Colors.BLACK }
        capturedByBlack = allCaptured.filter { it.color == Colors.WHITE }
        
        val colorName = if (currentPlayer == Colors.WHITE) "White" else "Black"
        gameStatus = when {
            game.board.isCheckmate(currentPlayer) -> {
                handleGameOver(winnerColor = if (currentPlayer == Colors.WHITE) Colors.BLACK else Colors.WHITE)
                "Checkmate! ${if (currentPlayer == Colors.WHITE) "Black" else "White"} wins!"
            }
            game.board.isStalemate(currentPlayer) -> {
                handleGameOver(winnerColor = null)
                "Stalemate! Draw."
            }
            game.board.isInCheck(currentPlayer) -> "Check! $colorName's turn"
            else -> "$colorName's turn"
        }
    }

    private fun handleGameOver(winnerColor: Colors?) {
        timerJob?.cancel()
        val user = UserManager.currentUser ?: return
        
        viewModelScope.launch {
            if (onlineMatchId != null) {
                val winnerId = if (winnerColor == null) null else {
                    val match = activeMatches.find { it.matchId == onlineMatchId }
                    if (winnerColor == Colors.WHITE) match?.whitePlayerId else match?.blackPlayerId
                }
                UserManager.finishMatch(onlineMatchId!!, winnerId, draw = (winnerColor == null))
            } else {
                FirestoreGameRepository.clearSavedGame(user.uid)
            }
            
            hasSavedGame = false
            
            if (winnerColor == null) {
                user.draws++
                user.points += 5
            } else if (winnerColor == game.humanPlayerColor) {
                user.wins++
                user.points += 20
            } else {
                user.losses++
                user.points = (user.points - 10).coerceAtLeast(0)
            }
            UserManager.updateUser(user)
        }
    }
}
