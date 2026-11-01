package com.campusone.app.ui.student

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

data class ChatMessage(
    val sender: String, // "AI" or "USER"
    val text: String,
    val source: String? = null
)

@Composable
fun StudentAIAssistantScreen(
    onBack: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("AI", "Hi Shrey! Ask me about a subject, or how to find something in the app."),
                ChatMessage("USER", "What is my attendance in CS302?"),
                ChatMessage("AI", "Your attendance in CS302 Operating Systems is 71%, which is currently below the 75% minimum threshold. You need to attend the next 3 consecutive lectures to cross 75%.", source = "Source: your attendance summary")
            )
        )
    }

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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "←",
                    fontSize = 22.sp,
                    color = Color.White,
                    modifier = Modifier.clickable { onBack() }
                )
                Column {
                    Text(
                        text = "AI assistant",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Academic help and app guidance",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Chat messages list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    if (msg.sender == "USER") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .background(IndigoPrimary, RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp))
                                    .padding(12.dp)
                            ) {
                                Text(msg.text, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 300.dp)
                                    .background(SurfaceWhite, RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(msg.text, color = Color(0xFF0F172A), fontSize = 13.sp)
                                if (msg.source != null) {
                                    Text(msg.source, color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // Suggestion prompt chips (PDF Page 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Explain a topic", "Where is leave?", "Due tomorrow?").forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE2E8F0))
                            .clickable {
                                messages = messages + ChatMessage("USER", prompt)
                                val reply = when (prompt) {
                                    "Where is leave?" -> "You can apply for leave directly under Campus Hub > Leave or the Leave tab on the bottom bar. It performs real-time timetable conflict checks."
                                    "Due tomorrow?" -> "You have 1 pending task due tomorrow at 11:59 PM: CS301 Binary Search Tree Implementation (20 points)."
                                    else -> "What topic would you like explained? I can help with Data Structures, Operating Systems, or DBMS."
                                }
                                messages = messages + ChatMessage("AI", reply)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(prompt, fontSize = 11.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Text(
                text = "AI can make mistakes. Check important details.",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type your question...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceWhite, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userQ = inputText
                            inputText = ""
                            messages = messages + ChatMessage("USER", userQ)
                            messages = messages + ChatMessage("AI", "I am looking into '$userQ' for your semester 5 courses.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text("➤", fontSize = 14.sp)
                }
            }
        }
    }
}

