package com.dariusepure.chessmobile.logic

import com.dariusepure.chessmobile.pieces.Piece
import com.dariusepure.chessmobile.pieces.PieceFactory
import kotlin.math.abs

class Board {
    private val pieces = mutableMapOf<Position, Piece>()
    var lastMove: Move? = null

    fun initialize() {
        pieces.clear()
        // White pieces
        addPiece(PieceFactory.createPiece('R', Colors.WHITE, Position('A', 1)))
        addPiece(PieceFactory.createPiece('N', Colors.WHITE, Position('B', 1)))
        addPiece(PieceFactory.createPiece('B', Colors.WHITE, Position('C', 1)))
        addPiece(PieceFactory.createPiece('Q', Colors.WHITE, Position('D', 1)))
        addPiece(PieceFactory.createPiece('K', Colors.WHITE, Position('E', 1)))
        addPiece(PieceFactory.createPiece('B', Colors.WHITE, Position('F', 1)))
        addPiece(PieceFactory.createPiece('N', Colors.WHITE, Position('G', 1)))
        addPiece(PieceFactory.createPiece('R', Colors.WHITE, Position('H', 1)))
        for (c in 'A'..'H') {
            addPiece(PieceFactory.createPiece('P', Colors.WHITE, Position(c, 2)))
        }

        // Black pieces
        addPiece(PieceFactory.createPiece('R', Colors.BLACK, Position('A', 8)))
        addPiece(PieceFactory.createPiece('N', Colors.BLACK, Position('B', 8)))
        addPiece(PieceFactory.createPiece('B', Colors.BLACK, Position('C', 8)))
        addPiece(PieceFactory.createPiece('Q', Colors.BLACK, Position('D', 8)))
        addPiece(PieceFactory.createPiece('K', Colors.BLACK, Position('E', 8)))
        addPiece(PieceFactory.createPiece('B', Colors.BLACK, Position('F', 8)))
        addPiece(PieceFactory.createPiece('N', Colors.BLACK, Position('G', 8)))
        addPiece(PieceFactory.createPiece('R', Colors.BLACK, Position('H', 8)))
        for (c in 'A'..'H') {
            addPiece(PieceFactory.createPiece('P', Colors.BLACK, Position(c, 7)))
        }
    }

    fun clear() {
        pieces.clear()
    }

    fun addPiece(piece: Piece) {
        pieces[piece.position] = piece
    }

    fun getPieceAt(position: Position): Piece? {
        return pieces[position]
    }

    fun getAllPieces(): Map<Position, Piece> {
        return pieces.toMap()
    }

    fun movePiece(from: Position, to: Position, promotedType: Char? = null): Move? {
        val movingPiece = getPieceAt(from) ?: return null
        var targetPiece = getPieceAt(to)

        if (!isValidMove(from, to)) return null

        // Handle En Passant capture
        if (movingPiece is com.dariusepure.chessmobile.pieces.Pawn && targetPiece == null && from.x != to.x) {
            val capPos = Position(to.x, from.y)
            targetPiece = getPieceAt(capPos)
            pieces.remove(capPos)
        }

        pieces.remove(from)
        
        val finalPiece = if (movingPiece is com.dariusepure.chessmobile.pieces.Pawn && (to.y == 1 || to.y == 8)) {
            PieceFactory.createPiece(promotedType ?: 'Q', movingPiece.color, to)
        } else {
            movingPiece
        }

        finalPiece.position = to
        finalPiece.hasMoved = true
        pieces[to] = finalPiece

        // Handle Castling extra move
        if (movingPiece is com.dariusepure.chessmobile.pieces.King && abs(from.x.code - to.x.code) == 2) {
            val rookFromX = if (to.x > from.x) 'H' else 'A'
            val rookToX = if (to.x > from.x) 'F' else 'D'
            val rookFrom = Position(rookFromX, from.y)
            val rookTo = Position(rookToX, from.y)
            val rook = getPieceAt(rookFrom)
            if (rook != null) {
                pieces.remove(rookFrom)
                rook.position = rookTo
                rook.hasMoved = true
                pieces[rookTo] = rook
            }
        }

        val move = Move(movingPiece.color, from, to, targetPiece, if (finalPiece.type != movingPiece.type) finalPiece.type else null)
        lastMove = move
        return move
    }

    fun isValidMove(from: Position, to: Position): Boolean {
        if (to.x !in 'A'..'H' || to.y !in 1..8) return false
        if (from == to) return false
        val piece = getPieceAt(from) ?: return false
        val target = getPieceAt(to)
        if (target != null && target.color == piece.color) return false

        val possibleMoves = piece.getPossibleMoves(this)
        if (to !in possibleMoves) return false

        return !wouldLeaveKingInCheck(from, to, piece.color)
    }

    private fun wouldLeaveKingInCheck(from: Position, to: Position, playerColor: Colors): Boolean {
        val movingPiece = getPieceAt(from) ?: return false
        val targetPiece = getPieceAt(to)

        pieces.remove(from)
        movingPiece.position = to
        pieces[to] = movingPiece

        val kingPos = findKingPosition(playerColor)
        val inCheck = kingPos != null && isPositionAttacked(kingPos, playerColor)

        // Undo
        pieces.remove(to)
        movingPiece.position = from
        pieces[from] = movingPiece
        if (targetPiece != null) {
            pieces[to] = targetPiece
        }

        return inCheck
    }

    private fun findKingPosition(color: Colors): Position? {
        return pieces.entries.find { it.value.type == 'K' && it.value.color == color }?.key
    }

    private fun isPositionAttacked(pos: Position, defenderColor: Colors): Boolean {
        return pieces.values.any { it.color != defenderColor && it.checkForCheck(this, pos) }
    }

    fun isInCheck(playerColor: Colors): Boolean {
        val kingPos = findKingPosition(playerColor) ?: return false
        return isPositionAttacked(kingPos, playerColor)
    }

    fun isCheckmate(playerColor: Colors): Boolean {
        if (!isInCheck(playerColor)) return false
        return !hasValidMoves(playerColor)
    }

    fun isStalemate(playerColor: Colors): Boolean {
        if (isInCheck(playerColor)) return false
        return !hasValidMoves(playerColor)
    }

    private fun hasValidMoves(playerColor: Colors): Boolean {
        return pieces.filter { it.value.color == playerColor }.any { entry ->
            val piece = entry.value
            piece.getPossibleMoves(this).any { to ->
                isValidMove(entry.key, to)
            }
        }
    }

    fun getValidMovesForPiece(position: Position): List<Position> {
        val piece = getPieceAt(position) ?: return emptyList()
        return piece.getPossibleMoves(this).filter { isValidMove(position, it) }
    }
}
