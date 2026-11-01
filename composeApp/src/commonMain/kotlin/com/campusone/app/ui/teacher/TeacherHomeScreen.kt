package com.campusone.app.ui.teacher

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
fun TeacherHomeScreen(
    teacherName: String = "Dr. Alan Turing",
    onTakeAttendance: () -> Unit = {},
    onNavigateToClasses: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToLeave: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .verticalScroll(rememberScrollState())
    ) {
        // Dark Blue Header (PDF Page 6)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Text("Teacher", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                Text("Hello, $teacherName", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // "Needs your attention" Card (PDF Page 6)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Needs your attention", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                    // Row 1: Attendance
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTakeAttendance() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Attendance to finalize", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            Text("2 open sessions", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFFBEB), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("▲ 2", color = StatusAttentionAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Row 2: Submissions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAssignments() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Submissions to grade", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            Text("3 assignments", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("◆ 18", color = StatusInfoIndigo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Row 3: Leave
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToLeave() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Leave requests pending", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            Text("Assigned to you", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("◆ 3", color = StatusInfoIndigo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Today's Classes Card (PDF Page 6)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Today's classes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("Friday, 30 Oct", fontSize = 12.sp, color = Color(0xFF64748B))
                    }

                    // Class 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("09:00", fontWeight = FontWeight.Bold, color = Color(0xFF64748B), fontSize = 13.sp)
                            Column {
                                Text("Section A · CS301", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Room 302", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        Button(
                            onClick = onTakeAttendance,
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Take attendance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Class 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("10:30", fontWeight = FontWeight.Bold, color = Color(0xFF64748B), fontSize = 13.sp)
                            Column {
                                Text("Section B · CS301", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Room 105", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        StatusMarkChip(label = "Done", type = StatusMarkType.GOOD)
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Class 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("12:00", fontWeight = FontWeight.Bold, color = Color(0xFF64748B), fontSize = 13.sp)
                            Column {
                                Text("Section C · CS303", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Room 204", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        StatusMarkChip(label = "Upcoming", type = StatusMarkType.INFO)
                    }
                }
            }

            // Bottom Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToAssignments,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("New assignment", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onNavigateToClasses,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Share material", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
