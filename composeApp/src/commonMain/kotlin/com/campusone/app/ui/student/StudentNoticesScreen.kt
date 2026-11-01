package com.campusone.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.campusone.app.ui.components.StatusMarkChip
import com.campusone.app.ui.components.StatusMarkType

data class NoticeItem(
    val title: String,
    val author: String,
    val date: String,
    val summary: String,
    val isPinned: Boolean = false
)

@Composable
fun StudentNoticesScreen(
    onBack: () -> Unit = {}
) {
    val notices = remember {
        listOf(
            NoticeItem("Campus Annual Hackathon 2026 Registration Open", "Dean Academic Affairs", "28 Oct 2026", "Teams of 3-4 students can register for the 36-hour hackathon before Nov 10. Cash prizes worth ₹1,50,000.", isPinned = true),
            NoticeItem("Mid-Semester Exam Schedule Published", "Examination Cell", "26 Oct 2026", "Mid-term examinations will commence from Nov 18. Detailed room allotments and timetable attached.", isPinned = false),
            NoticeItem("CS301 Extra Tutorial Lecture Scheduled", "Dr. Alan Turing", "25 Oct 2026", "Extra problem solving session on Red-Black Trees will be held this Saturday from 10:00 AM in Room 302.", isPinned = false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "←",
                    fontSize = 22.sp,
                    color = Color.White,
                    modifier = Modifier.clickable { onBack() }
                )
                Column {
                    Text(
                        text = "Campus Notices",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Official updates and announcements",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(notices) { notice ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
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
                                text = notice.date,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                            if (notice.isPinned) {
                                StatusMarkChip(label = "Pinned", type = StatusMarkType.INFO)
                            }
                        }

                        Text(
                            text = notice.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Text(
                            text = notice.summary,
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )

                        Text(
                            text = "Published by: ${notice.author}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

