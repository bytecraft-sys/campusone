package com.campusone.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusone.app.data.ApiClient
import com.campusone.app.theme.CampusOneTheme
import com.campusone.app.theme.IndigoPrimary
import com.campusone.app.theme.StatusGoodGreen
import com.campusone.app.ui.student.*
import com.campusone.app.ui.teacher.*

enum class AppRole {
    STUDENT, TEACHER
}

enum class StudentNavScreen {
    HOME, ATTENDANCE, TASKS, CAMPUS, PROFILE, NOTIFICATIONS, LEAVE,
    TIMETABLE, SUBJECTS, NOTICES, LIBRARY, AI_ASSISTANT
}

enum class TeacherNavScreen {
    HOME, CLASSES, ATTENDANCE, ASSIGNMENTS, LEAVE, PROFILE
}

@Composable
fun App() {
    var currentRole by remember { mutableStateOf(AppRole.STUDENT) }
    var studentScreen by remember { mutableStateOf(StudentNavScreen.HOME) }
    var teacherScreen by remember { mutableStateOf(TeacherNavScreen.HOME) }
    var apiStatus by remember { mutableStateOf("Checking...") }

    LaunchedEffect(Unit) {
        val res = ApiClient.checkHealth()
        apiStatus = if (res.contains("status") || res.contains("ok")) "Online" else "Connected"
    }

    CampusOneTheme {
        Scaffold(
            topBar = {
                // Top Persona Switcher Bar with Live Backend Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1127))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "CampusOne",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "● Backend: $apiStatus",
                            color = if (apiStatus.contains("Offline")) Color(0xFFEF4444) else StatusGoodGreen,
                            fontSize = 10.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                currentRole = AppRole.STUDENT
                                studentScreen = StudentNavScreen.HOME
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentRole == AppRole.STUDENT) IndigoPrimary else Color.Transparent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                        ) {
                            Text("Student", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                currentRole = AppRole.TEACHER
                                teacherScreen = TeacherNavScreen.HOME
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentRole == AppRole.TEACHER) IndigoPrimary else Color.Transparent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                        ) {
                            Text("Teacher", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            bottomBar = {
                when (currentRole) {
                    AppRole.STUDENT -> {
                        val isCampusActive = studentScreen in listOf(
                            StudentNavScreen.CAMPUS,
                            StudentNavScreen.TIMETABLE,
                            StudentNavScreen.SUBJECTS,
                            StudentNavScreen.NOTICES,
                            StudentNavScreen.LIBRARY,
                            StudentNavScreen.AI_ASSISTANT
                        )

                        // Student Bottom Nav (PDF Pages 2-5): Home, Attendance, Tasks, Campus, Profile
                        NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                            NavigationBarItem(
                                selected = studentScreen == StudentNavScreen.HOME,
                                onClick = { studentScreen = StudentNavScreen.HOME },
                                icon = { Text("🏠", fontSize = 18.sp) },
                                label = { Text("Home", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = studentScreen == StudentNavScreen.ATTENDANCE,
                                onClick = { studentScreen = StudentNavScreen.ATTENDANCE },
                                icon = { Text("📅", fontSize = 18.sp) },
                                label = { Text("Attendance", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = studentScreen == StudentNavScreen.TASKS,
                                onClick = { studentScreen = StudentNavScreen.TASKS },
                                icon = { Text("📝", fontSize = 18.sp) },
                                label = { Text("Tasks", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = isCampusActive,
                                onClick = { studentScreen = StudentNavScreen.CAMPUS },
                                icon = { Text("🏛️", fontSize = 18.sp) },
                                label = { Text("Campus", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = studentScreen == StudentNavScreen.PROFILE,
                                onClick = { studentScreen = StudentNavScreen.PROFILE },
                                icon = { Text("👤", fontSize = 18.sp) },
                                label = { Text("Profile", fontSize = 10.sp) }
                            )
                        }
                    }
                    AppRole.TEACHER -> {
                        // Teacher Bottom Nav (PDF Pages 6-10): Home, Classes, Attendance, Assignments, Leave, Profile
                        NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                            NavigationBarItem(
                                selected = teacherScreen == TeacherNavScreen.HOME,
                                onClick = { teacherScreen = TeacherNavScreen.HOME },
                                icon = { Text("🏠", fontSize = 16.sp) },
                                label = { Text("Home", fontSize = 9.sp) }
                            )
                            NavigationBarItem(
                                selected = teacherScreen == TeacherNavScreen.CLASSES,
                                onClick = { teacherScreen = TeacherNavScreen.CLASSES },
                                icon = { Text("👥", fontSize = 16.sp) },
                                label = { Text("Classes", fontSize = 9.sp) }
                            )
                            NavigationBarItem(
                                selected = teacherScreen == TeacherNavScreen.ATTENDANCE,
                                onClick = { teacherScreen = TeacherNavScreen.ATTENDANCE },
                                icon = { Text("📅", fontSize = 16.sp) },
                                label = { Text("Attendance", fontSize = 9.sp) }
                            )
                            NavigationBarItem(
                                selected = teacherScreen == TeacherNavScreen.ASSIGNMENTS,
                                onClick = { teacherScreen = TeacherNavScreen.ASSIGNMENTS },
                                icon = { Text("📝", fontSize = 16.sp) },
                                label = { Text("Assignments", fontSize = 8.sp) }
                            )
                            NavigationBarItem(
                                selected = teacherScreen == TeacherNavScreen.LEAVE,
                                onClick = { teacherScreen = TeacherNavScreen.LEAVE },
                                icon = { Text("✉️", fontSize = 16.sp) },
                                label = { Text("Leave", fontSize = 9.sp) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentRole) {
                    AppRole.STUDENT -> {
                        when (studentScreen) {
                            StudentNavScreen.HOME -> StudentHomeScreen(
                                onNavigateToAttendance = { studentScreen = StudentNavScreen.ATTENDANCE },
                                onNavigateToTasks = { studentScreen = StudentNavScreen.TASKS },
                                onNavigateToLeave = { studentScreen = StudentNavScreen.LEAVE },
                                onNavigateToLibrary = { studentScreen = StudentNavScreen.LIBRARY },
                                onNavigateToNotifications = { studentScreen = StudentNavScreen.NOTIFICATIONS }
                            )
                            StudentNavScreen.ATTENDANCE -> StudentAttendanceScreen()
                            StudentNavScreen.TASKS -> StudentTasksScreen()
                            StudentNavScreen.CAMPUS -> StudentCampusHubScreen(
                                onNavigateToSubjects = { studentScreen = StudentNavScreen.SUBJECTS },
                                onNavigateToTimetable = { studentScreen = StudentNavScreen.TIMETABLE },
                                onNavigateToLeave = { studentScreen = StudentNavScreen.LEAVE },
                                onNavigateToNotices = { studentScreen = StudentNavScreen.NOTICES },
                                onNavigateToLibrary = { studentScreen = StudentNavScreen.LIBRARY },
                                onNavigateToAI = { studentScreen = StudentNavScreen.AI_ASSISTANT }
                            )
                            StudentNavScreen.TIMETABLE -> StudentTimetableScreen(
                                onBack = { studentScreen = StudentNavScreen.CAMPUS }
                            )
                            StudentNavScreen.SUBJECTS -> StudentSubjectsScreen(
                                onBack = { studentScreen = StudentNavScreen.CAMPUS }
                            )
                            StudentNavScreen.NOTICES -> StudentNoticesScreen(
                                onBack = { studentScreen = StudentNavScreen.CAMPUS }
                            )
                            StudentNavScreen.LIBRARY -> StudentLibraryScreen(
                                onBack = { studentScreen = StudentNavScreen.CAMPUS }
                            )
                            StudentNavScreen.AI_ASSISTANT -> StudentAIAssistantScreen(
                                onBack = { studentScreen = StudentNavScreen.CAMPUS }
                            )
                            StudentNavScreen.PROFILE -> StudentProfileScreen(
                                onSignOut = { studentScreen = StudentNavScreen.HOME }
                            )
                            StudentNavScreen.NOTIFICATIONS -> StudentNotificationsScreen(
                                onBack = { studentScreen = StudentNavScreen.HOME }
                            )
                            StudentNavScreen.LEAVE -> StudentLeaveScreen()
                        }
                    }
                    AppRole.TEACHER -> {
                        when (teacherScreen) {
                            TeacherNavScreen.HOME -> TeacherHomeScreen(
                                onTakeAttendance = { teacherScreen = TeacherNavScreen.ATTENDANCE },
                                onNavigateToClasses = { teacherScreen = TeacherNavScreen.CLASSES },
                                onNavigateToAssignments = { teacherScreen = TeacherNavScreen.ASSIGNMENTS },
                                onNavigateToLeave = { teacherScreen = TeacherNavScreen.LEAVE }
                            )
                            TeacherNavScreen.CLASSES -> TeacherClassesScreen(
                                onTakeAttendance = { teacherScreen = TeacherNavScreen.ATTENDANCE }
                            )
                            TeacherNavScreen.ATTENDANCE -> TeacherAttendanceScreen(
                                onFinalized = { teacherScreen = TeacherNavScreen.HOME }
                            )
                            TeacherNavScreen.ASSIGNMENTS -> TeacherAssignmentsScreen()
                            TeacherNavScreen.LEAVE -> TeacherLeaveScreen()
                            TeacherNavScreen.PROFILE -> StudentProfileScreen(
                                studentName = "Dr. Alan Turing",
                                studentId = "FAC001",
                                course = "Dept of CSE",
                                section = "All",
                                semester = "Faculty",
                                onSignOut = { teacherScreen = TeacherNavScreen.HOME }
                            )
                        }
                    }
                }
            }
        }
    }
}
