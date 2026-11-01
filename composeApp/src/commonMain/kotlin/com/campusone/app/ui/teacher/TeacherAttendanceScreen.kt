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

data class StudentAttendanceItem(
    val rollNo: String,
    val name: String,
    var status: String // "P", "AB", "NM"
)

@Composable
fun TeacherAttendanceScreen(
    onFinalized: () -> Unit = {}
) {
    var students by remember {
        mutableStateOf(
            listOf(
                StudentAttendanceItem("01", "Shrey Student", "P"),
                StudentAttendanceItem("02", "Aarav Sharma", "P"),
                StudentAttendanceItem("03", "Ananya Verma", "AB"),
                StudentAttendanceItem("04", "Dev Patel", "NM"),
                StudentAttendanceItem("05", "Isha Gupta", "P")
            )
        )
    }

    val presentCount = students.count { it.status == "P" }
    val absentCount = students.count { it.status == "AB" }
    val nmCount = students.count { it.status == "NM" }

    var showFinalizeConfirm by remember { mutableStateOf(false) }
    var showCorrectDialog by remember { mutableStateOf<StudentAttendanceItem?>(null) }
    var isFinalized by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // Dark Blue Header (PDF Page 7)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Teacher", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                Text("Mark attendance", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Section A · CS301 Data Structures · 30 Oct · Period 1", fontSize = 12.sp, color = Color(0xFFCBD5E1))
            }
        }

        // Summary Bar (PDF Page 7)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${students.size} marked",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("● P $presentCount", color = StatusGoodGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("■ AB $absentCount", color = StatusDangerRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("◆ NM $nmCount", color = StatusInfoIndigo, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Student Roster
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Students (${students.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    TextButton(
                        onClick = {
                            students = students.map { it.copy(status = "P") }
                        }
                    ) {
                        Text("Mark all present", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IndigoPrimary)
                    }
                }
            }

            items(students) { student ->
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
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(student.rollNo, fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 12.sp)
                            }
                            Column {
                                Text(student.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                Text("Roll ${student.rollNo}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }

                        // Segmented Toggle P | AB | NM (PDF Page 7)
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            listOf("P", "AB", "NM").forEach { mark ->
                                val isSelected = student.status == mark
                                val btnBg = if (isSelected) {
                                    when (mark) {
                                        "P" -> StatusGoodGreen
                                        "AB" -> StatusDangerRed
                                        else -> StatusInfoIndigo
                                    }
                                } else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(btnBg)
                                        .clickable {
                                            students = students.map {
                                                if (it.rollNo == student.rollNo) it.copy(status = mark) else it
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Bar (Save Draft, Finalize) (PDF Page 7)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceWhite,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Draft saved */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save draft", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showFinalizeConfirm = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Finalize", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showFinalizeConfirm) {
        AlertDialog(
            onDismissRequest = { showFinalizeConfirm = false },
            title = { Text("Finalize Attendance?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This session will be locked and counted in institutional metrics. Corrections will require audit trail justification within 7 days.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinalizeConfirm = false
                        isFinalized = true
                        onFinalized()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Confirm Finalize")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalizeConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
