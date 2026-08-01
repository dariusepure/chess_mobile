package com.dariusepure.chessmobile.logic

import com.dariusepure.chessmobile.pieces.Piece
import kotlin.random.Random

enum class Difficulty {
    EASY, MEDIUM, HARD
}

object ComputerPlayer {
    fun makeMove(board: Board, computerColor: Colors, difficulty: Difficulty = Difficulty.MEDIUM): Move? {
        val pieces = board.getAllPieces().filter { it.value.color == computerColor }
        if (pieces.isEmpty()) return null

        val allValidMoves = mutableListOf<Pair<Position, Position>>()
        for ((from, _) in pieces) {
            val validMoves = board.getValidMovesForPiece(from)
            for (to in validMoves) {
                allValidMoves.add(from to to)
            }
        }

        if (allValidMoves.isEmpty()) return null

        return when (difficulty) {
            Difficulty.EASY -> allValidMoves.random().let { board.movePiece(it.first, it.second) }
            Difficulty.MEDIUM -> makeMediumMove(board, allValidMoves, computerColor)
            Difficulty.HARD -> makeHardMove(board, allValidMoves, computerColor)
        }
    }

    private fun makeMediumMove(board: Board, allValidMoves: List<Pair<Position, Position>>, computerColor: Colors): Move? {
        val captureMoves = allValidMoves.filter { (_, to) ->
            board.getPieceAt(to) != null
        }.sortedByDescending { (_, to) ->
            getPieceValue(board.getPieceAt(to)?.type ?: ' ')
        }

        if (captureMoves.isNotEmpty() && Random.nextFloat() > 0.3f) {
            val bestCapture = captureMoves.first()
            return board.movePiece(bestCapture.first, bestCapture.second)
        }

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

    private fun makeHardMove(board: Board, allValidMoves: List<Pair<Position, Position>>, computerColor: Colors): Move? {
        var bestMove = allValidMoves.random()
        var bestScore = -1000

        for ((from, to) in allValidMoves) {
            val target = board.getPieceAt(to)
            var score = getPieceValue(target?.type ?: ' ') * 10
            
            if (!isSafeSquare(board, to, computerColor)) {
                score -= getPieceValue(board.getPieceAt(from)?.type ?: ' ') * 5
            }
            
            if (to.x in 'C'..'F' && to.y in 3..6) score += 2

            if (score > bestScore) {
                bestScore = score
                bestMove = from to to
            }
        }

        return board.movePiece(bestMove.first, bestMove.second)
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
