package com.dariusepure.chessmobile.logic

data class Position(val x: Char, val y: Int) : Comparable<Position> {
    override fun compareTo(other: Position): Int {
        if (this.y != other.y) {
            return this.y.compareTo(other.y)
        }
        return this.x.compareTo(other.x)
    }

    override fun toString(): String {
        return "$x$y"
    }

    companion object {
        fun fromString(s: String): Position? {
            if (s.length != 2) return null
            val x = s[0].uppercaseChar()
            val y = s[1].digitToIntOrNull() ?: return null
            if (x !in 'A'..'H' || y !in 1..8) return null
            return Position(x, y)
        }
    }
}
