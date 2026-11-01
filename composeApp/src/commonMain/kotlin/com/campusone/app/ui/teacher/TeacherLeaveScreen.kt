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

data class TeacherLeaveItem(
    val id: String,
    val rollNo: String,
    val studentName: String,
    val section: String,
    val attendance: String,
    val waitingChip: String,
    val waitingType: StatusMarkType,
    val dates: String,
    val reason: String,
    val documentName: String,
    var status: String = "PENDING"
)

@Composable
fun TeacherLeaveScreen() {
    var selectedTab by remember { mutableStateOf("Pending") }
    var selectedLeaveForDecision by remember { mutableStateOf<TeacherLeaveItem?>(null) }

    var leaveList by remember {
        mutableStateOf(
            listOf(
                TeacherLeaveItem("1", "01", "Aarav Sharma", "Section A", "88%", "Waiting 1 day", StatusMarkType.INFO, "Nov 1 to Nov 3", "Severe viral fever with prescription", "Doctor_Certificate.pdf"),
                TeacherLeaveItem("2", "02", "Ananya Verma", "Section A", "91%", "Waiting 3 days", StatusMarkType.ATTENTION, "Nov 5 to Nov 6", "Attending national youth conference as university delegate", "Event_Invite.pdf"),
                TeacherLeaveItem("3", "03", "Dev Patel", "Section B", "72%", "Waiting today", StatusMarkType.INFO, "Nov 10", "Family function and travel", "Leave_Letter.pdf")
            )
        )
    }

    if (selectedLeaveForDecision == null) {
        // Leave Requests List (PDF Page 9, middle)
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
                    Text("Leave requests", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Only requests assigned to you appear here", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Filter Tabs: Pending 3, Decided, Escalated
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val pendingCount = leaveList.count { it.status == "PENDING" }
                    FilterChip(
                        selected = selectedTab == "Pending",
                        onClick = { selectedTab = "Pending" },
                        label = { Text("Pending $pendingCount") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedTab == "Decided",
                        onClick = { selectedTab = "Decided" },
                        label = { Text("Decided") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedTab == "Escalated",
                        onClick = { selectedTab = "Escalated" },
                        label = { Text("Escalated") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                val currentDisplay = when (selectedTab) {
                    "Pending" -> leaveList.filter { it.status == "PENDING" }
                    "Decided" -> leaveList.filter { it.status != "PENDING" }
                    else -> emptyList()
                }

                if (currentDisplay.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No requests in this category", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(currentDisplay) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedLeaveForDecision = item }
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(item.rollNo, fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 12.sp)
                                            }
                                            Column {
                                                Text(item.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                                Text("${item.section} · Roll ${item.rollNo}", fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                        }
                                        StatusMarkChip(label = item.waitingChip, type = item.waitingType)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.dates, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = IndigoPrimary)
                                        Text("📎 Document", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }

                                    Text(item.reason, fontSize = 12.sp, color = Color(0xFF475569))
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Leave Decision View (PDF Page 9, right)
        val leave = selectedLeaveForDecision!!
        var comment by remember { mutableStateOf("") }

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
                        modifier = Modifier.clickable { selectedLeaveForDecision = null }
                    )
                    Column {
                        Text("Leave request", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Submitted today", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Student Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(leave.rollNo, fontWeight = FontWeight.Bold, color = IndigoPrimary)
                            }
                            Column {
                                Text(leave.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("${leave.section} · Attendance ${leave.attendance}", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }

                // Dates & Reason Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Dates: ${leave.dates}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IndigoPrimary)
                        Text(leave.reason, fontSize = 13.sp, color = Color(0xFF334155))
                    }
                }

                // Document Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(leave.documentName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Your access is recorded", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Text("›", fontSize = 20.sp, color = Color(0xFF94A3B8))
                    }
                }

                // Comment input
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment for student (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Decision Action Buttons (Reject / Approve)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            leaveList = leaveList.map {
                                if (it.id == leave.id) it.copy(status = "REJECTED") else it
                            }
                            selectedLeaveForDecision = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDangerRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Text("Reject", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            leaveList = leaveList.map {
                                if (it.id == leave.id) it.copy(status = "APPROVED") else it
                            }
                            selectedLeaveForDecision = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Approve", fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reassign to a colleague",
                        color = IndigoPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { selectedLeaveForDecision = null }
                    )
                }
            }
        }
    }
}
