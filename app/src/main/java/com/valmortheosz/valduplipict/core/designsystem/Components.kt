package com.valmortheosz.valduplipict.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SimilarityBadge(
    similarityScore: Float,
    modifier: Modifier = Modifier
) {
    val (text, containerColor, contentColor) = when {
        similarityScore >= 0.95f -> Triple("High", Color(0xFF10B981), Color.White) // Emerald
        similarityScore >= 0.85f -> Triple("Medium", Color(0xFFF59E0B), Color.White) // Amber
        else -> Triple("Low", Color(0xFF6B7280), Color.White) // Gray
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
