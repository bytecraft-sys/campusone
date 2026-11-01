package com.campusone.app.ui.teacher

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
import com.campusone.app.ui.components.StatusMarkChip
import com.campusone.app.ui.components.StatusMarkType

data class ClassSectionItem(
    val id: String,
    val section: String,
    val subject: String,
    val semester: String,
    val studentCount: Int,
    val nextClass: String,
    val avgAttendance: Int,
    val statusLabel: String,
    val statusType: StatusMarkType
)

data class RosterStudent(
    val rollNo: String,
    val name: String,
    val attendance: String,
    val statusType: StatusMarkType
)

@Composable
fun TeacherClassesScreen(
    onTakeAttendance: () -> Unit = {}
) {
    var selectedSection by remember { mutableStateOf<ClassSectionItem?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }

    val classes = remember {
        listOf(
            ClassSectionItem("1", "Section A", "CS301 Data Structures", "Sem 5", 40, "Today, 09:00 AM", 84, "On track", StatusMarkType.GOOD),
            ClassSectionItem("2", "Section B", "CS301 Data Structures", "Sem 5", 40, "Tomorrow, 11:00 AM", 84, "3 low attendance", StatusMarkType.ATTENTION),
            ClassSectionItem("3", "Section C", "CS303 Database Systems", "Sem 5", 40, "Mon, 10:30 AM", 84, "On track", StatusMarkType.GOOD)
        )
    }

    if (selectedSection == null) {
        // Classes List View (PDF Page 6, middle)
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
                Column {
                    Text("My classes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Sections and subjects allocated to you", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                        selected = selectedFilter == "Today",
                        onClick = { selectedFilter = "Today" },
                        label = { Text("Today") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(classes) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSection = item }
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
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
                                    Text(
                                        text = "${item.section} · ${item.subject}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = item.semester,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoPrimary,
                                        modifier = Modifier
                                            .background(Color(0xFFEEF2FF), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "${item.studentCount} students · Next class ${item.nextClass}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Avg attendance ${item.avgAttendance}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                    StatusMarkChip(label = item.statusLabel, type = item.statusType)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Class Detail and Roster View (PDF Page 6, right)
        val sec = selectedSection!!
        val roster = listOf(
            RosterStudent("01", "Aarav Sharma", "92%", StatusMarkType.GOOD),
            RosterStudent("02", "Ananya Verma", "88%", StatusMarkType.GOOD),
            RosterStudent("03", "Dev Patel", "68%", StatusMarkType.ATTENTION),
            RosterStudent("04", "Isha Gupta", "No data", StatusMarkType.INFO),
            RosterStudent("05", "Rohan Mehta", "95%", StatusMarkType.GOOD)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F5F9))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "←",
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.clickable { selectedSection = null }
                    )
                    Column {
                        Text(
                            text = "${sec.section} · ${sec.subject}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Semester 5 · 2026-27",
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
                // 3 Metrics Row (PDF Page 6)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(value = "40", label = "Students", modifier = Modifier.weight(1f))
                    MetricBox(value = "84%", label = "Attendance", modifier = Modifier.weight(1f))
                    MetricBox(value = "6", label = "To grade", modifier = Modifier.weight(1f))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTakeAttendance,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Take attendance", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("New assignment", fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "Roster (Sorted by roll)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(roster) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(student.rollNo, fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 12.sp)
                                    }
                                    Column {
                                        Text(student.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                        Text("Roll ${student.rollNo}", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                }
                                StatusMarkChip(label = student.attendance, type = student.statusType)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}

