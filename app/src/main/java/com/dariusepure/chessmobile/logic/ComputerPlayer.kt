package com.dariusepure.chessmobile.logic

import com.dariusepure.chessmobile.pieces.Piece
import kotlin.random.Random

object ComputerPlayer {
    fun makeMove(board: Board, computerColor: Colors): Move? {
        val pieces = board.getAllPieces().filter { it.value.color == computerColor }
        if (pieces.isEmpty()) return null

        val allValidMoves = mutableListOf<Pair<Position, Position>>()
        for ((from, piece) in pieces) {
            val validMoves = board.getValidMovesForPiece(from)
            for (to in validMoves) {
                allValidMoves.add(from to to)
            }
        }

        if (allValidMoves.isEmpty()) return null

        // Priority 1: Captures with high value
        val captureMoves = allValidMoves.filter { (_, to) ->
            board.getPieceAt(to) != null
        }.sortedByDescending { (_, to) ->
            getPieceValue(board.getPieceAt(to)?.type ?: ' ')
        }

        if (captureMoves.isNotEmpty()) {
            val bestCapture = captureMoves.first()
            return board.movePiece(bestCapture.first, bestCapture.second)
        }

        // Priority 2: Safe moves
        val safeMoves = allValidMoves.filter { (_, to) ->
            isSafeSquare(board, to, computerColor)
        }

        val chosenMove = if (safeMoves.isNotEmpty()) {
            safeMoves.random()
        } else {
            allValidMoves.random()
        }

        return board.movePiece(chosenMove.first, chosenMove.second)
    }

    private fun isSafeSquare(board: Board, pos: Position, playerColor: Colors): Boolean {
        return board.getAllPieces().values.none { it.color != playerColor && it.getPossibleMoves(board).contains(pos) }
    }

    private fun getPieceValue(type: Char): Int {
        return when (type) {
            'Q' -> 9
            'R' -> 5
            'B' -> 3
            'N' -> 3
            'P' -> 1
            else -> 0
        }
    }
}
