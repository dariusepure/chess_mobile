package com.dariusepure.chessmobile.logic

class Game {
    val board = Board()
    var currentPlayer = Colors.WHITE
        private set
    
    var humanPlayerColor = Colors.WHITE
    var isComputerOpponent = true

    val history = mutableListOf<Move>()

    fun start(playerColor: Colors, vsComputer: Boolean) {
        board.initialize()
        currentPlayer = Colors.WHITE
        humanPlayerColor = playerColor
        isComputerOpponent = vsComputer
        history.clear()
    }

    fun makeMove(from: Position, to: Position, promotedType: Char? = null): Move? {
        val piece = board.getPieceAt(from) ?: return null
        if (piece.color != currentPlayer) return null

        val move = board.movePiece(from, to, promotedType) ?: return null
        history.add(move)
        
        switchPlayer()
        return move
    }

    fun makeComputerMove(): Move? {
        if (!isComputerOpponent || currentPlayer == humanPlayerColor) return null
        val move = ComputerPlayer.makeMove(board, currentPlayer)
        if (move != null) {
            history.add(move)
            switchPlayer()
        }
        return move
    }

    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == Colors.WHITE) Colors.BLACK else Colors.WHITE
    }
}
