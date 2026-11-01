package com.campusone.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusone.app.theme.*

enum class StatusMarkType(val shape: String, val color: Color, val bgColor: Color) {
    GOOD("●", StatusGoodGreen, Color(0xFFECFDF5)),
    ATTENTION("▲", StatusAttentionAmber, Color(0xFFFFFBEB)),
    DANGER("■", StatusDangerRed, Color(0xFFFEF2F2)),
    INFO("◆", StatusInfoIndigo, Color(0xFFEEF2FF))
}

@Composable
fun StatusMarkChip(
    type: StatusMarkType,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(type.bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = type.shape,
            color = type.color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = type.color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

