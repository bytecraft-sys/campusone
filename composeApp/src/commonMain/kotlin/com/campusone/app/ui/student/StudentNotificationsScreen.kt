package com.campusone.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusone.app.theme.*

@Composable
fun StudentNotificationsScreen(
    onBack: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var hasUnread by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // Dark Blue Top Header (PDF Page 3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "←",
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.clickable { onBack() }
                    )
                    Text(
                        text = "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                TextButton(
                    onClick = { hasUnread = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF93C5FD))
                ) {
                    Text("Mark all read", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter pills: All, Unread 2
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == "Unread",
                    onClick = { selectedFilter = "Unread" },
                    label = { Text(if (hasUnread) "Unread 2" else "Unread 0") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Today",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }

                item {
                    NotificationCard(
                        icon = "⚠️",
                        title = "Attendance alert",
                        description = "CS302 Operating Systems is below 75%",
                        time = "09:10",
                        isNew = hasUnread
                    )
                }

                item {
                    NotificationCard(
                        icon = "✉️",
                        title = "Leave approved",
                        description = "Dr. Alan Turing approved your leave request",
                        time = "08:30",
                        isNew = hasUnread
                    )
                }

                if (selectedFilter == "All") {
                    item {
                        Text(
                            text = "Earlier",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    item {
                        NotificationCard(
                            icon = "📝",
                            title = "Assignment due tomorrow",
                            description = "Binary Search Tree Implementation · CS301",
                            time = "Yesterday",
                            isNew = false
                        )
                    }

                    item {
                        NotificationCard(
                            icon = "📢",
                            title = "New notice",
                            description = "Mid-semester examination timetable published",
                            time = "Mon",
                            isNew = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    icon: String,
    title: String,
    description: String,
    time: String,
    isNew: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text(text = description, fontSize = 12.sp, color = Color(0xFF475569))
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = time, fontSize = 11.sp, color = Color(0xFF94A3B8))
                if (isNew) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "New", color = IndigoPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

