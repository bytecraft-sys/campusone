package com.campusone.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusone.app.theme.*

enum class ProfileDialogType {
    PERSONAL_DETAILS,
    NOTIFICATION_SETTINGS,
    CHANGE_PASSWORD,
    PRIVACY_CONSENT,
    REQUEST_DATA,
    HELP_GRIEVANCE
}

@Composable
fun StudentProfileScreen(
    studentName: String = "Shrey",
    studentId: String = "2023CS042",
    course: String = "B.Tech CSE",
    section: String = "A",
    semester: String = "5",
    onSignOut: () -> Unit = {}
) {
    var showSignOutDialog by remember { mutableStateOf(false) }
    var activeDialog by remember { mutableStateOf<ProfileDialogType?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .verticalScroll(rememberScrollState())
    ) {
        // Dark Blue Top Profile Header (PDF Page 5)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(vertical = 32.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Large Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(IndigoPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = studentName.take(1).uppercase(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = studentName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "ID: $studentId · $course, Sec $section, Sem $semester",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Toast feedback banner
        if (toastMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color(0xFFECFDF5), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = toastMessage!!,
                    color = Color(0xFF065F46),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Profile Menu Items (PDF Page 5)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column {
                    ProfileMenuItem(
                        icon = "👤",
                        label = "Personal details",
                        onClick = { activeDialog = ProfileDialogType.PERSONAL_DETAILS }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    ProfileMenuItem(
                        icon = "🔔",
                        label = "Notification settings",
                        onClick = { activeDialog = ProfileDialogType.NOTIFICATION_SETTINGS }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    ProfileMenuItem(
                        icon = "🔒",
                        label = "Change password",
                        onClick = { activeDialog = ProfileDialogType.CHANGE_PASSWORD }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    ProfileMenuItem(
                        icon = "🛡️",
                        label = "Privacy and consent",
                        onClick = { activeDialog = ProfileDialogType.PRIVACY_CONSENT }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    ProfileMenuItem(
                        icon = "📄",
                        label = "Request my data",
                        onClick = { activeDialog = ProfileDialogType.REQUEST_DATA }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    ProfileMenuItem(
                        icon = "❓",
                        label = "Help and grievance contact",
                        onClick = { activeDialog = ProfileDialogType.HELP_GRIEVANCE }
                    )
                }
            }

            // Sign out button
            OutlinedButton(
                onClick = { showSignOutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Text(
                    text = "Sign out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CampusOne v1.0.0",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }

    // --- 1. PERSONAL DETAILS DIALOG ---
    if (activeDialog == ProfileDialogType.PERSONAL_DETAILS) {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("👤", fontSize = 20.sp)
                    Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow(label = "Full Name", value = studentName)
                    DetailRow(label = "Login ID / Email", value = studentId)
                    DetailRow(label = "Academic Course", value = course)
                    DetailRow(label = "Assigned Section", value = section)
                    DetailRow(label = "Current Semester", value = semester)
                    DetailRow(label = "Enrollment Status", value = "● Active (Good Standing)", isGood = true)
                    DetailRow(label = "Institution", value = "Roorkee Institute of Technology")
                    DetailRow(label = "Guardian Contact", value = "+91 99966 66601")
                    DetailRow(label = "Emergency Contact", value = "Available on record")
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // --- 2. NOTIFICATION SETTINGS DIALOG ---
    if (activeDialog == ProfileDialogType.NOTIFICATION_SETTINGS) {
        var alertAttendance by remember { mutableStateOf(true) }
        var alertLowAttendance by remember { mutableStateOf(true) }
        var alertAssignments by remember { mutableStateOf(true) }
        var alertNotices by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔔", fontSize = 20.sp)
                    Text("Notification Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Configure alert channels for CampusOne:", fontSize = 12.sp, color = Color(0xFF64748B))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Attendance & Timetable", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Daily morning class schedule push", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = alertAttendance, onCheckedChange = { alertAttendance = it })
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Low Attendance Warning", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Immediate alert when below 75%", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = alertLowAttendance, onCheckedChange = { alertLowAttendance = it })
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Assignment Deadlines", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Reminders 24h before due time", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = alertAssignments, onCheckedChange = { alertAssignments = it })
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Campus Notices Broadcast", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("General institutional bulletins", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = alertNotices, onCheckedChange = { alertNotices = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeDialog = null
                        toastMessage = "✓ Notification preferences updated!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Save Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- 3. CHANGE PASSWORD DIALOG ---
    if (activeDialog == ProfileDialogType.CHANGE_PASSWORD) {
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var passError by remember { mutableStateOf<String?>(null) }
        var showPass by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔒", fontSize = 20.sp)
                    Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Institutional passwords must be at least 10 characters long.", fontSize = 12.sp, color = Color(0xFF64748B))

                    if (passError != null) {
                        Text(passError!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = { Text("Current Password", fontSize = 12.sp) },
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password (min 10 chars)", fontSize = 12.sp) },
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm New Password", fontSize = 12.sp) },
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showPass = !showPass }
                    ) {
                        Checkbox(checked = showPass, onCheckedChange = { showPass = it })
                        Text("Show passwords", fontSize = 12.sp, color = Color(0xFF475569))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPass.isEmpty() || newPass.isEmpty()) {
                            passError = "Please fill in all fields"
                        } else if (newPass.length < 10) {
                            passError = "New password must be at least 10 characters"
                        } else if (newPass != confirmPass) {
                            passError = "New passwords do not match"
                        } else {
                            activeDialog = null
                            toastMessage = "✓ Password changed successfully!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Update Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- 4. PRIVACY & CONSENT DIALOG ---
    if (activeDialog == ProfileDialogType.PRIVACY_CONSENT) {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🛡️", fontSize = 20.sp)
                    Text("Privacy & Consent", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Digital Personal Data Protection (DPDP) Act Compliance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "CampusOne processes student data solely for institutional administration, academic tracking, timetable management, and mandatory attendance auditing.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    DetailRow(label = "Consent Status", value = "● Granted & Active", isGood = true)
                    DetailRow(label = "Consent Date", value = "2026-10-30")
                    DetailRow(label = "Data Controller", value = "Campus ERP Administration")
                    DetailRow(label = "Retention Period", value = "Course duration + 3 years statutory")
                    DetailRow(label = "Guardian Consent", value = "Recorded for Minor Students")
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("I Understand")
                }
            }
        )
    }

    // --- 5. REQUEST MY DATA DIALOG ---
    if (activeDialog == ProfileDialogType.REQUEST_DATA) {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📄", fontSize = 20.sp)
                    Text("Request My Data", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Right to Data Portability",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "You can download a machine-readable export of all your academic and institutional records, including attendance timestamps, leave requests, and assignment submissions.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📦 Export Package Includes:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("• Complete finalized attendance logs", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("• Assignment submissions and grading scores", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("• Leave history and medical attachments", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeDialog = null
                        toastMessage = "✓ Data export bundle generated! Sent to $studentId"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Generate Data Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- 6. HELP & GRIEVANCE DIALOG ---
    if (activeDialog == ProfileDialogType.HELP_GRIEVANCE) {
        var grievanceText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("❓", fontSize = 20.sp)
                    Text("Help & Grievance Contact", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Institutional Helpdesk & Support",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📞 Helpline: +91 1800-CAMPUS-1", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("✉️ Email: support@campusone.edu", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("🏛️ Grievance Officer: grievance.officer@campusone.edu", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("⏰ Hours: Monday - Friday, 09:00 - 17:00 IST", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }

                    Text("Submit Grievance / Query:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = grievanceText,
                        onValueChange = { grievanceText = it },
                        placeholder = { Text("Describe your query or attendance grievance...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeDialog = null
                        toastMessage = if (grievanceText.isNotBlank()) "✓ Grievance submitted! Ticket #GRV-${(1000..9999).random()} created." else "✓ Support contact details acknowledged."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    // --- 7. SIGN OUT CONFIRMATION DIALOG ---
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out of CampusOne?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Sign out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isGood: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isGood) Color(0xFF16A34A) else Color(0xFF0F172A)
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: String,
    label: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = "›",
            fontSize = 20.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Bold
        )
    }
}
