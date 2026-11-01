package com.campusone.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusone.app.theme.*
import com.campusone.app.ui.components.StatusMarkChip
import com.campusone.app.ui.components.StatusMarkType

@Composable
fun StudentCampusHubScreen(
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToLeave: () -> Unit = {},
    onNavigateToNotices: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToAI: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .verticalScroll(rememberScrollState())
    ) {
        // Dark Blue Top Header (PDF Page 4)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Campus",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Row 1: Subjects & Timetable
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampusHubCard(
                    icon = "📖",
                    title = "Subjects",
                    subtitle = "Teachers and notes",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSubjects
                )
                CampusHubCard(
                    icon = "⏰",
                    title = "Timetable",
                    subtitle = "Today and this week",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTimetable
                )
            }

            // Row 2: Leave & Notices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampusHubCard(
                    icon = "✉️",
                    title = "Leave",
                    subtitle = "Apply and track",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLeave
                )
                CampusHubCard(
                    icon = "📢",
                    title = "Notices",
                    subtitle = "From your teachers",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToNotices
                )
            }

            // Row 3: Library & AI Assistant
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampusHubCard(
                    icon = "📚",
                    title = "Library",
                    subtitle = "Search and borrow",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLibrary
                )
                CampusHubCard(
                    icon = "🤖",
                    title = "AI assistant",
                    subtitle = "Ask for help",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAI
                )
            }

            // Pinned Notice Card (PDF Page 4)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToNotices() }
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pinned notice",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        StatusMarkChip(label = "Pinned", type = StatusMarkType.INFO)
                    }
                    Text(
                        text = "Campus Annual Hackathon 2026 Registration Open",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Published by Dean Academic Affairs · 28 Oct",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun CampusHubCard(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFEEF2FF), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
