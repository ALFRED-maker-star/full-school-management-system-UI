package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AnnouncementsScreen(
    user: UserAccount,
    announcements: List<Announcement>,
    onPostAnnouncement: (title: String, content: String, author: String, role: String, category: String, target: String, pinned: Boolean) -> Unit,
    onToggleRead: (announcementId: String, userId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showComposerDialog by remember { mutableStateOf(false) }

    // Composer Form State
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Academic") }
    var newTargetRole by remember { mutableStateOf("ALL") }
    var newPinned by remember { mutableStateOf(false) }

    val filteredAnnouncements = announcements.filter { anc ->
        val matchesCategory = selectedCategoryFilter == "All" || anc.category == selectedCategoryFilter
        val matchesRole = when (user.role) {
            Role.ADMIN -> true
            Role.TEACHER -> anc.targetRole == "ALL" || anc.targetRole == "TEACHER"
            Role.STUDENT_PARENT -> anc.targetRole == "ALL" || anc.targetRole == "STUDENT_PARENT"
        }
        val matchesSearch = anc.title.contains(searchQuery, ignoreCase = true) || anc.content.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesRole && matchesSearch
    }.sortedWith(compareByDescending<Announcement> { it.isPinned }.thenByDescending { it.id })

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Notices & Announcements",
                subtitle = "Campus bulletins, circulars, and institutional alerts",
                actionButtonText = if (user.role == Role.ADMIN) "+ New Notice" else null,
                actionButtonIcon = Icons.Default.AddComment,
                onActionClick = { showComposerDialog = true }
            )
        }

        // Search & Category Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search announcements...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("announcement_search_input")
                )

                // Category Filter Scroll Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All", "Academic", "Event", "Urgent", "Fee Notice").forEach { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Surface(
                            color = if (isSelected) Navy800 else Slate200,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { selectedCategoryFilter = cat }
                                .testTag("cat_filter_${cat.lowercase().replace(" ", "_")}")
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Slate700,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Announcements Feed
        items(filteredAnnouncements) { anc ->
            val isRead = anc.readByUsers.contains(user.id)
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("announcement_card_${anc.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top row: Badges, pin, time, read status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(
                                text = anc.category,
                                color = if (anc.category == "Urgent") DangerRed else Navy800,
                                backgroundColor = if (anc.category == "Urgent") DangerRedLight else Navy100
                            )
                            if (anc.isPinned) {
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusBadge(text = "PINNED", color = Amber600, backgroundColor = Amber100)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(
                                text = when (anc.targetRole) {
                                    "ALL" -> "School-wide"
                                    "TEACHER" -> "Faculty Only"
                                    else -> "Parents & Students"
                                },
                                color = Teal700,
                                backgroundColor = Teal100
                            )
                        }

                        // Read / Unread Indicator Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onToggleRead(anc.id, user.id) }
                                .testTag("toggle_read_${anc.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isRead) Slate300 else Amber500)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRead) "Read" else "Unread",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRead) Slate400 else Amber600
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = anc.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = anc.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate700,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Slate100)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${anc.authorName} (${anc.authorRole})",
                                fontSize = 12.sp,
                                color = Slate600
                            )
                        }
                        Text(
                            text = anc.timestamp,
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }
            }
        }
    }

    // Admin Composer Dialog
    if (showComposerDialog) {
        AlertDialog(
            onDismissRequest = { showComposerDialog = false },
            title = { Text("Compose School Notice", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Notice Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("modal_composer_title")
                    )

                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Announcement Details") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("modal_composer_content")
                    )

                    // Category dropdown
                    var catDrop by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { catDrop = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Category: $newCategory")
                        }
                        DropdownMenu(expanded = catDrop, onDismissRequest = { catDrop = false }) {
                            listOf("Academic", "Event", "Urgent", "Fee Notice", "Holiday").forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        newCategory = cat
                                        catDrop = false
                                    }
                                )
                            }
                        }
                    }

                    // Target Role dropdown
                    var roleDrop by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { roleDrop = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                when (newTargetRole) {
                                    "ALL" -> "Target: School-wide"
                                    "TEACHER" -> "Target: Faculty"
                                    else -> "Target: Parents & Students"
                                }
                            )
                        }
                        DropdownMenu(expanded = roleDrop, onDismissRequest = { roleDrop = false }) {
                            listOf("ALL" to "School-wide", "TEACHER" to "Faculty Only", "STUDENT_PARENT" to "Parents/Students").forEach { (r, l) ->
                                DropdownMenuItem(
                                    text = { Text(l) },
                                    onClick = {
                                        newTargetRole = r
                                        roleDrop = false
                                    }
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = newPinned, onCheckedChange = { newPinned = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pin Announcement to Top", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newContent.isNotBlank()) {
                            onPostAnnouncement(
                                newTitle,
                                newContent,
                                user.fullName,
                                user.title,
                                newCategory,
                                newTargetRole,
                                newPinned
                            )
                            showComposerDialog = false
                            newTitle = ""
                            newContent = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    modifier = Modifier.testTag("submit_modal_announcement")
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComposerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
