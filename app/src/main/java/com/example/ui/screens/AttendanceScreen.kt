package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.components.EduAvatar
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AttendanceScreen(
    user: UserAccount,
    classes: List<ClassRoom>,
    students: List<Student>,
    selectedClassId: String,
    selectedDate: String,
    draftAttendance: Map<String, AttendanceStatus>,
    onSelectClass: (String) -> Unit,
    onSelectDate: (String) -> Unit,
    onSetStatus: (studentId: String, status: AttendanceStatus) -> Unit,
    onMarkAllPresent: (List<String>) -> Unit,
    onSaveAttendance: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Mode toggle: "Daily Register" vs "School Overview"
    var activeTab by remember {
        mutableStateOf(if (user.role == Role.ADMIN) "Overview" else "Register")
    }

    val selectedClassObj = classes.find { it.id == selectedClassId } ?: classes.firstOrNull()
    val classStudents = students.filter { it.gradeClassId == selectedClassId }

    // Summary counts
    val presentCount = draftAttendance.values.count { it == AttendanceStatus.PRESENT }
    val absentCount = draftAttendance.values.count { it == AttendanceStatus.ABSENT }
    val lateCount = draftAttendance.values.count { it == AttendanceStatus.LATE }
    val excusedCount = draftAttendance.values.count { it == AttendanceStatus.EXCUSED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Attendance Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Real-time roll call logging & institution-wide analytics",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }

                // Segmented tab
                Surface(
                    color = Slate200,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        listOf("Register", "Overview").forEach { tab ->
                            val isSelected = activeTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Navy800 else Color.Transparent)
                                    .clickable { activeTab = tab }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("attendance_tab_${tab.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Slate700,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        if (activeTab == "Register") {
            // Teacher-Facing Register Controls
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Class Selector
                            var classDropdownOpen by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { classDropdownOpen = true },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("select_attendance_class")
                                ) {
                                    Text("Class: ${selectedClassObj?.name ?: "Select Class"}", fontSize = 13.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(
                                    expanded = classDropdownOpen,
                                    onDismissRequest = { classDropdownOpen = false }
                                ) {
                                    classes.forEach { cls ->
                                        DropdownMenuItem(
                                            text = { Text(cls.name) },
                                            onClick = {
                                                onSelectClass(cls.id)
                                                classDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Date Badge
                            Surface(
                                color = Slate100,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Navy800, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(selectedDate, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Slate800)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusBadge("Present: $presentCount", SuccessGreen, SuccessGreenLight, Modifier.weight(1f))
                            StatusBadge("Absent: $absentCount", DangerRed, DangerRedLight, Modifier.weight(1f))
                            StatusBadge("Late: $lateCount", Amber600, Amber100, Modifier.weight(1f))
                            StatusBadge("Excused: $excusedCount", InfoBlue, InfoBlueLight, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Batch Action & Save Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onMarkAllPresent(classStudents.map { it.id }) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("mark_all_present_btn")
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark All Present", fontSize = 12.sp, color = Slate700)
                            }

                            Button(
                                onClick = onSaveAttendance,
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("save_attendance_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Roll Call", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Student Register Roll List
            items(classStudents) { stu ->
                val currentStatus = draftAttendance[stu.id] ?: AttendanceStatus.PRESENT
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("attendance_row_${stu.rollNumber}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EduAvatar(
                                    initials = stu.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                    backgroundColor = Navy800,
                                    size = 36
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = stu.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Roll #${stu.rollNumber} • Avg: ${stu.attendancePercentage}%",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                            }

                            // Current Status Tag
                            StatusBadge(
                                text = currentStatus.label,
                                color = Color(currentStatus.color),
                                backgroundColor = Color(currentStatus.color).copy(alpha = 0.15f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Selection Segmented Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AttendanceStatus.values().forEach { st ->
                                val isSelected = currentStatus == st
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) Color(st.color) else Slate100
                                        )
                                        .clickable { onSetStatus(stu.id, st) }
                                        .padding(vertical = 6.dp)
                                        .testTag("status_btn_${stu.rollNumber}_${st.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = st.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Slate700
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Admin-Facing Campus Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "School Attendance Today",
                        value = "96.2%",
                        subtitle = "+0.8% vs monthly average",
                        icon = Icons.Default.EventAvailable,
                        iconBgColor = SuccessGreenLight,
                        iconTint = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Unexcused Absences",
                        value = "14",
                        subtitle = "Across all 32 classes",
                        icon = Icons.Default.Warning,
                        iconBgColor = DangerRedLight,
                        iconTint = DangerRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SectionHeader(
                    title = "Class-by-Class Attendance Rates",
                    subtitle = "Real-time compliance summary per cohort"
                )
            }

            items(classes) { cls ->
                val simulatedRate = when (cls.id) {
                    "cls_10a" -> 95.8f
                    "cls_10b" -> 97.0f
                    "cls_9a" -> 99.1f
                    "cls_11a" -> 94.0f
                    else -> 96.5f
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = cls.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = "Class Teacher: ${cls.classTeacherName} • ${cls.roomNumber}",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                            StatusBadge(
                                text = "$simulatedRate%",
                                color = if (simulatedRate >= 95) SuccessGreen else Amber600,
                                backgroundColor = if (simulatedRate >= 95) SuccessGreenLight else Amber100
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { simulatedRate / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (simulatedRate >= 95) SuccessGreen else Amber600,
                            trackColor = Slate200
                        )
                    }
                }
            }
        }
    }
}
