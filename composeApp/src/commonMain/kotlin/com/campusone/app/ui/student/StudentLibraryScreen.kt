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
import com.campusone.app.ui.components.StatusMarkChip
import com.campusone.app.ui.components.StatusMarkType

data class LibraryBook(
    val title: String,
    val author: String,
    val statusText: String,
    val markType: StatusMarkType
)

@Composable
fun StudentLibraryScreen(
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Catalogue") }

    val books = remember {
        listOf(
            LibraryBook("Introduction to Algorithms (4th Ed.)", "Thomas H. Cormen, Charles E. Leiserson", "2 available", StatusMarkType.GOOD),
            LibraryBook("Operating System Concepts", "Abraham Silberschatz, Peter B. Galvin", "All on loan", StatusMarkType.ATTENTION),
            LibraryBook("Database System Concepts", "Abraham Silberschatz, Henry F. Korth", "5 available", StatusMarkType.GOOD),
            LibraryBook("Computer Networking: A Top-Down Approach", "James F. Kurose, Keith W. Ross", "1 available", StatusMarkType.GOOD)
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
                .padding(horizontal = 20.dp, vertical = 20.dp)
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
                Text(
                    text = "Library",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar (PDF Page 5)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search title, author or ISBN") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Text("🔍", fontSize = 16.sp) },
                singleLine = true
            )

            // Tabs: Catalogue, My loans, Reservations
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Catalogue", "My loans", "Reservations").forEach { tab ->
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

            // Borrowed Now Card (PDF Page 5)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Borrowed now", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Database System Concepts", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            Text("Due 15 Nov 2026", fontSize = 12.sp, color = IndigoPrimary, fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Renew", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Fine due: ₹0.00", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }

            Text("Catalogue", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(books) { book ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(book.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(book.author, fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                            StatusMarkChip(label = book.statusText, type = book.markType)
                        }
                    }
                }
            }
        }
    }
}

