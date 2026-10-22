package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.components.EduAvatar
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    classes: List<ClassRoom>,
    students: List<Student>,
    teachers: List<Teacher>,
    feeRecords: List<FeeRecord>,
    announcements: List<Announcement>,
    onNavigate: (Screen) -> Unit,
    onPostAnnouncement: (title: String, content: String, author: String, role: String, category: String, target: String, pinned: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var composerTitle by remember { mutableStateOf("") }
    var composerContent by remember { mutableStateOf("") }
    var composerCategory by remember { mutableStateOf("Academic") }
    var composerTarget by remember { mutableStateOf("ALL") }
    var composerPinned by remember { mutableStateOf(false) }

    val totalFeeTarget = 525000.0
    val totalCollected = 482500.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Welcome Header Banner
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Navy800),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_welcome_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        StatusBadge(
                            text = "Executive Administration",
                            color = Teal100,
                            backgroundColor = Teal700.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Academic Operations Overview",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Term 1 (2026-2027) • Week 8 Active • Campus Normal Operations",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Teal500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = Navy900,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // 4 Primary Stat Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Students",
                        value = "1,248",
                        subtitle = "+3.4% enrollment increase",
                        icon = Icons.Default.People,
                        iconBgColor = Navy100,
                        iconTint = Navy800,
                        modifier = Modifier.weight(1f),
                        tag = "stat_students"
                    )
                    StatCard(
                        title = "Faculty & Staff",
                        value = "84",
                        subtitle = "100% active assignments",
                        icon = Icons.Default.School,
                        iconBgColor = Teal100,
                        iconTint = Teal700,
                        modifier = Modifier.weight(1f),
                        tag = "stat_teachers"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Active Classes",
                        value = "${classes.size}",
                        subtitle = "Average 28 students/class",
                        icon = Icons.Default.MeetingRoom,
                        iconBgColor = Amber100,
                        iconTint = Amber600,
                        modifier = Modifier.weight(1f),
                        tag = "stat_classes"
                    )
                    StatCard(
                        title = "Fees Collected",
                        value = "$482,500",
                        subtitle = "91.9% of term billings",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconBgColor = SuccessGreenLight,
                        iconTint = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        tag = "stat_fees"
                    )
                }
            }
        }

        // Quick Shortcuts Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Administrative Actions",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigate(Screen.STUDENTS) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enrol Student", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onNavigate(Screen.CLASSES) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.AddBox, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Class", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onNavigate(Screen.FEES) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Fee", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Announcements Composer Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_announcement_composer")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Navy100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = Navy800, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Announcements Composer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Broadcast notices to faculty, parents, or school-wide",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }
                        StatusBadge(text = "Admin Only", color = Navy800, backgroundColor = Navy100)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = composerTitle,
                        onValueChange = { composerTitle = it },
                        label = { Text("Announcement Headline") },
                        placeholder = { Text("e.g. End of Term Examination Schedule") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("composer_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = composerContent,
                        onValueChange = { composerContent = it },
                        label = { Text("Message Body") },
                        placeholder = { Text("Write your message details...") },
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("composer_content_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Selector
                        var categoryExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { categoryExpanded = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cat: $composerCategory", fontSize = 12.sp, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                listOf("Academic", "Event", "Urgent", "Fee Notice", "Holiday").forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            composerCategory = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Audience Selector
                        var audienceExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { audienceExpanded = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Target: $composerTarget", fontSize = 12.sp, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = audienceExpanded,
                                onDismissRequest = { audienceExpanded = false }
                            ) {
                                listOf("ALL" to "All Campus", "TEACHER" to "Teachers Only", "STUDENT_PARENT" to "Parents/Students").forEach { (code, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            composerTarget = code
                                            audienceExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = composerPinned,
                                onCheckedChange = { composerPinned = it },
                                colors = CheckboxDefaults.colors(checkedColor = Amber500)
                            )
                            Text("Pin Notice to Top", fontSize = 12.sp, color = Slate700)
                        }

                        Button(
                            onClick = {
                                if (composerTitle.isNotBlank() && composerContent.isNotBlank()) {
                                    onPostAnnouncement(
                                        composerTitle,
                                        composerContent,
                                        "Dr. Eleanor Vance",
                                        "Principal",
                                        composerCategory,
                                        composerTarget,
                                        composerPinned
                                    )
                                    composerTitle = ""
                                    composerContent = ""
                                    composerPinned = false
                                }
                            },
                            enabled = composerTitle.isNotBlank() && composerContent.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                            modifier = Modifier.testTag("publish_announcement_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Broadcast Notice", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Recent School Announcements Feed
        item {
            SectionHeader(
                title = "Active Announcements",
                subtitle = "Latest broadcasts published across the institution",
                actionButtonText = "View All",
                actionButtonIcon = Icons.Default.OpenInNew,
                onActionClick = { onNavigate(Screen.ANNOUNCEMENTS) }
            )
        }

        items(announcements.take(3)) { anc ->
            ElevatedCard(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.ANNOUNCEMENTS) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                        }
                        Text(text = anc.timestamp, fontSize = 11.sp, color = Slate400)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = anc.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = anc.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
