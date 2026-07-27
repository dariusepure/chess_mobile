package com.dariusepure.chessmobile.pieces

import com.dariusepure.chessmobile.logic.Board
import com.dariusepure.chessmobile.logic.Position

interface MoveStrategy {
    fun getPossibleMoves(board: Board, currentPos: Position): List<Position>
    fun canCheckKing(board: Board, currentPos: Position, kingPos: Position): Boolean
}
