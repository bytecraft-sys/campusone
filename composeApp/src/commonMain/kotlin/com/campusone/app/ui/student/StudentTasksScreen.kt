package com.campusone.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

data class TaskItem(
    val id: String,
    val subjectCode: String,
    val title: String,
    val dueDate: String,
    val marks: String,
    val status: String,
    val markType: StatusMarkType
)

@Composable
fun StudentTasksScreen() {
    var selectedTab by remember { mutableStateOf("Pending") }
    val tabs = listOf("Pending", "Submitted", "Graded", "All")

    var showSubmitDialog by remember { mutableStateOf<TaskItem?>(null) }
    var submittedTaskIds by remember { mutableStateOf(setOf<String>()) }

    var allTasks by remember {
        mutableStateOf(
            listOf(
                TaskItem("1", "CS303", "Binary Search Tree Implementation", "Tomorrow, 11:59 PM", "20 Pts", "Pending", StatusMarkType.ATTENTION),
                TaskItem("2", "CS301", "ER Diagram for Hospital System", "Nov 4, 5:00 PM", "15 Pts", "Pending", StatusMarkType.ATTENTION),
                TaskItem("3", "MATH201", "Fourier Transform Problem Set 4", "Submitted Oct 28", "25 Pts", "Submitted", StatusMarkType.GOOD),
                TaskItem("4", "CS302", "CPU Scheduling Simulator (Round Robin)", "Oct 24", "30 Pts", "Graded (28/30)", StatusMarkType.INFO)
            )
        )
    }

    LaunchedEffect(Unit) {
        val live = com.campusone.app.data.ApiClient.fetchLiveAssignments()
        if (live.isNotEmpty()) {
            allTasks = live.mapIndexed { idx, it ->
                TaskItem(
                    id = it.id,
                    subjectCode = when (idx % 3) { 0 -> "CS303"; 1 -> "CS301"; else -> "CS302" },
                    title = it.title,
                    dueDate = "Due ${it.dueAt}",
                    marks = "${it.maxMarks.toInt()} Pts",
                    status = if (idx == 2) "Graded (28/30)" else "Pending",
                    markType = if (idx == 2) StatusMarkType.INFO else StatusMarkType.ATTENTION
                )
            }
        }
    }

    val filteredTasks = allTasks.filter { task ->
        when (selectedTab) {
            "Pending" -> (task.status == "Pending" && !submittedTaskIds.contains(task.id))
            "Submitted" -> (task.status == "Submitted" || submittedTaskIds.contains(task.id))
            "Graded" -> task.status.startsWith("Graded")
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Coursework & Tasks",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Semester 5 • 3 active assignments",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }

        // Tab Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Button(
                    onClick = { selectedTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) IndigoPrimary else Color.White,
                        contentColor = if (isSelected) Color.White else Color(0xFF475569)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 2.dp else 0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = tab, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Task Cards List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredTasks) { task ->
                val isSubmitted = submittedTaskIds.contains(task.id) || task.status == "Submitted"
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task.subjectCode,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary,
                                modifier = Modifier
                                    .background(Color(0xFFEEF2FF), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            StatusMarkChip(
                                label = if (isSubmitted) "Submitted" else task.status,
                                type = if (isSubmitted) StatusMarkType.GOOD else task.markType
                            )
                        }

                        Text(
                            text = task.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Due Date", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(task.dueDate, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Weightage", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(task.marks, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            }
                        }

                        if (!isSubmitted && !task.status.startsWith("Graded")) {
                            Button(
                                onClick = { showSubmitDialog = task },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                            ) {
                                Text("Submit Solution", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Submission Dialog
    if (showSubmitDialog != null) {
        val currentTask = showSubmitDialog!!
        var submissionText by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSubmitDialog = null },
            title = {
                Text(
                    text = "Submit: ${currentTask.subjectCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = currentTask.title,
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = submissionText,
                        onValueChange = { submissionText = it },
                        label = { Text("Solution notes / Git link / file text") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("https://github.com/... or code summary") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        submittedTaskIds = submittedTaskIds + currentTask.id
                        showSubmitDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    enabled = submissionText.isNotBlank()
                ) {
                    Text("Confirm Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

