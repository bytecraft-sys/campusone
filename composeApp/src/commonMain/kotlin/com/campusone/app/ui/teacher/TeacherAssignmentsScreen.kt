
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

data class TeacherAssignment(
    val id: String,
    val title: String,
    val sectionSubject: String,
    val dueDate: String,
    val submitted: Int,
    val total: Int,
    val graded: Int,
    val statusLabel: String,
    val statusType: StatusMarkType
)

@Composable
fun TeacherAssignmentsScreen() {
    var selectedTab by remember { mutableStateOf("Active") }
    var showCreateModal by remember { mutableStateOf(false) }

    val assignments = remember {
        listOf(
            TeacherAssignment("1", "Binary Search Tree Implementation", "Section A · CS301", "Due Tomorrow, 11:59 PM", 32, 40, 14, "18 to grade", StatusMarkType.ATTENTION),
            TeacherAssignment("2", "ER Diagram for Hospital System", "Section B · CS303", "Due in 2 days", 18, 40, 0, "Due in 2 days", StatusMarkType.INFO),
            TeacherAssignment("3", "CPU Scheduling Simulator", "Section A · CS302", "Closed 24 Oct", 40, 40, 40, "All graded", StatusMarkType.GOOD)
        )
    }

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
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Assignments", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Button(
                    onClick = { showCreateModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("+ New", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pills: Active, Closed, Drafts
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Active", "Closed", "Drafts").forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(assignments) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                StatusMarkChip(label = item.statusLabel, type = item.statusType)
                            }

                            Text(
                                text = "${item.sectionSubject} · ${item.dueDate}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )

                            // Submission Progress
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Submitted ${item.submitted}/${item.total}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        text = "Graded ${item.graded}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { item.submitted.toFloat() / item.total.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = IndigoPrimary,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateModal) {
        var newTitle by remember { mutableStateOf("") }
        var maxMarks by remember { mutableStateOf("20") }
        AlertDialog(
            onDismissRequest = { showCreateModal = false },
            title = { Text("New assignment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = "Section A · CS301",
                        onValueChange = {},
                        label = { Text("Section and subject") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Assignment title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = maxMarks,
                        onValueChange = { maxMarks = it },
                        label = { Text("Max marks") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCreateModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
