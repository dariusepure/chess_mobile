package com.dariusepure.chessmobile.pieces

import com.dariusepure.chessmobile.logic.Board
import com.dariusepure.chessmobile.logic.Colors
import com.dariusepure.chessmobile.logic.Position

interface ChessPiece {
    fun getPossibleMoves(board: Board): List<Position>
    fun checkForCheck(board: Board, kingPosition: Position): Boolean
    val type: Char
    val color: Colors
    var position: Position
}
