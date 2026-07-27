package com.dariusepure.chessmobile.pieces

import com.dariusepure.chessmobile.logic.Board
import com.dariusepure.chessmobile.logic.Colors
import com.dariusepure.chessmobile.logic.Position
import kotlin.math.abs

class King(color: Colors, position: Position) : Piece(color, position, KingMoveStrategy()) {
    override val type: Char = 'K'
}

class Queen(color: Colors, position: Position) : Piece(color, position, QueenMoveStrategy()) {
    override val type: Char = 'Q'
}

class Rook(color: Colors, position: Position) : Piece(color, position, RookMoveStrategy()) {
    override val type: Char = 'R'
}

class Bishop(color: Colors, position: Position) : Piece(color, position, BishopMoveStrategy()) {
    override val type: Char = 'B'
}

class Knight(color: Colors, position: Position) : Piece(color, position, KnightMoveStrategy()) {
    override val type: Char = 'N'
}

class Pawn(color: Colors, position: Position) : Piece(color, position, PawnMoveStrategy()) {
    override val type: Char = 'P'
}

// Strategies

class KingMoveStrategy : MoveStrategy {
    override fun getPossibleMoves(board: Board, currentPos: Position): List<Position> {
        val moves = mutableListOf<Position>()
        val king = board.getPieceAt(currentPos) as? King ?: return moves

        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val nextX = (currentPos.x.code + dx).toChar()
                val nextY = currentPos.y + dy
                if (nextX in 'A'..'H' && nextY in 1..8) {
                    moves.add(Position(nextX, nextY))
                }
            }
        }

        // Castling
        if (!king.hasMoved && !board.isInCheck(king.color)) {
            // Kingside
            val kingsideRook = board.getPieceAt(Position('H', currentPos.y))
            if (kingsideRook is Rook && !kingsideRook.hasMoved) {
                if (board.getPieceAt(Position('F', currentPos.y)) == null &&
                    board.getPieceAt(Position('G', currentPos.y)) == null
                ) {
                    if (!isSquareAttacked(board, Position('F', currentPos.y), king.color) &&
                        !isSquareAttacked(board, Position('G', currentPos.y), king.color)
                    ) {
                        moves.add(Position('G', currentPos.y))
                    }
                }
            }
            // Queenside
            val queensideRook = board.getPieceAt(Position('A', currentPos.y))
            if (queensideRook is Rook && !queensideRook.hasMoved) {
                if (board.getPieceAt(Position('B', currentPos.y)) == null &&
                    board.getPieceAt(Position('C', currentPos.y)) == null &&
                    board.getPieceAt(Position('D', currentPos.y)) == null
                ) {
                    if (!isSquareAttacked(board, Position('D', currentPos.y), king.color) &&
                        !isSquareAttacked(board, Position('C', currentPos.y), king.color)
                    ) {
                        moves.add(Position('C', currentPos.y))
                    }
                }
            }
        }

        return moves
    }

    private fun isSquareAttacked(board: Board, pos: Position, defenderColor: Colors): Boolean {
        return board.getAllPieces().values.any { it.color != defenderColor && it.checkForCheck(board, pos) }
    }

    override fun canCheckKing(board: Board, currentPos: Position, kingPos: Position): Boolean {
        return abs(currentPos.x.code - kingPos.x.code) <= 1 && abs(currentPos.y - kingPos.y) <= 1
    }
}

class KnightMoveStrategy : MoveStrategy {
    private val knightMoves = listOf(
        -2 to -1, -2 to 1, -1 to -2, -1 to 2,
        1 to -2, 1 to 2, 2 to -1, 2 to 1
    )

    override fun getPossibleMoves(board: Board, currentPos: Position): List<Position> {
        val moves = mutableListOf<Position>()
        for ((dx, dy) in knightMoves) {
            val nextX = (currentPos.x.code + dx).toChar()
            val nextY = currentPos.y + dy
            if (nextX in 'A'..'H' && nextY in 1..8) {
                moves.add(Position(nextX, nextY))
            }
        }
        return moves
    }

    override fun canCheckKing(board: Board, currentPos: Position, kingPos: Position): Boolean {
        val dx = abs(currentPos.x.code - kingPos.x.code)
        val dy = abs(currentPos.y - kingPos.y)
        return (dx == 1 && dy == 2) || (dx == 2 && dy == 1)
    }
}

class RookMoveStrategy : MoveStrategy {
    override fun getPossibleMoves(board: Board, currentPos: Position): List<Position> {
        val moves = mutableListOf<Position>()
        val directions = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)
        for ((dx, dy) in directions) {
            var nextX = (currentPos.x.code + dx).toChar()
            var nextY = currentPos.y + dy
            while (nextX in 'A'..'H' && nextY in 1..8) {
                val pos = Position(nextX, nextY)
                moves.add(pos)
                if (board.getPieceAt(pos) != null) break
                nextX = (nextX.code + dx).toChar()
                nextY += dy
            }
        }
        return moves
    }

    override fun canCheckKing(board: Board, currentPos: Position, kingPos: Position): Boolean {
        if (currentPos.x != kingPos.x && currentPos.y != kingPos.y) return false
        val dx = if (kingPos.x > currentPos.x) 1 else if (kingPos.x < currentPos.x) -1 else 0
        val dy = if (kingPos.y > currentPos.y) 1 else if (kingPos.y < currentPos.y) -1 else 0
        var nextX = (currentPos.x.code + dx).toChar()
        var nextY = currentPos.y + dy
        while (nextX != kingPos.x || nextY != kingPos.y) {
            if (board.getPieceAt(Position(nextX, nextY)) != null) return false
            nextX = (nextX.code + dx).toChar()
            nextY += dy
        }
        return true
    }
}

class BishopMoveStrategy : MoveStrategy {
    override fun getPossibleMoves(board: Board, currentPos: Position): List<Position> {
        val moves = mutableListOf<Position>()
        val directions = listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1)
        for ((dx, dy) in directions) {
            var nextX = (currentPos.x.code + dx).toChar()
            var nextY = currentPos.y + dy
            while (nextX in 'A'..'H' && nextY in 1..8) {
                val pos = Position(nextX, nextY)
                moves.add(pos)
                if (board.getPieceAt(pos) != null) break
                nextX = (nextX.code + dx).toChar()
                nextY += dy
            }
        }
        return moves
    }

    override fun canCheckKing(board: Board, currentPos: Position, kingPos: Position): Boolean {
        if (abs(currentPos.x.code - kingPos.x.code) != abs(currentPos.y - kingPos.y)) return false
        val dx = if (kingPos.x > currentPos.x) 1 else -1
        val dy = if (kingPos.y > currentPos.y) 1 else -1
        var nextX = (currentPos.x.code + dx).toChar()
        var nextY = currentPos.y + dy
        while (nextX != kingPos.x || nextY != kingPos.y) {
            if (board.getPieceAt(Position(nextX, nextY)) != null) return false
            nextX = (nextX.code + dx).toChar()
            nextY += dy
        }
        return true
    }
}

class QueenMoveStrategy : MoveStrategy {
    private val rookStrategy = RookMoveStrategy()
    private val bishopStrategy = BishopMoveStrategy()

    override fun getPossibleMoves(board: Board, currentPos: Position): List<Position> {
        return rookStrategy.getPossibleMoves(board, currentPos) + bishopStrategy.getPossibleMoves(board, currentPos)
    }

    override fun canCheckKing(board: Board, currentPos: Position, kingPos: Position): Boolean {
        return rookStrategy.canCheckKing(board, currentPos, kingPos) || bishopStrategy.canCheckKing(board, currentPos, kingPos)
    }
}

class PawnMoveStrategy : MoveStrategy {
    override fun getPossibleMoves(board: Board, currentPos: Position): List<Position> {
        val moves = mutableListOf<Position>()
        val pawn = board.getPieceAt(currentPos) as? Pawn ?: return moves
        val direction = if (pawn.color == Colors.WHITE) 1 else -1
        
        // Forward
        val nextY = currentPos.y + direction
        if (nextY in 1..8) {
            val forwardPos = Position(currentPos.x, nextY)
            if (board.getPieceAt(forwardPos) == null) {
                moves.add(forwardPos)
                if (!pawn.hasMoved) {
                    val doubleForwardY = currentPos.y + 2 * direction
                    if (doubleForwardY in 1..8) {
                        val doubleForwardPos = Position(currentPos.x, doubleForwardY)
                        if (board.getPieceAt(doubleForwardPos) == null) {
                            moves.add(doubleForwardPos)
                        }
                    }
                }
            }
        }
        
        // Captures
        for (dx in listOf(-1, 1)) {
            val capX = (currentPos.x.code + dx).toChar()
            if (capX in 'A'..'H') {
                val capPos = Position(capX, nextY)
                val target = board.getPieceAt(capPos)
                if (target != null && target.color != pawn.color) {
                    moves.add(capPos)
                } else if (target == null) {
                    // En Passant
                    val lastMove = board.lastMove
                    if (lastMove != null && lastMove.capturedPiece == null) {
                        val lastPiece = board.getPieceAt(lastMove.to)
                        if (lastPiece is Pawn && lastPiece.color != pawn.color) {
                            if (abs(lastMove.from.y - lastMove.to.y) == 2 && lastMove.to.x == capX && lastMove.to.y == currentPos.y) {
                                moves.add(capPos)
                            }
                        }
                    }
                }
            }
        }
        
        return moves
    }

    override fun canCheckKing(board: Board, currentPos: Position, kingPos: Position): Boolean {
        val pawn = board.getPieceAt(currentPos) as? Pawn ?: return false
        val direction = if (pawn.color == Colors.WHITE) 1 else -1
        return kingPos.y == currentPos.y + direction && abs(kingPos.x.code - currentPos.x.code) == 1
    }
}
