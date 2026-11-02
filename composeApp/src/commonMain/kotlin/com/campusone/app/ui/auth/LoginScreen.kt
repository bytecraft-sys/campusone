package com.campusone.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusone.app.AppRole
import com.campusone.app.data.ApiClient
import com.campusone.app.data.AuthUser
import com.campusone.app.data.RecentAccount
import com.campusone.app.data.SessionStorage
import com.campusone.app.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (role: AppRole, user: AuthUser) -> Unit,
    apiStatus: String = "Online"
) {
    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var recentAccounts by remember { mutableStateOf(SessionStorage.getRecentAccounts()) }

    fun performLogin(idToUse: String = loginId, passToUse: String = password) {
        if (idToUse.isBlank() || passToUse.isBlank()) {
            errorMessage = "Please enter both Login ID and password."
            return
        }
        focusManager.clearFocus()
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            val result = ApiClient.authenticate(idToUse, passToUse)
            isLoading = false
            if (result.success && result.user != null) {
                val role = if (result.user.role.contains("teach") || 
                               result.user.role.contains("fac") || 
                               result.user.role.contains("hod") || 
                               result.user.role.contains("dean") ||
                               idToUse.startsWith("TEA", ignoreCase = true) ||
                               idToUse.startsWith("FAC", ignoreCase = true)) {
                    AppRole.TEACHER
                } else {
                    AppRole.STUDENT
                }
                
                // Add to persistent recent accounts history
                SessionStorage.addRecentAccount(
                    RecentAccount(
                        loginId = result.user.loginId,
                        fullName = result.user.fullName,
                        role = result.user.role
                    )
                )

                onLoginSuccess(role, result.user)
            } else {
                errorMessage = result.errorMessage ?: "Authentication failed. Please verify credentials."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Institutional Brand Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(2.dp, IndigoPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏛️", fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CampusOne",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Student & Faculty ERP System",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Backend Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "●",
                    color = if (apiStatus.contains("Offline")) Color(0xFFEF4444) else StatusGoodGreen,
                    fontSize = 10.sp
                )
                Text(
                    text = "Cloud Backend: $apiStatus",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // White Authentication Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Institutional Sign In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Log in with your assigned CampusOne ID",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Error Notification Banner
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEE2E2), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⚠️", fontSize = 14.sp)
                                Text(
                                    text = errorMessage!!,
                                    fontSize = 12.sp,
                                    color = Color(0xFFB91C1C),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Login ID Field
                    OutlinedTextField(
                        value = loginId,
                        onValueChange = { 
                            loginId = it
                            errorMessage = null 
                        },
                        label = { Text("Institutional Login ID / Email") },
                        placeholder = { Text("e.g., adarsh@rit.com or TEA101") },
                        leadingIcon = { Text("👤", fontSize = 16.sp) },
                        trailingIcon = {
                            if (loginId.isNotEmpty()) {
                                Text(
                                    text = "✕",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.clickable { loginId = "" }
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = { Text("🔒", fontSize = 16.sp) },
                        trailingIcon = {
                            Text(
                                text = if (passwordVisible) "🙈" else "👁️",
                                fontSize = 16.sp,
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { performLogin() }),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    // Remember Me / Stay Logged In Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = IndigoPrimary)
                        )
                        Text(
                            text = "Remember me & keep session logged in",
                            fontSize = 12.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Sign In Submit Button
                    Button(
                        onClick = { performLogin() },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 2.dp))

                    // Remembered Logins History Section
                    if (recentAccounts.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🕒 Remembered Accounts (Tap to switch):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )

                            recentAccounts.take(3).forEach { account ->
                                val isTeacher = account.role.contains("teach") || account.role.contains("fac")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                        .clickable {
                                            loginId = account.loginId
                                            password = if (isTeacher) "TeacherPass123!" else "StudentPass123!"
                                            errorMessage = null
                                            performLogin(loginId, password)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (isTeacher) Color(0xFF14532D) else IndigoPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = account.fullName.take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = account.fullName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "${account.loginId} · ${if (isTeacher) "Faculty" else "Student"}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Login ›",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Quick Demo Switcher Section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Quick Demo Access (One-tap):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Adarsh Student
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEEF2FF))
                                    .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(10.dp))
                                    .clickable {
                                        loginId = "adarsh@rit.com"
                                        password = "StudentPass123!"
                                        errorMessage = null
                                        performLogin("adarsh@rit.com", "StudentPass123!")
                                    }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎓 Adarsh", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndigoPrimary)
                                    Text("adarsh@rit.com", fontSize = 9.sp, color = Color(0xFF4338CA))
                                }
                            }

                            // Teacher Demo Pill
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0FDF4))
                                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                                    .clickable {
                                        loginId = "TEA101"
                                        password = "TeacherPass123!"
                                        errorMessage = null
                                        performLogin("TEA101", "TeacherPass123!")
                                    }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("👨‍🏫 Teacher", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusGoodGreen)
                                    Text("TEA101", fontSize = 9.sp, color = Color(0xFF15803D))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer
            Text(
                text = "CampusOne Mobile Client · v1.0.0",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
