package com.dariusepure.chessmobile.pieces

import com.dariusepure.chessmobile.logic.Colors
import com.dariusepure.chessmobile.logic.Position

object PieceFactory {
    fun createPiece(type: Char, color: Colors, position: Position): Piece {
        return when (type) {
            'K' -> King(color, position)
            'Q' -> Queen(color, position)
            'R' -> Rook(color, position)
            'B' -> Bishop(color, position)
            'N' -> Knight(color, position)
            'P' -> {
                val pawn = Pawn(color, position)
                if ((color == Colors.WHITE && position.y != 2) ||
                    (color == Colors.BLACK && position.y != 7)
                ) {
                    pawn.hasMoved = true
                }
                pawn
            }
            else -> throw IllegalArgumentException("Invalid piece type: $type")
        }
    }
}
