package com.campusone.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.campusone.app.data.ApiClient
import com.campusone.app.data.SavedSession
import com.campusone.app.data.SessionStorage
import com.campusone.app.theme.CampusOneTheme
import com.campusone.app.theme.IndigoPrimary
import com.campusone.app.theme.StatusGoodGreen
import com.campusone.app.ui.auth.LoginScreen
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
    val initialSession = remember { SessionStorage.getSession() }
    var isLoggedIn by remember { mutableStateOf(initialSession != null) }
    var currentRole by remember {
        mutableStateOf(
            if (initialSession?.role?.contains("teach") == true || initialSession?.role?.contains("fac") == true)
                AppRole.TEACHER
            else
                AppRole.STUDENT
        )
    }
    var currentUserFullName by remember { mutableStateOf(initialSession?.fullName ?: "Adarsh") }
    var currentUserLoginId by remember { mutableStateOf(initialSession?.loginId ?: "adarsh@rit.com") }
    
    var studentScreen by remember { mutableStateOf(StudentNavScreen.HOME) }
    var teacherScreen by remember { mutableStateOf(TeacherNavScreen.HOME) }
    var apiStatus by remember { mutableStateOf("Checking...") }

    LaunchedEffect(Unit) {
        val res = ApiClient.checkHealth()
        apiStatus = if (res.contains("status") || res.contains("ok")) "Online" else "Connected"
    }

    LaunchedEffect(initialSession) {
        if (initialSession != null) {
            if (currentRole == AppRole.STUDENT) {
                ApiClient.studentToken = initialSession.token
            } else {
                ApiClient.teacherToken = initialSession.token
            }
        }
    }

    CampusOneTheme {
        if (!isLoggedIn) {
            LoginScreen(
                apiStatus = apiStatus,
                onLoginSuccess = { role, user ->
                    SessionStorage.saveSession(
                        SavedSession(
                            loginId = user.loginId,
                            fullName = user.fullName,
                            role = user.role,
                            token = user.token
                        )
                    )
                    currentRole = role
                    currentUserFullName = user.fullName
                    currentUserLoginId = user.loginId
                    studentScreen = StudentNavScreen.HOME
                    teacherScreen = TeacherNavScreen.HOME
                    isLoggedIn = true
                }
            )
        } else {
            Scaffold(
                topBar = {
                    // Authenticated Top Bar (Separated for Student and Teacher)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D1127))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CampusOne",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "● Backend: $apiStatus",
                                color = if (apiStatus.contains("Offline")) Color(0xFFEF4444) else StatusGoodGreen,
                                fontSize = 10.sp
                            )
                        }

                        // Authenticated Role & User Info with Sign Out Action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Role Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (currentRole == AppRole.STUDENT) Color(0xFF1E293B) else Color(0xFF14532D))
                                    .border(
                                        1.dp, 
                                        if (currentRole == AppRole.STUDENT) IndigoPrimary else StatusGoodGreen, 
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (currentRole == AppRole.STUDENT) "🎓 Student" else "👨‍🏫 Faculty",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Sign Out Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF334155))
                                    .clickable {
                                        SessionStorage.clearSession()
                                        ApiClient.logout()
                                        isLoggedIn = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🚪 Sign out",
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
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
                            // Teacher Bottom Nav (PDF Pages 6-10): Home, Classes, Attendance, Assignments, Leave
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
                                NavigationBarItem(
                                    selected = teacherScreen == TeacherNavScreen.PROFILE,
                                    onClick = { teacherScreen = TeacherNavScreen.PROFILE },
                                    icon = { Text("👤", fontSize = 16.sp) },
                                    label = { Text("Profile", fontSize = 9.sp) }
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
                                    studentName = currentUserFullName,
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
                                    studentName = currentUserFullName,
                                    studentId = currentUserLoginId,
                                    course = "B.Tech CSE",
                                    section = "Section A",
                                    semester = "Semester 5",
                                    onSignOut = {
                                        SessionStorage.clearSession()
                                        ApiClient.logout()
                                        isLoggedIn = false
                                    }
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
                                    teacherName = currentUserFullName,
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
                                    studentName = currentUserFullName,
                                    studentId = currentUserLoginId,
                                    course = "Dept of CSE",
                                    section = "All",
                                    semester = "Faculty",
                                    onSignOut = {
                                        SessionStorage.clearSession()
                                        ApiClient.logout()
                                        isLoggedIn = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
