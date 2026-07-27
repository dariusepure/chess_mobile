package com.dariusepure.chessmobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dariusepure.chessmobile.logic.Colors

@Composable
fun PawnPromotionDialog(
    color: Colors,
    onSelect: (Char) -> Unit
) {
    val pieces = listOf('Q', 'R', 'B', 'N')
    
    Dialog(onDismissRequest = { /* Cannot dismiss */ }) {
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3E3C39))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Promote Pawn to:", color = Color.White, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pieces.forEach { type ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF769656))
                                .clickable { onSelect(type) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getPromotionSymbol(type, color),
                                fontSize = 32.sp,
                                color = if (color == Colors.WHITE) Color.White else Color(0xFF1E1E1E)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getPromotionSymbol(type: Char, color: Colors): String {
    return when (type) {
        'Q' -> if (color == Colors.WHITE) "♕" else "♛"
        'R' -> if (color == Colors.WHITE) "♖" else "♜"
        'B' -> if (color == Colors.WHITE) "♗" else "♝"
        'N' -> if (color == Colors.WHITE) "♘" else "♞"
        else -> ""
    }
}
