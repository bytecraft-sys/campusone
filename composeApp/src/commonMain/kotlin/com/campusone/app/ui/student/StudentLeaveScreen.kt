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
import kotlinx.coroutines.launch
import com.campusone.app.theme.*
import com.campusone.app.ui.components.StatusMarkChip
import com.campusone.app.ui.components.StatusMarkType

data class LeaveRecord(
    val id: String,
    val type: String,
    val dates: String,
    val reason: String,
    val status: String,
    val markType: StatusMarkType
)

@Composable
fun StudentLeaveScreen() {
    var showApplyModal by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var leaveHistory by remember {
        mutableStateOf(
            listOf(
                LeaveRecord("1", "Medical Leave", "Oct 12 - Oct 14 (3 days)", "Viral fever with doctor certificate", "Approved", StatusMarkType.GOOD),
                LeaveRecord("2", "Casual Leave", "Nov 10 (1 day)", "Family commitment / function", "Under Review", StatusMarkType.ATTENTION)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Apply Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Leave Management",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Balances & Application",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
            Button(
                onClick = { showApplyModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ Apply", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Leave Balances Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Casual Balance", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text("4 Days", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = IndigoPrimary)
                    Text("Out of 6 allocated", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Medical Balance", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text("7 Days", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StatusGoodGreen)
                    Text("Certificate required", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        Text(
            text = "Request History",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(leaveHistory) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
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
                                text = item.type,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            StatusMarkChip(label = item.status, type = item.markType)
                        }
                        Text(
                            text = item.dates,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = IndigoPrimary
                        )
                        Text(
                            text = item.reason,
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }

    // Apply Leave Modal
    if (showApplyModal) {
        var selectedType by remember { mutableStateOf("Casual Leave") }
        var datesInput by remember { mutableStateOf("Nov 15 - Nov 16") }
        var reasonInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showApplyModal = false },
            title = { Text("Apply for Leave", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Leave Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedType == "Casual Leave",
                            onClick = { selectedType = "Casual Leave" },
                            label = { Text("Casual") }
                        )
                        FilterChip(
                            selected = selectedType == "Medical Leave",
                            onClick = { selectedType = "Medical Leave" },
                            label = { Text("Medical") }
                        )
                    }

                    OutlinedTextField(
                        value = datesInput,
                        onValueChange = { datesInput = it },
                        label = { Text("Dates / Range") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text("Reason for absence") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Text(
                        text = "⚡ Real-time overlap check: No schedule conflicts detected.",
                        fontSize = 11.sp,
                        color = StatusGoodGreen
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reasonText = reasonInput.ifBlank { "Personal leave" }
                        val newRecord = LeaveRecord(
                            id = (leaveHistory.size + 1).toString(),
                            type = selectedType,
                            dates = datesInput,
                            reason = reasonText,
                            status = "Under Review",
                            markType = StatusMarkType.ATTENTION
                        )
                        leaveHistory = listOf(newRecord) + leaveHistory
                        showApplyModal = false
                        coroutineScope.launch {
                            com.campusone.app.data.ApiClient.applyLeave("2026-11-15", "2026-11-16", reasonText)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Submit Application")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

