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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusone.app.theme.*

data class TimetableSlot(
    val time: String,
    val subjectCode: String,
    val subjectName: String,
    val room: String,
    val teacher: String,
    val isLiveNow: Boolean = false
)

@Composable
fun StudentTimetableScreen(
    onBack: () -> Unit = {}
) {
    var selectedDay by remember { mutableStateOf("Fri") }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

    val weeklySchedule = remember {
        mapOf(
            "Mon" to listOf(
                TimetableSlot("09:00 - 10:00", "CS301", "Data Structures & Algorithms", "Room 302", "Dr. Alan Turing"),
                TimetableSlot("10:30 - 11:30", "CS302", "Operating Systems", "Room 105", "Prof. Grace Hopper"),
                TimetableSlot("12:00 - 01:00", "CS303", "Database Management Systems", "Lab 2", "Dr. Edgar Codd"),
                TimetableSlot("02:00 - 03:30", "CS304", "Data Structures Lab", "Software Lab 1", "Dr. Alan Turing")
            ),
            "Tue" to listOf(
                TimetableSlot("09:00 - 10:00", "CS302", "Operating Systems", "Room 105", "Prof. Grace Hopper"),
                TimetableSlot("10:30 - 11:30", "MATH201", "Discrete Mathematics", "Hall B", "Dr. John Nash"),
                TimetableSlot("12:00 - 01:00", "CS301", "Data Structures & Algorithms", "Room 302", "Dr. Alan Turing")
            ),
            "Wed" to listOf(
                TimetableSlot("09:00 - 10:00", "CS301", "Data Structures & Algorithms", "Room 302", "Dr. Alan Turing"),
                TimetableSlot("10:30 - 11:30", "CS303", "Database Management Systems", "Lab 2", "Dr. Edgar Codd"),
                TimetableSlot("01:30 - 03:00", "CS305", "Operating Systems Lab", "Systems Lab", "Prof. Grace Hopper")
            ),
            "Thu" to listOf(
                TimetableSlot("09:00 - 10:00", "CS303", "Database Management Systems", "Lab 2", "Dr. Edgar Codd"),
                TimetableSlot("10:30 - 11:30", "CS302", "Operating Systems", "Room 105", "Prof. Grace Hopper"),
                TimetableSlot("12:00 - 01:00", "CS301", "Data Structures & Algorithms", "Room 302", "Dr. Alan Turing")
            ),
            "Fri" to listOf(
                TimetableSlot("09:00 - 10:00", "CS301", "Data Structures & Algorithms", "Room 302", "Dr. Alan Turing", isLiveNow = true),
                TimetableSlot("10:30 - 11:30", "CS302", "Operating Systems", "Room 105", "Prof. Grace Hopper"),
                TimetableSlot("12:00 - 01:00", "CS303", "Database Management Systems", "Lab 2", "Dr. Edgar Codd"),
                TimetableSlot("02:00 - 03:00", "MATH201", "Discrete Mathematics", "Hall B", "Dr. John Nash")
            )
        )
    }

    val currentSlots = weeklySchedule[selectedDay] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // Dark Navy Header (Matching PDF Skeleton)
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
                        text = "Weekly Timetable",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Semester 5 · Section A (B.Tech CSE)",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Days of the Week Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    val isSelected = selectedDay == day
                    Button(
                        onClick = { selectedDay = day },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) IndigoPrimary else SurfaceWhite,
                            contentColor = if (isSelected) Color.White else Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 2.dp else 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, if (isSelected) IndigoPrimary else Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = day, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            if (day == "Fri") {
                                Text(text = "● Today", fontSize = 9.sp, color = if (isSelected) Color.White else StatusGoodGreen)
                            }
                        }
                    }
                }
            }

            // Timeline Slots List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentSlots) { slot ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (slot.isLiveNow) 2.dp else 1.dp,
                                color = if (slot.isLiveNow) IndigoPrimary else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (slot.isLiveNow) 3.dp else 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                        text = slot.subjectCode,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoPrimary,
                                        modifier = Modifier
                                            .background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                    Text(
                                        text = slot.time,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                if (slot.isLiveNow) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEEF2FF), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "● Live Now",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IndigoPrimary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = slot.subjectName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("📍", fontSize = 12.sp)
                                    Text(slot.room, fontSize = 12.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("👤", fontSize = 12.sp)
                                    Text(slot.teacher, fontSize = 12.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

