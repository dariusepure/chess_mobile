package com.dariusepure.chessmobile.pieces

import com.dariusepure.chessmobile.logic.Board
import com.dariusepure.chessmobile.logic.Colors
import com.dariusepure.chessmobile.logic.Position

abstract class Piece(
    override val color: Colors,
    override var position: Position,
    protected val moveStrategy: MoveStrategy
) : ChessPiece {
    var hasMoved: Boolean = false

    override fun getPossibleMoves(board: Board): List<Position> {
        return moveStrategy.getPossibleMoves(board, position)
    }

    override fun checkForCheck(board: Board, kingPosition: Position): Boolean {
        return moveStrategy.canCheckKing(board, position, kingPosition)
    }

    protected fun isValidPosition(x: Char, y: Int): Boolean {
        return x in 'A'..'H' && y in 1..8
    }

    protected fun isOpponentPiece(board: Board, p: Position): Boolean {
        val piece = board.getPieceAt(p)
        return piece != null && piece.color != this.color
    }

    protected fun isOwnPiece(board: Board, p: Position): Boolean {
        val piece = board.getPieceAt(p)
        return piece != null && piece.color == this.color
    }

    protected fun isEmptySquare(board: Board, p: Position): Boolean {
        return board.getPieceAt(p) == null
    }
}
