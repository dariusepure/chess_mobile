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

    var themeMode by mutableStateOf(UserManager.getThemeMode(application))
        private set

    fun startGame(playerColor: Colors, vsComputer: Boolean, difficulty: Difficulty = Difficulty.MEDIUM) {
        game.start(playerColor, vsComputer, difficulty)
        whiteTime = 600
        blackTime = 600
        startTimer()
        updateUIState()
        val app = getApplication<Application>()
        GameRepository.saveGame(app, game.history, game.humanPlayerColor, game.isComputerOpponent)
        
        // If human is Black, computer should move first
        if (vsComputer && playerColor == Colors.BLACK) {
            makeComputerMoveWithDelay()
        }
    }

    fun resumeGame() {
        val app = getApplication<Application>()
        val saved = GameRepository.loadGame(app) ?: return
        game.start(saved.second, saved.third)
        saved.first.forEach { move ->
            game.makeMove(move.first, move.second, move.third)
        }
        startTimer()
        updateUIState()
    }

    fun hasSavedGame(): Boolean {
        return GameRepository.loadGame(getApplication<Application>()) != null
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
        if (game.isComputerOpponent && game.currentPlayer != game.humanPlayerColor) return

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
        game.makeMove(from, to, promotedType)
        updateUIState()
        val app = getApplication<Application>()
        GameRepository.saveGame(app, game.history, game.humanPlayerColor, game.isComputerOpponent)
        
        if (isCapture) SoundManager.playCaptureSound() else SoundManager.playMoveSound()
        if (game.board.isInCheck(game.currentPlayer)) SoundManager.playCheckSound()

        if (game.isComputerOpponent && !game.board.isCheckmate(game.currentPlayer)) {
            makeComputerMoveWithDelay()
        }
    }

    private fun makeComputerMoveWithDelay() {
        viewModelScope.launch {
            delay(1000)
            val move = game.makeComputerMove()
            updateUIState()
            val app = getApplication<Application>()
            GameRepository.saveGame(app, game.history, game.humanPlayerColor, game.isComputerOpponent)
            
            if (move?.capturedPiece != null) SoundManager.playCaptureSound() else SoundManager.playMoveSound()
            if (game.board.isInCheck(game.currentPlayer)) SoundManager.playCheckSound()
        }
    }

    fun setTheme(theme: BoardTheme) {
        currentTheme = theme
    }

    fun updateThemeMode(mode: Int) {
        themeMode = mode
        UserManager.setThemeMode(getApplication(), mode)
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
        
        // Calculate evaluation
        var eval = 0f
        boardState.values.forEach { piece ->
            val value = when(piece.type) {
                'Q' -> 9f; 'R' -> 5f; 'B' -> 3f; 'N' -> 3f; 'P' -> 1f; else -> 0f
            }
            if (piece.color == Colors.WHITE) eval += value else eval -= value
        }
        evaluation = eval

        // Update captured pieces lists
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
        val app = getApplication<Application>()
        GameRepository.clearSavedGame(app)
        
        val user = UserManager.currentUser ?: return
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
        UserManager.updateUser(app, user)
    }
}
