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
import androidx.compose.runtime.Composable
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
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun TeacherDashboardScreen(
    user: UserAccount,
    timetableSlots: List<TimetableSlot>,
    announcements: List<Announcement>,
    classes: List<ClassRoom>,
    onNavigate: (Screen) -> Unit,
    onSelectAttendanceClass: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Today's classes for the teacher (Monday/Tuesday schedule for demo)
    val todaySlots = timetableSlots.filter { it.teacherName.contains("Reed") || it.classId in (user.assignedClassIds) }

    val teacherAnnouncements = announcements.filter { it.targetRole == "ALL" || it.targetRole == "TEACHER" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher Banner Header
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Navy800),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_welcome_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EduAvatar(
                        initials = user.avatarInitial,
                        backgroundColor = Teal600,
                        textColor = Color.White,
                        size = 52
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(
                                text = "Faculty Portal",
                                color = Teal100,
                                backgroundColor = Teal700.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Room: Lab 204",
                                color = Amber100,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Senior STEM Faculty • Assigned Classes: Grade 10-A, Grade 10-B",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300
                        )
                    }
                }
            }
        }

        // Quick Entry Cards for Assigned Classes
        item {
            SectionHeader(
                title = "Class Action Links",
                subtitle = "Rapid attendance logging and grading for your students"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick Attendance Card
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onSelectAttendanceClass("cls_10a")
                            onNavigate(Screen.ATTENDANCE)
                        }
                        .testTag("teacher_quick_attendance"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Teal100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = Teal700,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Attendance Register",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Mark roll call for Grade 10-A & 10-B",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Launch Roll Call",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Teal700
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Teal700,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Quick Result Entry Card
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(Screen.RESULTS) }
                        .testTag("teacher_quick_results"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Amber100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Grade,
                                contentDescription = null,
                                tint = Amber600,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Exam Score Entry",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Publish Midterm & Quiz grades",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Grade Students",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber600
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Amber600,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Today's Classes List
        item {
            SectionHeader(
                title = "Today's Schedule & Classes",
                subtitle = "Active lecture and laboratory timetable slots",
                actionButtonText = "Full Timetable",
                actionButtonIcon = Icons.Default.CalendarMonth,
                onActionClick = { onNavigate(Screen.TIMETABLE) }
            )
        }

        items(todaySlots.take(4)) { slot ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Navy100),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "P${slot.periodNumber}",
                                fontWeight = FontWeight.Bold,
                                color = Navy800,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Period",
                                fontSize = 9.sp,
                                color = Navy800.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = slot.subject,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(
                                text = slot.className,
                                color = Teal700,
                                backgroundColor = Teal100
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = slot.timeRange,
                                fontSize = 12.sp,
                                color = Slate600
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.Room,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = slot.roomNumber,
                                fontSize = 12.sp,
                                color = Slate600
                            )
                        }
                    }
                    Button(
                        onClick = {
                            onSelectAttendanceClass(slot.classId)
                            onNavigate(Screen.ATTENDANCE)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = Navy800),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Roll Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Announcements Feed for Faculty
        item {
            SectionHeader(
                title = "Faculty Notices & Announcements",
                subtitle = "School circulars and departmental memos",
                actionButtonText = "All Notices",
                actionButtonIcon = Icons.Default.OpenInNew,
                onActionClick = { onNavigate(Screen.ANNOUNCEMENTS) }
            )
        }

        items(teacherAnnouncements.take(3)) { anc ->
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
                        StatusBadge(
                            text = anc.category,
                            color = if (anc.category == "Urgent") DangerRed else Navy800,
                            backgroundColor = if (anc.category == "Urgent") DangerRedLight else Navy100
                        )
                        Text(text = anc.timestamp, fontSize = 11.sp, color = Slate400)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = anc.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = anc.content,
                        fontSize = 12.sp,
                        color = Slate600,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
