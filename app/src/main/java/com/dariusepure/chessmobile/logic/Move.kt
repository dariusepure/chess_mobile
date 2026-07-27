package com.dariusepure.chessmobile.logic

import com.dariusepure.chessmobile.pieces.Piece

data class Move(
    val playerColor: Colors,
    val from: Position,
    val to: Position,
    val capturedPiece: Piece? = null,
    val promotedType: Char? = null
)
